package com.studip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.studip.api.API;
import com.studip.api.ManagedObjectListener;
import com.studip.api.Messages;
import com.studip.api.RouteCallback;
import com.studip.api.rest.StudipListObject;
import com.studip.api.rest.StudipMessage;
import com.studip.api.rest.StudipSearchResult;
import com.studip.api.rest.StudipSearchUser;
import com.studip.api.rest.StudipUser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MessagesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    MessagesAdapter adapter;
    Handler h;
    
    
    volatile boolean can_send = false; // gets set when the messages/write request completed
    
    private Callback listener = new Callback();
    
    View dialog_layout;
    AlertDialog message_write_dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        
        
        // route: /user/:user_id/inbox
        // HEAD request doesn't seem to work

        h = HandlerCompat.createAsync(Looper.getMainLooper());
        if (Data.messages_provider == null)
        {
            Data.messages_provider = new Messages(h);
        }
        Data.messages_provider.addRefreshListener(listener);
        
        // TODO test sending a message to more than one person
        
        
        // TODO option to only search in contacts while not connected to wifi

        

        SwipeRefreshLayout ref = v.findViewById(R.id.messages_refresh);
        ref.setOnRefreshListener(this);
        
        
        ListView l = v.findViewById(R.id.messages_list);
        adapter = new MessagesAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
        l.setAdapter(adapter);
        
        v.findViewById(R.id.message_write).setOnClickListener(this::OnNewMessage);
        
        
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null)
        {
            if (Data.messages == null)
            {
                Data.messages_provider.refresh();
            }
        }
        else
        {
            if (savedInstanceState.getString("subject") != null)
            {
                StudipSearchUser[] users = (StudipSearchUser[]) savedInstanceState.getSerializable("addressees");
                String subject = savedInstanceState.getString("subject");
                String content = savedInstanceState.getString("message-content");
                String ad_edit = savedInstanceState.getString("addressee-edit");
                OnNewMessage(null);
                ((TextView)dialog_layout.findViewById(R.id.message_addressee)).setText(ad_edit);
                ((TextView)dialog_layout.findViewById(R.id.message_subject_edit)).setText(subject);
                ((TextView)dialog_layout.findViewById(R.id.message_content_edit)).setText(content);
                AddresseeListAdapter ad = (AddresseeListAdapter) ((ListView)dialog_layout.findViewById(R.id.message_addressee_list)).getAdapter();
                ad.users = users;
                ad.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (message_write_dialog != null && message_write_dialog.isShowing())
        {
            AddresseeListAdapter ad = (AddresseeListAdapter) ((ListView)dialog_layout.findViewById(R.id.message_addressee_list)).getAdapter();
            outState.putSerializable("addressees",ad.users);
            TextView subj = dialog_layout.findViewById(R.id.message_subject_edit);
            outState.putString("subject",subj.getText().toString());
            TextView cont = dialog_layout.findViewById(R.id.message_content_edit);
            outState.putString("message-content",cont.getText().toString());
            TextView ad_edit = dialog_layout.findViewById(R.id.message_addressee);
            outState.putString("addressee-edit",ad_edit.getText().toString());
        }
        
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (message_write_dialog != null)
        {
            message_write_dialog.dismiss();
        }
        Data.messages_provider.removeRefreshListener(listener);
    }


    @Override
    public void onRefresh()
    {
        Data.messages_provider.refresh();
    }
    
    private class Callback extends ManagedObjectListener<StudipListObject>
    {

        @Override
        public void callback(StudipListObject obj, Exception error)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.messages_refresh);
            StudipListObject l  = Data.messages_provider.getData();
            Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
            Data.messages = new StudipMessage[l.collection.size()];
            Data.senders = new StudipUser[Data.messages.length];
            //System.out.println(l.pagination.total);
            int index = 0;
            while (it.hasNext())
            {
                JsonElement e = it.next().getValue();
                try
                {
                    Data.messages[index] = Data.gson.fromJson(e,StudipMessage.class);
                }
                catch (JsonSyntaxException ignored)
                {
                    Data.messages = null;
                    ref.setRefreshing(false);
                    return;
                };
                index++;
            }
            adapter.notifyDataSetChanged();
            for (int i = 0;i<Data.messages.length;i++)
            {
                //System.out.println(Data.messages[i].sender);
                Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new BlankRoute(Data.messages[i].sender),new GetSubjectData(i)));
            }
        }
    }

    
    
    private class GetSubjectData extends RouteCallback
    {
        private final int index;
        public GetSubjectData(int index)
        {
            this.index = index;
        }
        @Override
        public void routeFinished(String result, Exception error)
        {
            if (result != null)
            {
                StudipUser u = Data.gson.fromJson(result, StudipUser.class);
                Data.senders[index] = u;
                h.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        boolean is_null = false;
                        for (int i = 0; i < Data.senders.length; i++)
                        {
                            if (Data.senders[i] == null)
                            {
                                is_null = true;
                                break;
                            }
                        }
                        if (!is_null)
                        {
                            View v = getView();
                            if (v != null)
                            {
                                SwipeRefreshLayout ref = v.findViewById(R.id.messages_refresh);
                                ref.setRefreshing(false);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            if (error != null)
            {
                error.printStackTrace();
            }
        }
    }


    public void OnAdd(View v)
    {
        //System.out.println("add");
        try
        {
            Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new DispatchRoute("multipersonsearch/ajax_search/add_adressees?s="
                    + URLEncoder.encode(((AutoCompleteTextView)dialog_layout.findViewById(R.id.message_addressee)).getText().toString(),"UTF-8")), new RouteCallback()
            {
                @Override
                public void routeFinished(String result, Exception error)
                {
                    //System.out.println("route finished");
                    if (result != null)
                    {
                        StudipSearchUser[] users;
                        try
                        {
                            users = Data.gson.fromJson(result, StudipSearchUser[].class);
                        } catch (JsonSyntaxException e)
                        {
                            return;
                        }
                        for (int i = 0;i<users.length;i++)
                        {
                            int index = users[i].text.indexOf(" --");
                            if (index != -1)
                            {
                                users[i].text = users[i].text.substring(0,index);
                            }
                        }
                        //System.out.println("user results");
                        for (int i = 0;i<users.length;i++)
                        {
                            if (users[i].text.equals(((AutoCompleteTextView)dialog_layout.findViewById(R.id.message_addressee)).getText().toString()))
                            {
                                //System.out.println("matching user");
                                final int finalI = i;
                                h.post(() ->
                                {
                                    ((AutoCompleteTextView)dialog_layout.findViewById(R.id.message_addressee)).setText("");
                                    AddresseeListAdapter ad = (AddresseeListAdapter) ((ListView)dialog_layout.findViewById(R.id.message_addressee_list)).getAdapter();
                                    if (ad.users != null)
                                    {
                                        for (int a = 0;a<ad.users.length;a++)
                                        {
                                            if (ad.users[a].text.equals(users[finalI].text))
                                            {
                                                return;
                                            }
                                        }
                                        ad.users = Arrays.copyOf(ad.users, ad.users.length + 1);
                                        ad.users[ad.users.length - 1] = users[finalI];
                                        //System.out.println("user added: "+ad.users.length);
                                    }
                                    else
                                    {
                                        ad.users = new StudipSearchUser[1];
                                        ad.users[0] = users[finalI];
                                    }
                                    ad.notifyDataSetChanged();
                                });
                                return;
                            }
                        }
                        h.post(() ->
                        {
                            AddresseeListAdapter ad = (AddresseeListAdapter) ((AutoCompleteTextView)dialog_layout.findViewById(R.id.message_addressee)).getAdapter();
                            ad.users = users;
                            ad.notifyDataSetChanged();;
                        });
                    }
                    if (error != null)
                    {
                        can_send = false;
                        message_write_dialog.dismiss();
                        error.printStackTrace();
                    }
                }
            }));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
    public void OnSend(View v)
    {
        AddresseeListAdapter ad = (AddresseeListAdapter) ((ListView)dialog_layout.findViewById(R.id.message_addressee_list)).getAdapter();
        TextView subj = dialog_layout.findViewById(R.id.message_subject_edit);
        TextView cont = dialog_layout.findViewById(R.id.message_content_edit);
        String subject = subj.getText().toString();
        String content = cont.getText().toString();
        StudipSearchUser[] users = ad.users;
        if (users == null || users.length == 0)
        {
            Toast.makeText(getActivity(),R.string.message_no_addressee,Toast.LENGTH_SHORT).show();
            return;
        }
        if (subject.equals(""))
        {
            Toast.makeText(getActivity(),R.string.message_no_subject,Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.equals(""))
        {
            Toast.makeText(getActivity(),R.string.message_no_content,Toast.LENGTH_SHORT).show();
            return;
        }
        try
        {
            StringBuilder data = new StringBuilder("subject=" + URLEncoder.encode(subject, "UTF-8") + "&message=" + URLEncoder.encode(content, "UTF-8"));
            for (int i = 0; i < users.length; i++)
            {
                data.append("&recipients[]=").append(URLEncoder.encode(users[i].user_id, "UTF-8"));
            }
            try
            {
                Data.api.submit(Data.api.new SendMessageRoute(data.toString())).get();
                message_write_dialog.dismiss();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            catch (InterruptedException ignored) {}
        } catch (UnsupportedEncodingException ignored) {}
    }
    private class AddresseeListAdapter extends ArrayAdapter
    {
        StudipSearchUser[] users;
        public AddresseeListAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }

        @Nullable
        @Override
        public Object getItem(int position)
        {
            if (users == null || position >= users.length)
            {
                return null;
            }
            else
            {
                return users[position].text;
            }
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            TextView v;
            if (convertView != null)
            {
                v = (TextView) convertView;
            }
            else
            {
                v = new TextView(getActivity());
            }
            if (users == null || position >= users.length)
            {
                return null;
            }
            v.setText(users[position].text);
            return v;
        }
        @Override
        public int getCount()
        {
            if (users == null)
            {
                return 0;
            }
            return users.length;
        }
    }
    
    public void OnNewMessage(View v)
    {
        if (! can_send)
        {
            try
            {
                // this has to be called once per session for the multiperson search to work
                Data.api.submit(Data.api.new DispatchRoute("messages/write")).get();
                can_send = true;
            }
            catch (Exception ignored)
            {
                return;
            }
        }
        View c = getLayoutInflater().inflate(R.layout.dialog_new_message,null);
        dialog_layout = c;
        c.findViewById(R.id.message_scroll).setNestedScrollingEnabled(true);
        c.findViewById(R.id.message_scroll).startNestedScroll(View.SCROLL_AXIS_VERTICAL);
        c.findViewById(R.id.addressee_add).setOnClickListener(this::OnAdd);
        c.findViewById(R.id.message_send).setOnClickListener(this::OnSend);
        ((AutoCompleteTextView)c.findViewById(R.id.message_addressee)).setAdapter(new AddresseeListAdapter(getActivity(),ArrayAdapter.NO_SELECTION));
        ((ListView)c.findViewById(R.id.message_addressee_list)).setAdapter(new AddresseeListAdapter(getActivity(),ArrayAdapter.NO_SELECTION));
        
        message_write_dialog = new AlertDialog.Builder(getActivity()).setView(c).create();
        message_write_dialog.show();
    }
    
    
    
    
    
    public class MessageClicked implements View.OnClickListener, View.OnLongClickListener
    {
        int index;
        public MessageClicked(int index)
        {
            this.index = index;
        }
        @Override
        public void onClick(View v)
        {
            View layout = getLayoutInflater().inflate(R.layout.message_view_dialog,null);
            TextView t = layout.findViewById(R.id.message_content);
            Document d = Jsoup.parse(Data.messages[index].message_html);
            t.setText(d.wholeText());
            new AlertDialog.Builder(getActivity()).setTitle(Data.messages[index].subject).setView(layout).show();
        }
        @Override
        public boolean onLongClick(View v)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.messages_refresh);
            new AlertDialog.Builder(getActivity()).setTitle(R.string.message_delete_title).setMessage(getResources().getString(R.string.message_delete_msg)+" "+Data.messages[index].subject)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ref.setRefreshing(true);
                        Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new MessageRoute("",API.METHOD_DELETE,Data.messages[index].message_id), new RouteCallback()
                        {
                            @Override
                            public void routeFinished(String result, Exception error)
                            {
                                h.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Data.messages_provider.refresh();
                                    }
                                });
                            }
                        }));
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                }).show();
            return true;
        }
    }
    
    public class MessagesAdapter extends ArrayAdapter
    {
        public MessagesAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }
        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            View v;
            if (convertView != null)
            {
                v = convertView;
            }
            else
            {
                v = getLayoutInflater().inflate(R.layout.message_entry,parent,false);
            }
            MessageClicked m = new MessageClicked(position);
            v.setOnClickListener(m);
            v.setOnLongClickListener(m);
            TextView subject = v.findViewById(R.id.message_subject);
            subject.setText(Data.messages[position].subject);
            if (Data.messages[position].unread)
            {
                //System.out.println("unread message");
                subject.setTextColor(Color.RED);
            }
            else
            {
                subject.setTextColor(Color.BLACK); // TODO adjust for dark theme
            }
            TextView sender = v.findViewById(R.id.message_sender);
            if (Data.senders != null && Data.senders[position] != null)
            {
                sender.setText(Data.senders[position].name.formatted);
            }
            else
            {
                sender.setText("");
            }
            TextView time = v.findViewById(R.id.message_time);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(1000*Long.parseLong(Data.messages[position].mkdate));
            time.setText(c.get(Calendar.DATE)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
            return v;
        }

        @Override
        public int getCount()
        {
            if (Data.messages == null)
            {
                return 0;
            }
            return Data.messages.length;
        }
    }
    
    
    
    
    
    
    
    
    
    
}