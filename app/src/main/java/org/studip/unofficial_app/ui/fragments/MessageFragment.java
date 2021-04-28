package org.studip.unofficial_app.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.databinding.FragmentMessagesBinding;
import org.studip.unofficial_app.databinding.MessageEntryBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.viewmodels.MessagesViewModel;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.fragments.dialog.MessageDialogFragment;

import java.util.Calendar;
import java.util.Objects;

import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends SwipeRefreshFragment
{

    private MessageAdapter ad;
    private MessagesViewModel m;
    private FragmentMessagesBinding binding;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentMessagesBinding.inflate(inflater);
        m = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        
        setSwipeRefreshLayout(binding.messagesRefresh);
        
        m.mes.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(),status));
        m.mes.isRefreshing().observe(getViewLifecycleOwner(), ref -> binding.messagesRefresh.setRefreshing(ref));
        
        ad = new MessageAdapter(requireActivity(),ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
        binding.messagesList.setAdapter(ad);
        
        m.mes.get().observe(getViewLifecycleOwner(), (messages) -> {
            System.out.println("messages");
            if (messages.length == 0 && m.mes.getStatus().getValue() == -1) {
                //System.out.println("refreshing");
                binding.messagesRefresh.setRefreshing(true);
                m.mes.refresh(requireActivity());
                return;
            }
            ad.clear();
            ad.addAll(messages);
            //System.out.println(messages.length);
            binding.messagesList.setAdapter(ad); // when the fragment is first shown, the data will not be visible without this
            
        });
        
        /*
        PagedList.Config conf = new PagedList.Config.Builder().setEnablePlaceholders(false).setPageSize(10).build();
        
        new LivePagedListBuilder<>(DBProvider.getDB(requireActivity()).messagesDao().getPagedList(), conf).build().observe(getViewLifecycleOwner(), (l) -> {
            System.out.println("list");
            System.out.println(l.size());
            for (StudipMessage m : l.snapshot()) {
                System.out.println(m.subject);
            }
            ad.submitList(l);
        });
        */
        
        
        binding.messagesRefresh.setOnRefreshListener(() -> m.mes.refresh(requireActivity()));

        
        
        return binding.getRoot();
    }


    public class MessageAdapter extends ArrayAdapter<StudipMessage> {

        public MessageAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            MessageEntryBinding b;
            if (convertView != null) {
                b = MessageEntryBinding.bind(convertView);
            } else {
                b = MessageEntryBinding.inflate(getLayoutInflater());
            }
            StudipMessage m = getItem(position);
            View layout = b.getRoot();
            // TODO onClick and onLongClick for layout

            layout.setOnClickListener((v) -> {
                MessageDialogFragment d = new MessageDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable(MessageDialogFragment.ARG_MESSAGE_ID,m);
                d.setArguments(args);
                d.show(getParentFragmentManager(),"message_view_dialog");
            });



            DBProvider.getDB(requireActivity()).userDao().observe(m.sender).observe(getViewLifecycleOwner(),(user) -> {
                if (user == null) {
                    APIProvider.getAPI(requireActivity()).user.user(m.sender).enqueue(new Callback<StudipUser>()
                    {
                        @Override
                        public void onResponse(Call<StudipUser> call, Response<StudipUser> response)
                        {
                            StudipUser u = response.body();
                            if (u != null) {
                                //System.out.println(u.name.formatted);
                                Schedulers.io().scheduleDirect(() -> DBProvider.getDB(requireActivity()).userDao().updateInsert(u));
                            }
                        }
                        @Override
                        public void onFailure(Call<StudipUser> call, Throwable t)
                        {}
                    });
                } else {
                    b.messageSender.setText(user.name.formatted);
                }
            });

            b.messageSubject.setText(m.subject);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(1000*Long.parseLong(m.mkdate));

            b.messageTime.setText(c.get(Calendar.DATE)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
            return b.getRoot();
        }
    }
    
    
    
    /*
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }

    public static final DiffUtil.ItemCallback<StudipMessage> DIFFCALLBACK = new DiffUtil.ItemCallback<StudipMessage>()
    {
        @Override
        public boolean areItemsTheSame(@NonNull StudipMessage oldItem, @NonNull StudipMessage newItem)
        {
            return Objects.equals(oldItem.message_id,newItem.message_id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull StudipMessage oldItem, @NonNull StudipMessage newItem)
        {
            return Objects.equals(oldItem,newItem);
        }
    };
    
    public class MessageAdapter extends PagedListAdapter<StudipMessage,MessageViewHolder>
    {
        protected MessageAdapter()
        {
            super(DIFFCALLBACK);
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            System.out.println("create");
            MessageEntryBinding b;
            b = MessageEntryBinding.inflate(getLayoutInflater());
            return new MessageViewHolder(b.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
        {
            System.out.println("bind");
            MessageEntryBinding b;
            b = MessageEntryBinding.bind(holder.itemView);
            StudipMessage m = getItem(position);
            View layout = b.getRoot();
            // TODO onClick and onLongClick for layout

            layout.setOnClickListener((v) -> {
                MessageDialogFragment d = new MessageDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable(MessageDialogFragment.ARG_MESSAGE_ID,m);
                d.setArguments(args);
                d.show(getParentFragmentManager(),"message_view_dialog");
            });



            DBProvider.getDB(requireActivity()).userDao().observe(m.sender).observe(getViewLifecycleOwner(),(user) -> {
                if (user == null) {
                    APIProvider.getAPI(requireActivity()).user.user(m.sender).enqueue(new Callback<StudipUser>()
                    {
                        @Override
                        public void onResponse(Call<StudipUser> call, Response<StudipUser> response)
                        {
                            StudipUser u = response.body();
                            if (u != null) {
                                //System.out.println(u.name.formatted);
                                Schedulers.io().scheduleDirect(() -> DBProvider.getDB(requireActivity()).userDao().updateInsert(u));
                            }
                        }
                        @Override
                        public void onFailure(Call<StudipUser> call, Throwable t)
                        {}
                    });
                } else {
                    b.messageSender.setText(user.name.formatted);
                }
            });

            b.messageSubject.setText(m.subject);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(1000*Long.parseLong(m.mkdate));

            b.messageTime.setText(c.get(Calendar.DATE)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));


            
        }
    }
    */
    
    
}
