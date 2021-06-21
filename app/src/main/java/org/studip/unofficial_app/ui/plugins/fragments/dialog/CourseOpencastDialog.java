package org.studip.unofficial_app.ui.plugins.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.plugins.opencast.OpencastVideo;
import org.studip.unofficial_app.databinding.DialogOpencastBinding;
import org.studip.unofficial_app.databinding.DialogOpencastEntryBinding;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.model.viewmodels.OpencastViewModel;
import org.studip.unofficial_app.model.viewmodels.StringSavedStateViewModelFactory;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;

public class CourseOpencastDialog  extends DialogFragment
{
    public static final String COURSE_ID_KEY = "cid";
    private static final String LIST_KEY = "list";
    
    private OpencastViewModel m;
    private DialogOpencastBinding binding;
    
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        Bundle args = getArguments();
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return b.create();
        }
        m = new ViewModelProvider(this,new StringSavedStateViewModelFactory(this, null,
                requireActivity().getApplication(),args.getString(COURSE_ID_KEY))).get(OpencastViewModel.class);
        
        b.setTitle("Opencast");
    
        binding = DialogOpencastBinding.inflate(getLayoutInflater());
        b.setView(binding.getRoot());
        
        
        OpencastAdapter ad = new OpencastAdapter(requireActivity(),ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
        binding.opencastList.setAdapter(ad);
        
        m.isRefreshing().observe(this, binding.opencastRefresh::setRefreshing);
        binding.opencastRefresh.setOnRefreshListener(() -> m.refresh(requireActivity()));
        
        m.getVideos().observe(this, (videos) -> {
            if (videos != null) {
                ad.clear();
                ad.addAll(videos);
                if (savedInstanceState != null && savedInstanceState.containsKey(LIST_KEY)) {
                    binding.opencastList.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_KEY));
                    savedInstanceState.remove(LIST_KEY);
                }
            }
        });
        
        m.isError().observe(this, (error) -> {
            if (error) {
                dismiss();
            }
        });
        
        AlertDialog d = b.create();
        d.setCanceledOnTouchOutside(false);
        
        return d;
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_KEY, binding.opencastList.onSaveInstanceState());
    }
    
    public class OpencastAdapter extends ArrayAdapter<OpencastVideo> {
        public OpencastAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
    
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            DialogOpencastEntryBinding b;
            if (convertView != null) {
                b = DialogOpencastEntryBinding.bind(convertView);
            } else {
                b = DialogOpencastEntryBinding.inflate(getLayoutInflater(), parent, false);
            }
            b.opencastDownloads.removeAllViews();
            b.opencastView.removeAllViews();
            
            OpencastVideo v = getItem(position);
            
            b.opencastTitle.setText(v.title);
            b.opencastTitle.setTextIsSelectable(true);
            b.opencastDate.setText(v.date);
            b.opencastDate.setTextIsSelectable(true);
            b.opencastAuthor.setText(v.author);
            b.opencastAuthor.setTextIsSelectable(true);
            b.opencastDescription.setText(v.description);
            b.opencastDescription.setTextIsSelectable(true);
            
            Settings s = SettingsProvider.getSettings(requireActivity());
            ConnectivityManager con = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (s.load_images_on_mobile || ! con.isActiveNetworkMetered()) {
                if (!v.preview_url.equals("")) {
                    Picasso.get().load(v.preview_url).into(b.opencastPreview);
                }
            }
            
            
            for (OpencastVideo.VideoVersion q : v.versions) {
                Button view = new Button(requireActivity());
                Button download = new Button(requireActivity());
                
                view.setText(getString(R.string.view,q.resolution));
                download.setText(getString(R.string.download,q.resolution));
                
                view.setOnClickListener(v1 -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.parse(q.download),"video/*");
                    startActivity(Intent.createChooser(i,getString(R.string.view_with)));
                });
    
                download.setOnClickListener(v1 -> {
                    DownloadManager m = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(q.download));
                    r.setMimeType("video/*");
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, v.title);
                    r.setVisibleInDownloadsUi(true);
                    r.allowScanningByMediaScanner();
                    r.setTitle(v.title);
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    m.enqueue(r);
                });
                
                b.opencastDownloads.addView(download);
                b.opencastView.addView(view);
            }
            return b.getRoot();
        }
    }
    
    
    
    
}
