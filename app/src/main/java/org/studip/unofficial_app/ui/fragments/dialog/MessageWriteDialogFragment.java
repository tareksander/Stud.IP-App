package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipSearchUser;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.databinding.DialogNewMessageBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.ui.HomeActivity;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageWriteDialogFragment extends DialogFragment
{
    private static final String ADDRESSEE_KEY = "addressee";
    private static final String ADDRESSEE_LIST_KEY = "addressee_list";
    private static final String ADDRESSEE_LIST_AUTOCOMPLETE_KEY = "addressee_list_autocomplete";
    private static final String SUBJECT_KEY = "subject";
    private static final String CONTENT_KEY = "content";
    

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
        showOnUpdate[0] = false;
        
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
            public void onResponse(Call<Void> call, Response<Void> response)
            {
                if (response.code() == 200) {
                    canSearch[0] = true;
                }
                HomeActivity.onStatusReturn(requireActivity(),response.code());
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t)
            {
                Toast.makeText(requireActivity(),R.string.message_no_search,Toast.LENGTH_SHORT).show();
            }
        });
        
        
        b.addresseeAdd.setOnClickListener((v) -> {
            if (! canSearch[0]) {
                APIProvider.getAPI(requireActivity()).dispatch.startMessage().enqueue(new Callback<Void>()
                {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response)
                    {
                        if (response.code() == 200) {
                            canSearch[0] = true;
                            // call again, now that search is possible
                            b.addresseeAdd.callOnClick();
                        }
                        HomeActivity.onStatusReturn(requireActivity(),response.code());
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t)
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
                    public void onResponse(Call<StudipSearchUser[]> call, Response<StudipSearchUser[]> response)
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
                                    public void onResponse(Call<StudipUser> call, Response<StudipUser> response)
                                    {
                                        StudipUser u = response.body();
                                        if (u != null) {
                                            Schedulers.io().scheduleDirect(() -> {
                                                db.userDao().updateInsert(u);
                                                if (count.incrementAndGet() == users.length) {
                                                    showOnUpdate[0] = false;
                                                    System.out.println("finished");
                                                }
                                            });
                                        } else {
                                            if (count.incrementAndGet() == users.length) {
                                                showOnUpdate[0] = false;
                                                System.out.println("finished");
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<StudipUser> call, Throwable t)
                                    {
                                        if (count.incrementAndGet() == users.length) {
                                            showOnUpdate[0] = false;
                                            System.out.println("finished");
                                        }
                                    }
                                });
                            }
                        }
                        HomeActivity.onStatusReturn(requireActivity(),response.code());
                    }
                    @Override
                    public void onFailure(Call<StudipSearchUser[]> call, Throwable t) {}
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
                public void onResponse(Call<Void> call, Response<Void> response)
                {
                    if (response.code() != 201)
                    {
                        Toast.makeText(requireActivity(),R.string.message_no_send,Toast.LENGTH_SHORT).show();
                        HomeActivity.onStatusReturn(requireActivity(),response.code());
                        //System.out.println(response.code());
                    } else {
                        Toast.makeText(requireActivity(),R.string.message_send_successfully,Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t)
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
