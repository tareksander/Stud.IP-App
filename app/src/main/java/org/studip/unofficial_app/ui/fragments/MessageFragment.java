package org.studip.unofficial_app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.Features;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.databinding.FragmentMessagesBinding;
import org.studip.unofficial_app.databinding.MessageEntryBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.GetMessageUsersWork;
import org.studip.unofficial_app.model.viewmodels.MessagesViewModel;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.fragments.dialog.MessageDialogFragment;
import org.studip.unofficial_app.ui.fragments.dialog.MessageWriteDialogFragment;

import java.util.Calendar;
import java.util.Objects;

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
        
        //System.out.println("messages fragment");
        
        setSwipeRefreshLayout(binding.messagesRefresh);
    
        API api = APIProvider.getAPI(requireActivity());
        if (api != null && api.isFeatureEnabled(Features.FEATURE_MESSAGES)) {
            m.mes.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(), status));
            m.mes.isRefreshing().observe(getViewLifecycleOwner(), ref -> binding.messagesRefresh.setRefreshing(ref));
    
            //ad = new MessageAdapter(requireActivity(),ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
            ad = new MessageAdapter();
            binding.messagesList.setAdapter(ad);
    
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
            binding.messagesList.addItemDecoration(dividerItemDecoration);
    
            m.mes.get().observe(getViewLifecycleOwner(), (messages) -> {
                //System.out.println("messages");
                if (messages.length == 0 && m.mes.getStatus().getValue() == -1) {
                    //System.out.println("refreshing");
                    binding.messagesRefresh.setRefreshing(true);
                    m.mes.refresh(requireActivity());
                }
        
                binding.messagesList.setAdapter(ad);
            });
    
    
            PagedList.Config conf = new PagedList.Config.Builder().setEnablePlaceholders(true).setPageSize(10).build();
    
            new LivePagedListBuilder<>(DBProvider.getDB(requireActivity()).messagesDao().getPagedList(), conf).build().observe(getViewLifecycleOwner(), (l) -> ad.submitList(l));
    
    
            binding.messageWrite.setOnClickListener((v) -> new MessageWriteDialogFragment().show(getParentFragmentManager(), "message_write"));
    
    
            binding.messagesRefresh.setOnRefreshListener(() -> m.mes.refresh(requireActivity()));
        } else {
            binding.messagesRefresh.setOnRefreshListener(() -> binding.messagesRefresh.setRefreshing(false));
        }
        
        
        return binding.getRoot();
    }
    
    
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
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
            //System.out.println("create");
            return new MessageViewHolder(getLayoutInflater().inflate(R.layout.message_entry, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
        {
            //System.out.println("bind");
            MessageEntryBinding b;
            b = MessageEntryBinding.bind(holder.itemView);
            StudipMessage m = getItem(position);
            if (m == null) {
                b.getRoot().setOnClickListener(null);
                b.getRoot().setOnLongClickListener(null);
                b.messageSubject.setText("");
                b.messageSender.setText("");
                b.messageTime.setText("");
                return;
            }
            View layout = b.getRoot();
            // TODO onClick and onLongClick for layout

            layout.setOnClickListener((v) -> {
                MessageDialogFragment d = new MessageDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable(MessageDialogFragment.ARG_MESSAGE_ID,m);
                d.setArguments(args);
                d.show(getParentFragmentManager(),"message_view_dialog");
            });

            layout.setOnLongClickListener(v -> {
                binding.messagesRefresh.setRefreshing(true);
                APIProvider.getAPI(requireActivity()).message.delete(m.message_id).enqueue(new Callback<Void>()
                {
                    @Override
                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                        binding.messagesRefresh.setRefreshing(false);
                        if (response.code() == 204 || response.code() == 404) {
                            DBProvider.getDB(requireActivity()).messagesDao().deleteAsync(m).subscribeOn(Schedulers.io()).subscribe();
                        } else {
                            HomeActivity.onStatusReturn(requireActivity(),response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                        binding.messagesRefresh.setRefreshing(false);
                    }
                });
                return true;
            });
    
            Transformations.distinctUntilChanged(DBProvider.getDB(requireActivity()).userDao().observe(m.sender)).observe(getViewLifecycleOwner(),(user) -> {
                if (user != null) {
                    if (! b.messageSender.getText().toString().equals(user.name.formatted)) {
                        b.messageSender.setText(user.name.formatted);
                    }
                } else {
                    WorkManager.getInstance(requireActivity()).enqueueUniqueWork(GetMessageUsersWork.WORK_NAME, ExistingWorkPolicy.KEEP, OneTimeWorkRequest.from(GetMessageUsersWork.class));
                }
            });

            b.messageSubject.setText(m.subject);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(1000*Long.parseLong(m.mkdate));

            b.messageTime.setText(c.get(Calendar.DATE)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));


            
        }
    }
    
    
    
}
