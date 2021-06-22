package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipSearchUser;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.databinding.DialogNewMessageBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.ui.DeepLinkActivity;
import org.studip.unofficial_app.ui.HomeActivity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageWriteDialogFragment extends DialogFragment
{
    public static final String ADDRESSEE_KEY = "addressee";
    private static final String ADDRESSEE_LIST_KEY = "addressee_list";
    private static final String ADDRESSEE_LIST_AUTOCOMPLETE_KEY = "addressee_list_autocomplete";
    public static final String SUBJECT_KEY = "subject";
    public static final String CONTENT_KEY = "content";
    

    private DialogNewMessageBinding b;
    private AddresseeAdapter ad;
    private AddresseeAdapter ad_autocomplete;
    
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        
        outState.putString(ADDRESSEE_KEY,b.messageAddressee.getText().toString());
        StudipUser[] ads = new StudipUser[ad.getCount()];
        for (int i = 0;i<ads.length;i++) {
            ads[i] = ad.getItem(i);
        }
        outState.putSerializable(ADDRESSEE_LIST_KEY,ads);
        ads = new StudipUser[ad_autocomplete.getCount()];
        for (int i = 0;i<ads.length;i++) {
            ads[i] = ad_autocomplete.getItem(i);
        }
        outState.putSerializable(ADDRESSEE_LIST_AUTOCOMPLETE_KEY,ads);
        outState.putString(SUBJECT_KEY,b.messageSubjectEdit.getText().toString());
        outState.putString(CONTENT_KEY,b.messageContentEdit.getText().toString());
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        b = DialogNewMessageBinding.inflate(getLayoutInflater());
        b.messageScroll.setNestedScrollingEnabled(true);
        
        
        builder.setView(b.getRoot());
        
        ad = new AddresseeAdapter(requireActivity(), R.layout.list_textview);
        ad_autocomplete = new AddresseeAdapter(requireActivity(), R.layout.list_textview);
    
        if (savedInstanceState == null && getArguments() != null) {
            Bundle args = getArguments();
            b.messageSubjectEdit.setText(args.getString(SUBJECT_KEY, ""));
            b.messageContentEdit.setText(args.getString(CONTENT_KEY, ""));
            String adr = args.getString(ADDRESSEE_KEY);
            if (adr != null) {
                LiveData<StudipUser> d = DBProvider.getDB(requireActivity()).userDao().observe(adr);
                d.observe(this, u -> {
                    d.removeObservers(this);
                    if (u != null) {
                        ad.add(u);
                    }
                });
            }
        }
        
        
        b.messageAddresseeList.setAdapter(ad);
        b.messageAddressee.setAdapter(ad_autocomplete);
        b.messageAddressee.setThreshold(3);
        
        if (savedInstanceState != null) {
            b.messageAddressee.setText(savedInstanceState.getString(ADDRESSEE_KEY));
            ad.addAll((StudipUser[]) savedInstanceState.getSerializable(ADDRESSEE_LIST_KEY));
            ad_autocomplete.addAll((StudipUser[]) savedInstanceState.getSerializable(ADDRESSEE_LIST_AUTOCOMPLETE_KEY));
            b.messageSubjectEdit.setText(savedInstanceState.getString(SUBJECT_KEY));
            b.messageContentEdit.setText(savedInstanceState.getString(CONTENT_KEY));
        }

        final boolean[] showOnUpdate = new boolean[1];
    
        DBProvider.getDB(requireActivity()).userDao().observeAll().observe(this,(users) -> {
            ad_autocomplete.clear();
            if (users != null)
            {
                ad_autocomplete.addAll(users);
            }
            if (showOnUpdate[0] && ! b.messageAddressee.isPopupShowing()) {
                b.messageAddressee.requestFocus();
                b.messageAddressee.setText(b.messageAddressee.getText());
            }
        });
        
        b.messageAddresseeList.setOnItemLongClickListener((parent, view, position, id) ->
        {
            ad.remove(ad.getItem(position));
            return true;
        });
        
        b.messageAddressee.setOnItemClickListener((parent, view, position, id) ->
        {
            //System.out.println(ad_autocomplete.getItem(position).toString());
            StudipUser u = ad_autocomplete.getItem(position);
            boolean already = false;
            for (int i = 0;i<ad.getCount();i++) {
                if (ad.getItem(i).user_id.equals(u.user_id)) {
                    already = true;
                    break;
                }
            }
            if (! already) {
                ad.add(u);
            }
            b.messageAddressee.setText("");
        });

        final boolean[] canSearch = new boolean[1];
        canSearch[0] = false;

        APIProvider.getAPI(requireActivity()).dispatch.startMessage().enqueue(new Callback<Void>()
        {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response)
            {
                if (response.code() == 200) {
                    canSearch[0] = true;
                }
                HomeActivity.onStatusReturn(requireActivity(),response.code());
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t)
            {
                Toast.makeText(requireActivity(),R.string.message_no_search,Toast.LENGTH_SHORT).show();
            }
        });
        
        
        b.addresseeAdd.setOnClickListener((v) -> {
            if (! canSearch[0]) {
                APIProvider.getAPI(requireActivity()).dispatch.startMessage().enqueue(new Callback<Void>()
                {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response)
                    {
                        if (response.code() == 200) {
                            canSearch[0] = true;
                            // call again, now that search is possible
                            b.addresseeAdd.callOnClick();
                        }
                        HomeActivity.onStatusReturn(requireActivity(),response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t)
                    {
                        Toast.makeText(requireActivity(),R.string.message_no_search,Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                API api = APIProvider.getAPI(requireActivity());
                DB db = DBProvider.getDB(requireActivity());
                api.dispatch.searchAddresses(b.messageAddressee.getText().toString()).enqueue(new Callback<StudipSearchUser[]>()
                {
                    @Override
                    public void onResponse(@NonNull Call<StudipSearchUser[]> call, @NonNull Response<StudipSearchUser[]> response)
                    {
                        StudipSearchUser[] users = response.body();
                        if (users != null) {
                            showOnUpdate[0] = true;
                            for (StudipSearchUser u : users) {
                                final AtomicInteger count = new AtomicInteger(0);
                                //System.out.println("found user: "+u.text);
                                api.user.user(u.user_id).enqueue(new Callback<StudipUser>()
                                {
                                    @Override
                                    public void onResponse(@NonNull Call<StudipUser> call, @NonNull Response<StudipUser> response)
                                    {
                                        StudipUser u = response.body();
                                        if (u != null) {
                                            Schedulers.io().scheduleDirect(() -> {
                                                db.userDao().updateInsert(u);
                                                if (count.incrementAndGet() == users.length) {
                                                    showOnUpdate[0] = false;
                                                    //System.out.println("finished");
                                                }
                                            });
                                        } else {
                                            if (count.incrementAndGet() == users.length) {
                                                showOnUpdate[0] = false;
                                                //System.out.println("finished");
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(@NonNull Call<StudipUser> call, @NonNull Throwable t)
                                    {
                                        if (count.incrementAndGet() == users.length) {
                                            showOnUpdate[0] = false;
                                            //System.out.println("finished");
                                        }
                                    }
                                });
                            }
                        }
                        HomeActivity.onStatusReturn(requireActivity(),response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<StudipSearchUser[]> call, @NonNull Throwable t) {}
                });
            }
        });
        
        
        b.messageSend.setOnClickListener(v ->
        {
            if (ad.getCount() == 0) {
                Toast.makeText(requireActivity(),R.string.message_no_addressee,Toast.LENGTH_SHORT).show();
                return;
            }
            if (b.messageSubjectEdit.getText().length() == 0) {
                Toast.makeText(requireActivity(),R.string.message_no_subject,Toast.LENGTH_SHORT).show();
                return;
            }
            if (b.messageContentEdit.getText().length() == 0) {
                Toast.makeText(requireActivity(),R.string.message_no_content,Toast.LENGTH_SHORT).show();
                return;
            }
            String[] recipients = new String[ad.getCount()];
            for (int i = 0;i<recipients.length;i++) {
                recipients[i] = ad.getItem(i).user_id;
            }
            APIProvider.getAPI(requireActivity()).message.create(b.messageSubjectEdit.getText().toString(),b.messageContentEdit.getText().toString(),recipients).enqueue(new Callback<Void>()
            {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response)
                {
                    if (response.code() != 201)
                    {
                        Toast.makeText(requireActivity(),R.string.message_no_send,Toast.LENGTH_SHORT).show();
                        HomeActivity.onStatusReturn(requireActivity(),response.code());
                        //System.out.println(response.code());
                    } else {
                        /*
                        ShortcutInfoCompat.Builder info = new ShortcutInfoCompat.Builder(requireActivity(), "person:"+recipients[0]);
                        final StudipUser u = ad.getItem(0);
                        info.setShortLabel(u.name.formatted);
                        HashSet<String> cat = new HashSet<>();
                        cat.add("org.studip.unofficial_app.TEXT_SHARE_TARGET");
                        info.setCategories(cat);
                        info.setIcon(IconCompat.createWithResource(requireActivity(), R.drawable.mail_blue));
                        Intent i = new Intent(requireActivity(), DeepLinkActivity.class);
                        i.setAction(requireActivity().getPackageName()+".dynamic_shortcut");
                        i.setData(Uri.parse(requireActivity().getPackageName()+".share://"+recipients[0]));
                        info.setIntent(i);
                        info.setActivity(new ComponentName(requireActivity().getPackageName(),
                                requireActivity().getPackageName()+".ui.HomeActivity"));
                        try {
                            ShortcutManagerCompat.pushDynamicShortcut(requireActivity(), info.build());
                        } catch (IllegalArgumentException e) {
                            // seems to happen instead of replacing a shortcut in api <30
                            // try to remove a shortcut to make room and try pushing the shortcut again
                            try {
                                List<ShortcutInfoCompat> l = ShortcutManagerCompat.getDynamicShortcuts(requireActivity());
                                if (l.size() > 0) {
                                    List<String> l2 = new LinkedList<>();
                                    l2.add(l.get(0).getId());
                                    ShortcutManagerCompat.removeDynamicShortcuts(requireActivity(), l2);
                                    ShortcutManagerCompat.pushDynamicShortcut(requireActivity(), info.build());
                                }
                            } catch (Exception ignored) {}
                        }
                         */
                        Toast.makeText(requireActivity(),R.string.message_send_successfully,Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t)
                {
                    Toast.makeText(requireActivity(),R.string.message_no_send,Toast.LENGTH_SHORT).show();
                }
            });
        });


        AlertDialog d = builder.create();
        // to prevent from accidentally losing a message you write by tapping outside
        d.setCanceledOnTouchOutside(false);
        return d;
    }
    
    public class AddresseeAdapter extends ArrayAdapter<StudipUser> {
        public AddresseeAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            TextView t;
            if (convertView instanceof TextView) {
                t = (TextView) convertView;
            } else {
                t = new TextView(requireActivity());
            }
            StudipUser u = getItem(position);
            t.setText(u.toString());
            return t;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
