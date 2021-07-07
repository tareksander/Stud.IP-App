package org.studip.unofficial_app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingDataAdapter;
import androidx.paging.PagingLiveData;
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
import org.studip.unofficial_app.model.room.DB;
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
            m.mes.isRefreshing().observe(getViewLifecycleOwner(), ref -> {
                binding.messagesRefresh.setRefreshing(ref);
                String open = m.open.getValue();
                if (! ref && open != null) {
                    LiveData<StudipMessage> d = DBProvider.getDB(requireActivity()).messagesDao().observe(open);
                    d.observe(getViewLifecycleOwner(), m -> {
                        d.removeObservers(getViewLifecycleOwner());
                        if (m != null) {
                            viewMessage(m);
                        }
                    });
                    m.open.setValue(null);
                }
            });
    
            //ad = new MessageAdapter(requireActivity(),ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
            ad = new MessageAdapter();
            ad.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
            binding.messagesList.setAdapter(ad);
    
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
            binding.messagesList.addItemDecoration(dividerItemDecoration);
    
    
            m.mes.get().observe(getViewLifecycleOwner(), (messages) -> {
                //System.out.println("messages");
                if (messages.length == 0 && m.mes.getStatus().getValue() != null && m.mes.getStatus().getValue() == -1) {
                    //System.out.println("refreshing");
                    binding.messagesRefresh.setRefreshing(true);
                    m.mes.refresh(requireActivity());
                }
                binding.messagesList.setAdapter(ad);
            });
            
            m.source.observe(getViewLifecycleOwner(), source -> {
                if (source != null) {
                    Bundle args = new Bundle();
                    args.putString(MessageWriteDialogFragment.SUBJECT_KEY, source.getStringExtra(Intent.EXTRA_SUBJECT));
                    args.putString(MessageWriteDialogFragment.CONTENT_KEY, source.getStringExtra(Intent.EXTRA_TEXT));
                    String shortcut = source.getStringExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID);
                    if (shortcut != null) {
                        String[] split = shortcut.split(":");
                        if (split.length == 2) {
                            String uid = split[1];
                            args.putString(MessageWriteDialogFragment.ADDRESSEE_KEY, uid);
                        }
                    }
                    MessageWriteDialogFragment d = new MessageWriteDialogFragment();
                    d.setArguments(args);
                    d.show(getParentFragmentManager(), "message_write");
                    m.source.setValue(null);
                }
            });
            
            
            PagingConfig conf = new PagingConfig(10, 10, true);
    
    
            DB db = DBProvider.getDB(requireActivity());
            PagingLiveData.cachedIn(PagingLiveData.getLiveData(new Pager<>(conf, null,
                            () -> db.messagesDao().getPagedList())),
                    ViewModelKt.getViewModelScope(m)).observe(getViewLifecycleOwner(), (d) -> ad.submitData(getLifecycle(), d));
            
    
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
    
    public class MessageAdapter extends PagingDataAdapter<StudipMessage,MessageViewHolder>
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
            if (m.unread) {
                b.messageSubject.setTextColor(0xffff0000);
            } else {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    b.messageSubject.setTextColor(0xffcccccc);
                } else {
                    b.messageSubject.setTextColor(0xff000000);
                }
            }
            
            View layout = b.getRoot();

            layout.setOnClickListener((v) -> viewMessage(m));

            layout.setOnLongClickListener(v -> {
                binding.messagesRefresh.setRefreshing(true);
                API api = APIProvider.getAPI(requireActivity());
                if (api != null) {
                    api.message.delete(m.message_id).enqueue(new Callback<Void>()
                    {
                        @Override
                        public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                            binding.messagesRefresh.setRefreshing(false);
                            if (response.code() == 204 || response.code() == 404) {
                                DBProvider.getDB(requireActivity()).messagesDao().deleteAsync(m).subscribeOn(Schedulers.io()).subscribe();
                            }
                            else {
                                HomeActivity.onStatusReturn(requireActivity(), response.code());
                            }
                        }
        
                        @Override
                        public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                            binding.messagesRefresh.setRefreshing(false);
                        }
                    });
                }
                return true;
            });
    
            Transformations.distinctUntilChanged(DBProvider.getDB(requireActivity()).userDao().observe(m.sender)).observe(getViewLifecycleOwner(),(user) -> {
                if (user != null) {
                    if (! b.messageSender.getText().toString().equals(user.name.formatted)) {
                        b.messageSender.setText(user.name.formatted);
                    }
                } else {
                    WorkManager.getInstance(requireActivity()).enqueueUniqueWork(GetMessageUsersWork.WORK_NAME, ExistingWorkPolicy.KEEP,
                            OneTimeWorkRequest.from(GetMessageUsersWork.class));
                }
            });

            b.messageSubject.setText(m.subject);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(1000*Long.parseLong(m.mkdate));

            b.messageTime.setText(getString(R.string.message_time_template, c.get(Calendar.DATE), (c.get(Calendar.MONTH)+1), c.get(Calendar.YEAR),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));


            
        }
    }
    
    public void viewMessage(StudipMessage m) {
        MessageDialogFragment d = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(MessageDialogFragment.ARG_MESSAGE_ID,m);
        d.setArguments(args);
        d.show(getParentFragmentManager(),"message_view_dialog");
    }
    
    
}
