package org.studip.unofficial_app.ui.plugins.fragments.dialog;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareBlock;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareChapter;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareSection;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareSubchapter;
import org.studip.unofficial_app.api.plugins.courseware.blocks.CoursewareHTMLBlock;
import org.studip.unofficial_app.api.plugins.courseware.blocks.CoursewareOpencastBlock;
import org.studip.unofficial_app.api.plugins.courseware.blocks.CoursewarePDFBlock;
import org.studip.unofficial_app.api.plugins.opencast.OpencastQueryResult;
import org.studip.unofficial_app.api.plugins.opencast.OpencastVideo;
import org.studip.unofficial_app.databinding.DialogCoursewareBinding;
import org.studip.unofficial_app.databinding.DialogCoursewareChapterBinding;
import org.studip.unofficial_app.databinding.DialogOpencastCoursewareBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.CoursewareViewModel;
import org.studip.unofficial_app.model.viewmodels.StringSavedStateViewModelFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.studip.unofficial_app.api.plugins.opencast.OpencastQueryResult.SearchResults.Result.MediaPackage.Media.Track;

public class CoursewareDialog extends DialogFragment
{
    public static final String COURSE_ID_KEY = "cid";
    private CoursewareViewModel m;
    private DialogCoursewareBinding binding;
    
    private static final String LIST_KEY = "list";
    
    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("section_name",binding.coursewareSectionTitle.getText().toString());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCoursewareBinding.inflate(getLayoutInflater(), container, false);
        
        if (savedInstanceState != null) {
            binding.coursewareSectionTitle.setText(savedInstanceState.getString("section_name"));
        }
        
        Bundle args = getArguments();
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return binding.getRoot();
        }
        m = new ViewModelProvider(this,new StringSavedStateViewModelFactory(this, null, requireActivity().getApplication()
                ,args.getString(COURSE_ID_KEY))).get(CoursewareViewModel.class);
    
    
        
    
        binding.coursewareSections.addItemDecoration(new DividerItemDecoration(requireActivity(), RecyclerView.HORIZONTAL));
        binding.coursewareSections.addItemDecoration(new SpacingDecorator(true, 30));
    
        binding.coursewareChapters.addItemDecoration(new DividerItemDecoration(requireActivity(), RecyclerView.VERTICAL));
        binding.coursewareChapters.addItemDecoration(new SpacingDecorator(false, 10));
    
        binding.coursewareBlocks.addItemDecoration(new DividerItemDecoration(requireActivity(), RecyclerView.VERTICAL));
        binding.coursewareBlocks.addItemDecoration(new SpacingDecorator(false, 20));
        
        m.isRefreshing().observe(this, binding.coursewareRefresh::setRefreshing);
        binding.coursewareRefresh.setOnRefreshListener(() -> m.reload(requireActivity()));
        
        //System.out.println(m.getChapters().getValue());
    
        binding.coursewareChapters.setAdapter(new CoursewareChapterAdapter());
        binding.coursewareSections.setAdapter(new CoursewareSectionAdapter());
        
        CoursewareBlockAdapter bad = new CoursewareBlockAdapter();
        bad.setStateRestorationPolicy(Adapter.StateRestorationPolicy.PREVENT);
        binding.coursewareBlocks.setAdapter(bad);
        
    
        m.getChapters().observe(this, (chapters) -> {
            if (chapters != null) {
                if (m.selectedChapterData.getValue() != null) {
                    for (CoursewareChapter c : chapters) {
                        if (c.id.equals(m.selectedChapterData.getValue())) {
                            if (c.subchapters == null) {
                                m.refresh(requireActivity(), c.id, CoursewareViewModel.TYPE_CHAPTER);
                            } else {
                                if (m.selectedSectionData.getValue() == null) {
                                    for (CoursewareSubchapter sub : c.subchapters) {
                                        if (sub.id.equals(m.selectedSubchapterData.getValue())) {
                                            if (sub.sections == null) {
                                                m.refresh(requireActivity(), m.selectedSubchapterData.getValue(), CoursewareViewModel.TYPE_SUBCHAPTER);
                                            } else {
                                                if (sub.sections.length != 0) {
                                                    m.selectedSectionData.setValue(sub.sections[0].id);
                                                    binding.coursewareSectionTitle.setText(sub.sections[0].name);
                                                    if (sub.sections[0].blocks == null) {
                                                        m.refresh(requireActivity(), m.selectedSectionData.getValue(), CoursewareViewModel.TYPE_SECTION);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (c.subchapters.length != 0 && m.selectedSubchapterData.getValue() == null) {
                                    m.selectedSubchapterData.setValue(c.subchapters[0].id);
                                    if (c.subchapters[0].sections == null) {
                                        m.refresh(requireActivity(), m.selectedSubchapterData.getValue(), CoursewareViewModel.TYPE_SUBCHAPTER);
                                    }
                                }
                            }
                        }
                    }
                }
                if (m.selectedChapterData.getValue() == null && chapters.length != 0) {
                    m.selectedChapterData.setValue(chapters[0].id);
                    m.refresh(requireActivity(), m.selectedChapterData.getValue(), CoursewareViewModel.TYPE_CHAPTER);
                }
            }
            binding.coursewareChapters.getAdapter().notifyDataSetChanged();
            binding.coursewareSections.getAdapter().notifyDataSetChanged();
            binding.coursewareBlocks.getAdapter().notifyDataSetChanged();
        });
    
        
    
    
        m.isError().observe(this, (error) -> {
            if (error) {
                dismiss();
            }
        });
        
        return binding.getRoot();
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return d;
        }
        return d;
    }
    
    
    public class ChapterHolder extends RecyclerView.ViewHolder {
        public ChapterHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }
    public class CoursewareChapterAdapter extends RecyclerView.Adapter<ChapterHolder> {
        @NonNull
        @NotNull
        @Override
        public ChapterHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            return new ChapterHolder(getLayoutInflater().inflate(R.layout.dialog_courseware_chapter, null, false));
        }
    
        @Override
        public void onBindViewHolder(@NonNull @NotNull ChapterHolder holder, int position) {
            DialogCoursewareChapterBinding b = DialogCoursewareChapterBinding.bind(holder.itemView);
            
            b.chapterSubchapters.removeAllViews();
            b.chapterTitle.setOnClickListener(null);
            
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null && position < chapters.length) {
                b.chapterTitle.setText(chapters[position].name);
                CoursewareSubchapter[] sub = chapters[position].subchapters;
                if (sub != null) {
                    for (CoursewareSubchapter s : sub) {
                        TextView t = new TextView(requireActivity());
                        t.setPadding(0,0,0, (int) (8*getResources().getDisplayMetrics().density));
                        t.setText(s.name);
                        t.setOnClickListener(v1 -> {
                            m.selectedChapterData.setValue(chapters[position].id);
                            m.selectedSubchapterData.setValue(s.id);
                            m.selectedSectionData.setValue(null);
                            if (s.sections == null) {
                                m.refresh(requireActivity(), s.id, CoursewareViewModel.TYPE_SUBCHAPTER);
                            } else {
                                if (s.sections.length != 0) {
                                    m.selectedSectionData.setValue(s.sections[0].id);
                                    binding.coursewareSectionTitle.setText(s.sections[0].name);
                                }
                            }
                            binding.coursewareChapters.getAdapter().notifyDataSetChanged();
                            binding.coursewareSections.getAdapter().notifyDataSetChanged();
                            binding.coursewareBlocks.getAdapter().notifyDataSetChanged();
                        });
                        b.chapterSubchapters.addView(t);
                    }
                }
                b.chapterTitle.setOnClickListener(v1 -> {
                    m.selectedChapterData.setValue(chapters[position].id);
                    m.selectedSubchapterData.setValue(null);
                    m.selectedSectionData.setValue(null);
                    if (chapters[position].subchapters == null) {
                        m.refresh(requireActivity(), m.selectedChapterData.getValue(), CoursewareViewModel.TYPE_CHAPTER);
                    } else {
                        if (chapters[position].subchapters.length != 0) {
                            m.selectedSubchapterData.setValue(chapters[position].subchapters[0].id);
                            if (chapters[position].subchapters[0].sections == null) {
                                m.refresh(requireActivity(), m.selectedSubchapterData.getValue(), CoursewareViewModel.TYPE_SUBCHAPTER);
                            } else {
                                if (chapters[position].subchapters[0].sections.length != 0) {
                                    m.selectedSectionData.setValue(chapters[position].subchapters[0].sections[0].id);
                                    binding.coursewareSectionTitle.setText(chapters[position].subchapters[0].sections[0].name);
                                }
                            }
                        }
                    }
                    binding.coursewareChapters.getAdapter().notifyDataSetChanged();
                    binding.coursewareSections.getAdapter().notifyDataSetChanged();
                    binding.coursewareBlocks.getAdapter().notifyDataSetChanged();
                });
            }
            
        }
    
        @Override
        public int getItemCount() {
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null) {
                return chapters.length;
            } else {
                return 0;
            }
        }
    }
    
    public class SectionHolder extends RecyclerView.ViewHolder {
        public SectionHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }
    public class CoursewareSectionAdapter extends RecyclerView.Adapter<SectionHolder> {
        @NonNull
        @NotNull
        @Override
        public SectionHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            ImageView v = new ImageView(requireActivity());
            v.setImageResource(R.drawable.info_blue);
            return new SectionHolder(v);
        }
        
        @Override
        public void onBindViewHolder(@NonNull @NotNull SectionHolder holder, int position) {
            ImageView v = (ImageView) holder.itemView;
            v.setOnClickListener(null);
            CoursewareChapter[] chapters = m.getChapters().getValue();
            boolean found = false;
            if (chapters != null && m.selectedChapterData.getValue() != null && m.selectedSubchapterData.getValue() != null) {
                for (CoursewareChapter c : chapters) {
                    if (c.id.equals(m.selectedChapterData.getValue()) && c.subchapters != null) {
                        for (CoursewareSubchapter s : c.subchapters) {
                            if (s.id.equals(m.selectedSubchapterData.getValue())) {
                                found = true;
                                if (s.sections != null && position < s.sections.length) {
                                    v.setOnClickListener(v1 -> {
                                        m.selectedSectionData.setValue(s.sections[position].id);
                                        binding.coursewareSectionTitle.setText(s.sections[position].name);
                                        if (s.sections[position].blocks == null) {
                                            m.refresh(requireActivity(), m.selectedSectionData.getValue(), CoursewareViewModel.TYPE_SECTION);
                                        }
                                        binding.coursewareBlocks.getAdapter().notifyDataSetChanged();
                                    });
                                }
                                break;
                            }
                        }
                    }
                    if (found) break;
                }
            }
        }
        
        @Override
        public int getItemCount() {
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null && m.selectedChapterData.getValue() != null && m.selectedSubchapterData.getValue() != null) {
                for (CoursewareChapter c : chapters) {
                    if (c.id.equals(m.selectedChapterData.getValue()) && c.subchapters != null) {
                        for (CoursewareSubchapter s : c.subchapters) {
                            if (s.id.equals(m.selectedSubchapterData.getValue())) {
                                if (s.sections == null) {
                                    return 0;
                                }
                                return s.sections.length;
                            }
                        }
                    }
                }
            }
            return 0;
        }
    }
    
    public class BlockHolder extends RecyclerView.ViewHolder {
        public BlockHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }
    public class CoursewareBlockAdapter extends RecyclerView.Adapter<BlockHolder> {
        @NonNull
        @NotNull
        @Override
        public BlockHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            return new BlockHolder(new FrameLayout(requireActivity()));
        }
        
        @Override
        public void onBindViewHolder(@NonNull @NotNull BlockHolder holder, int position) {
            FrameLayout f = (FrameLayout) holder.itemView;
            f.removeAllViews();
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null && m.selectedChapterData.getValue() != null && m.selectedSubchapterData.getValue() != null && m.selectedSectionData.getValue() != null) {
                for (CoursewareChapter c : chapters) {
                    if (c.id.equals(m.selectedChapterData.getValue()) && c.subchapters != null) {
                        for (CoursewareSubchapter s : c.subchapters) {
                            if (s.id.equals(m.selectedSubchapterData.getValue()) && s.sections != null) {
                                for (CoursewareSection sect : s.sections) {
                                    if (sect.id.equals(m.selectedSectionData.getValue())) {
                                        if (sect.blocks != null && position < sect.blocks.length) {
                                            CoursewareBlock b = sect.blocks[position];
                                            if (b instanceof CoursewareHTMLBlock) {
                                                CoursewareHTMLBlock html = (CoursewareHTMLBlock) b;
                                                WebView web = createWebView(html);
                                                web.setWebViewClient(new CoursewareWebViewClient(html, f));
                                                f.addView(web);
                                                setStateRestorationPolicy(StateRestorationPolicy.ALLOW);
                                                return;
                                            }
                                            if (b instanceof CoursewareOpencastBlock) {
                                                CoursewareOpencastBlock opencast = (CoursewareOpencastBlock) b;
                                                DialogOpencastCoursewareBinding entry = DialogOpencastCoursewareBinding.inflate(getLayoutInflater(),
                                                        f, true);
                                                //System.out.println(opencast.hostname+"  "+opencast.id);
                                                opencastBlock(opencast, entry);
                                                setStateRestorationPolicy(StateRestorationPolicy.ALLOW);
                                                return;
                                            }
                                            if (b instanceof CoursewarePDFBlock) {
                                                CoursewarePDFBlock pdf = (CoursewarePDFBlock) b;
                                                TextView v = new TextView(requireActivity());
                                                v.setText(getString(R.string.download, pdf.name));
                                                v.setOnClickListener(v1 -> {
                                                    API api = APIProvider.getAPI(requireActivity());
                                                    if (api == null) {
                                                        return;
                                                    }
                                                    api.downloadFile(requireActivity(), pdf.url, pdf.name, true);
                                                });
                                                v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                                                f.addView(v);
                                                setStateRestorationPolicy(StateRestorationPolicy.ALLOW);
                                                return;
                                            }
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        private void clearHolderWebView(@NonNull BlockHolder holder) {
            FrameLayout f = (FrameLayout) holder.itemView;
            View child = f.getChildAt(0);
            if (child instanceof WebView) {
                f.removeAllViews();
                //System.out.println("WebView destroyed while recycling");
                ((WebView) child).destroy();
            }
        }
        
        @Override
        public boolean onFailedToRecycleView(@NonNull BlockHolder holder) {
            clearHolderWebView(holder);
            return false;
        }
    
        @Override
        public void onViewRecycled(@NonNull BlockHolder holder) {
            clearHolderWebView(holder);
            super.onViewRecycled(holder);
        }
        
        public WebView createWebView(CoursewareHTMLBlock html) {
            // TODO using the application context can cause crashes in some circumstances, but prevents a memory leak
            WebView web = new WebView(requireActivity().getApplicationContext());
            web.setBackgroundColor(Color.TRANSPARENT);
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    WebSettingsCompat.setForceDark(web.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                        WebSettingsCompat.setForceDarkStrategy(web.getSettings(), WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
                    }
                } else {
                    WebSettingsCompat.setForceDark(web.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                }
            } else {
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    web.setBackgroundColor(Color.WHITE);
                }
                //System.out.println("dark mode not supported for WebView");
            }
            web.loadData(Base64.encodeToString(html.content.getBytes(), Base64.NO_PADDING), "text/html; charset=utf-8","base64");
            web.scrollTo(html.scrollx, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                web.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    html.scrollx = scrollX;
                });
            }
            return web;
        }
        
        public class CoursewareWebViewClient extends WebViewClient {
            private final CoursewareHTMLBlock b;
            private final FrameLayout f;
            public CoursewareWebViewClient(CoursewareHTMLBlock b, FrameLayout f) {
                this.b = b;
                this.f = f;
            }
            private void handleURL(String url) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException ignored) {}
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                handleURL(request.getUrl().toString());
                return true;
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                handleURL(url);
                return true;
            }
            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                f.removeAllViews();
                WebView newweb = createWebView(b);
                view.setWebViewClient(null);
                newweb.setWebViewClient(this);
                f.addView(newweb);
                return true;
            }
        }
        
        
        @Override
        public int getItemCount() {
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null && m.selectedChapterData.getValue() != null && m.selectedSubchapterData.getValue() != null && m.selectedSectionData.getValue() != null) {
                for (CoursewareChapter c : chapters) {
                    if (c.id.equals(m.selectedChapterData.getValue()) && c.subchapters != null) {
                        for (CoursewareSubchapter s : c.subchapters) {
                            if (s.id.equals(m.selectedSubchapterData.getValue()) && s.sections != null) {
                                for (CoursewareSection sect : s.sections) {
                                    if (sect.id.equals(m.selectedSectionData.getValue())) {
                                        if (sect.blocks == null) {
                                            return 0;
                                        }
                                        return sect.blocks.length;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return 0;
        }
        
        private void opencastBlock(CoursewareOpencastBlock opencast, DialogOpencastCoursewareBinding entry) {
            if (opencast.video == null) {
                APIProvider.getAPI(requireActivity()).opencast.lti(opencast.hostname, opencast.ltidata).enqueue(new Callback<Void>()
                {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        String cookie = response.headers().get("Set-Cookie");
                        if (cookie == null) {
                            return;
                        }
                        APIProvider.getAPI(requireActivity()).opencast.queryVideo(opencast.hostname, opencast.id, cookie)
                            .enqueue(new Callback<OpencastQueryResult>()
                            {
                                @Override
                                public void onResponse(Call<OpencastQueryResult> call, Response<OpencastQueryResult> response) {
                                    OpencastQueryResult q = response.body();
                                    FragmentActivity a = getActivity();
                                    if (q != null && a != null) {
                                        //System.out.println(GsonProvider.getGson().toJson(q));
                                        try {
                                            String title = q.search_results.result.mediapackage.title;
                                            //System.out.println(title);
                                            opencast.video = new OpencastVideo();
                                            opencast.video.title = title;
                                            opencast.video.versions = new OpencastVideo.VideoVersion[q.search_results.result.mediapackage.media.track.length];
                                            entry.opencastTitle.setText(title);
                                            int index = 0;
                                            for (Track t : q.search_results.result.mediapackage.media.track) {
                                                opencast.video.versions[index] = new OpencastVideo.VideoVersion();
                                                opencast.video.versions[index].download = t.url;
                                                opencast.video.versions[index].resolution = t.video.resolution;
                                                Button download = new Button(a);
                                                //System.out.println(t.video.resolution);
                                                download.setText(getString(R.string.download, t.video.resolution));
                                                download.setOnClickListener(v1 -> {
                                                    DownloadManager m = (DownloadManager) requireActivity().
                                                            getSystemService(Context.DOWNLOAD_SERVICE);
                                                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(t.url));
                                                    r.setMimeType(t.mimetype);
                                                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
                                                    r.setVisibleInDownloadsUi(true);
                                                    r.allowScanningByMediaScanner();
                                                    r.setTitle(title);
                                                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                    m.enqueue(r);
                                                });
                                                entry.opencastDownloads.addView(download);
                                                Button view = new Button(a);
                                                view.setText(getString(R.string.view, t.video.resolution));
                                                view.setOnClickListener(v1 -> {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setDataAndType(Uri.parse(t.url), "video/*");
                                                    startActivity(Intent.createChooser(i, getString(R.string.view_with)));
                                                });
                                                entry.opencastView.addView(view);
                                                index++;
                                            }
                                        }
                                        catch (NullPointerException ignored) {
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<OpencastQueryResult> call, Throwable t) {
                                }
                            });
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }  else  {
                entry.opencastTitle.setText(opencast.video.title);
                for (OpencastVideo.VideoVersion v : opencast.video.versions) {
                    Button download = new Button(requireActivity());
                    //System.out.println(t.video.resolution);
                    download.setText(getString(R.string.download, v.resolution));
                    download.setOnClickListener(v1 -> {
                        DownloadManager m = (DownloadManager) requireActivity().
                                getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(v.download));
                        r.setMimeType("video/*");
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, opencast.video.title);
                        r.setVisibleInDownloadsUi(true);
                        r.allowScanningByMediaScanner();
                        r.setTitle(opencast.video.title);
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        m.enqueue(r);
                    });
                    entry.opencastDownloads.addView(download);
                    Button view = new Button(requireActivity());
                    view.setText(getString(R.string.view, v.resolution));
                    view.setOnClickListener(v1 -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.parse(v.download), "video/*");
                        startActivity(Intent.createChooser(i, getString(R.string.view_with)));
                    });
                    entry.opencastView.addView(view);
                }
            }
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        int children = binding.coursewareBlocks.getChildCount();
        WebView[] views = new WebView[children];
        for (int i = 0;i<children;i++) {
            View v = binding.coursewareBlocks.getChildAt(i);
            if (v instanceof FrameLayout && ((FrameLayout)v).getChildAt(0) instanceof WebView) {
                views[i] = (WebView) ((FrameLayout)v).getChildAt(0);
            }
        }
        binding.coursewareBlocks.removeAllViews();
        for (int i = 0;i<children;i++) {
            if (views[i] != null) {
                if (i == children-1) {
                    views[i].clearCache(false);
                }
                //System.out.println("destroying WebView");
                views[i].destroy();
            }
        }
        //System.out.println("destroyed");
    }
    
    private static class SpacingDecorator extends RecyclerView.ItemDecoration {
        boolean horizontal;
        int margin;
        public SpacingDecorator(boolean horizontal, int margin) {
            this.horizontal = horizontal;
            this.margin = margin;
        }
    
        @SuppressWarnings("rawtypes")
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            Adapter ad = parent.getAdapter();
            if (ad != null && parent.getChildAdapterPosition(view) != ad.getItemCount()) {
                if (horizontal) {
                    outRect.right = margin;
                } else {
                    outRect.bottom = margin;
                }
            }
        }
    }
    
    
    
    
    
}
