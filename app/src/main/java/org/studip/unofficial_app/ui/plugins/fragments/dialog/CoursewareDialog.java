package org.studip.unofficial_app.ui.plugins.fragments.dialog;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareChapter;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareSubchapter;
import org.studip.unofficial_app.databinding.DialogCoursewareBinding;
import org.studip.unofficial_app.databinding.DialogCoursewareChapterBinding;
import org.studip.unofficial_app.model.viewmodels.CoursewareViewModel;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;

public class CoursewareDialog extends DialogFragment
{
    public static final String COURSE_ID_KEY = "cid";
    private CoursewareViewModel m;
    
    
    @Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogCoursewareBinding binding = DialogCoursewareBinding.inflate(getLayoutInflater(), container, false);
        
        Bundle args = getArguments();
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return binding.getRoot();
        }
        m = new ViewModelProvider(this,new StringViewModelFactory(requireActivity().getApplication(),args.getString(COURSE_ID_KEY))).get(CoursewareViewModel.class);
    
    
        binding.coursewareSections.addItemDecoration(new DividerItemDecoration(requireActivity(), RecyclerView.HORIZONTAL));
        binding.coursewareSections.addItemDecoration(new SpacingDecorator(true, 30));
    
        binding.coursewareChapters.addItemDecoration(new DividerItemDecoration(requireActivity(), RecyclerView.VERTICAL));
        binding.coursewareChapters.addItemDecoration(new SpacingDecorator(false, 10));
    
        binding.coursewareBlocks.addItemDecoration(new DividerItemDecoration(requireActivity(), RecyclerView.VERTICAL));
        binding.coursewareBlocks.addItemDecoration(new SpacingDecorator(false, 20));
        
        m.isRefreshing().observe(this, binding.coursewareRefresh::setRefreshing);
        binding.coursewareRefresh.setOnRefreshListener(() -> m.reload(requireActivity()));
    
    
        binding.coursewareChapters.setAdapter(new CoursewareChapterAdapter());
        binding.coursewareSections.setAdapter(new CoursewareSectionAdapter());
        binding.coursewareBlocks.setAdapter(new CoursewareBlockAdapter());
    
    
        m.getChapters().observe(this, (chapters) -> {
            if (chapters != null && chapters.length != 0) {
                if (m.selectedChapter == null) {
                    m.selectedChapter = chapters[0].id;
                    m.refresh(requireActivity(),chapters[0].id, CoursewareViewModel.TYPE_CHAPTER);
                }
                if (chapters[0].subchapters != null) {
                    if (chapters[0].subchapters.length != 0) {
                        if (m.selectedSubchapter == null) {
                            m.selectedSubchapter = chapters[0].subchapters[0].id;
                            m.refresh(requireActivity(), chapters[0].subchapters[0].id, CoursewareViewModel.TYPE_SUBCHAPTER);
                        }
                        if (chapters[0].subchapters[0].sections != null) {
                            if (chapters[0].subchapters[0].sections.length != 0) {
                                if (m.selectedSection == null) {
                                    m.selectedSection = chapters[0].subchapters[0].sections[0].id;
                                    m.refresh(requireActivity(), chapters[0].subchapters[0].sections[0].id, CoursewareViewModel.TYPE_SECTION);
                                }
                            }
                        }
                    }
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
            
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null && position < chapters.length) {
                b.chapterTitle.setText(chapters[position].name);
                CoursewareSubchapter[] sub = chapters[position].subchapters;
                if (sub != null) {
                    for (CoursewareSubchapter s : sub) {
                        TextView t = new TextView(requireActivity());
                        t.setText(s.name);
                        //System.out.println(s.name);
                        b.chapterSubchapters.addView(t);
                    }
                }
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
            
        }
        
        @Override
        public int getItemCount() {
            CoursewareChapter[] chapters = m.getChapters().getValue();
            if (chapters != null && m.selectedChapter != null && m.selectedSubchapter != null) {
                for (CoursewareChapter c : chapters) {
                    if (c.id.equals(m.selectedChapter) && c.subchapters != null) {
                        for (CoursewareSubchapter s : c.subchapters) {
                            if (s.id.equals(m.selectedSubchapter) && s.sections != null) {
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
            TextView v = new TextView(requireActivity());
            v.setText("lorem ipsum\ndolor si tamet");
            v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            f.addView(v);
        }
        
        @Override
        public int getItemCount() {
            return 10;
        }
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
        public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
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
