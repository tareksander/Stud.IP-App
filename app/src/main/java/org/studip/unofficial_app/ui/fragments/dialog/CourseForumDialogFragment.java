package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipCourseWithForumCategories;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumCategoryWithEntries;
import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.api.rest.StudipForumEntryWithChildren;
import org.studip.unofficial_app.databinding.DialogForumBinding;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.ForumResource;
import org.studip.unofficial_app.model.viewmodels.ForumViewModel;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;
import org.studip.unofficial_app.ui.HomeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

public class CourseForumDialogFragment extends DialogFragment
{
    public static final String COURSE_ID_KEY = "cid";
    
    ForumViewModel m;
    DialogForumBinding binding;
    AlertDialog d;
    
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        
        Bundle args = getArguments();
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return b.create();
        }
        String cid = args.getString(COURSE_ID_KEY);
        m = new ViewModelProvider(this,new StringViewModelFactory(requireActivity().getApplication(),cid)).get(ForumViewModel.class);
    
        binding = DialogForumBinding.inflate(getLayoutInflater());
        b.setView(binding.getRoot());
        
        b.setTitle("");
        
        d = b.create();
        
        binding.forumRefresh.setOnRefreshListener(() -> m.f.refresh(requireActivity()));
        
        m.f.isRefreshing().observe(this, binding.forumRefresh::setRefreshing);
        m.f.getStatus().observe(this, (status) -> {
            if (status != 200 && status != 201 && status != -1)
            {
                dismiss();
                HomeActivity.onStatusReturn(requireActivity(), status);
            }
        });
        
        ForumAdapter ad = new ForumAdapter(requireActivity(),ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
        binding.forumList.setAdapter(ad);
        
        m.f.get().observe(this, ad::setObject);
        
        m.f.refresh(requireActivity());
        
        d.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                {
                    switch (m.f.getSelectedEntry().getType()) {
                        case COURSE:
                            return false;
                        case CATEGORY:
                            m.f.setEntry(requireActivity(),null);
                            return true;
                        case ENTRY:
                            System.out.println("entry");
                            if (ad.o instanceof StudipForumEntryWithChildren) {
                                StudipForumEntryWithChildren ents = (StudipForumEntryWithChildren) ad.o;
                                System.out.println("parent: "+ents.entry.parent_id);
                                if (ents.entry.depth.equals("1")) {
                                    m.f.setEntry(requireActivity(),new ForumResource.ForumEntry(ents.entry.parent_id, ForumResource.ForumEntry.Type.CATEGORY));
                                    //System.out.println("category");
                                } else {
                                    m.f.setEntry(requireActivity(),new ForumResource.ForumEntry(ents.entry.parent_id, ForumResource.ForumEntry.Type.ENTRY));
                                    //System.out.println("entry");
                                }
                            } else {
                                System.out.println("not loaded yet");
                            }
                            return true;
                    }
                }
                return false;
            }
        });
        
        
        d.setCanceledOnTouchOutside(false);
        return d;
    }
    
    public class ForumAdapter extends ArrayAdapter<Object> {
        private Object o;
        public ForumAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
        
        public void setObject(Object o) {
            this.o = o;
            if (o instanceof StudipCourseWithForumCategories) {
                StudipCourseWithForumCategories cc = (StudipCourseWithForumCategories) o;
                StudipCourse c = cc.c;
                binding.forumParentName.setText(c.title);
            }
            if (o instanceof StudipForumCategoryWithEntries) {
                StudipForumCategoryWithEntries catc = (StudipForumCategoryWithEntries) o;
                StudipForumCategory cat = catc.category;
                binding.forumParentName.setText(cat.entry_name);
            }
            if (o instanceof StudipForumEntryWithChildren) {
                //System.out.println("count: "+(((StudipForumEntryWithChildren)o).children.size()+1));
                //System.out.println(((StudipForumEntryWithChildren)o).entry.depth);
                StudipForumEntryWithChildren e = (StudipForumEntryWithChildren) o;
                binding.forumParentName.setText(Jsoup.parse(e.entry.subject).wholeText());
            }
            //System.out.println("object updated");
            notifyDataSetChanged();
        }
    
        @Override
        public int getCount() {
            if (o instanceof StudipCourseWithForumCategories) {
                //System.out.println("count: "+((StudipCourseWithForumCategories)o).categories.size());
                return ((StudipCourseWithForumCategories)o).categories.size();
            }
            if (o instanceof StudipForumCategoryWithEntries) {
                //System.out.println("count: "+((StudipForumCategoryWithEntries)o).children.size());
                return ((StudipForumCategoryWithEntries)o).children.size();
            }
            if (o instanceof StudipForumEntryWithChildren) {
                //System.out.println("count: "+(((StudipForumEntryWithChildren)o).children.size()+1));
                //System.out.println(((StudipForumEntryWithChildren)o).entry.depth);
                StudipForumEntryWithChildren e = (StudipForumEntryWithChildren) o;
                if (e.entry.depth.equals("1")) {
                    return e.children.size();
                } else {
                    return e.children.size()+1;
                }
            }
            //System.out.println("count: 0");
            return 0;
        }
    
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView t;
            if (convertView instanceof TextView) {
                t = (TextView) convertView;
            } else {
                t = new TextView(requireActivity());
            }
            t.setText("");
            t.setOnClickListener(null);
            if (o instanceof StudipCourseWithForumCategories) {
                StudipCourseWithForumCategories cc = (StudipCourseWithForumCategories) o;
                List<StudipForumCategory> cats = cc.categories;
                Collections.sort(cats);
                StudipForumCategory cat = cats.toArray(new StudipForumCategory[0])[position];
                t.setText(cat.entry_name);
                t.setOnClickListener(v1 -> m.f.setEntry(requireActivity(), new ForumResource.ForumEntry(cat.category_id, ForumResource.ForumEntry.Type.CATEGORY)));
            }
            
            if (o instanceof StudipForumCategoryWithEntries) {
                StudipForumCategoryWithEntries catc = (StudipForumCategoryWithEntries) o;
                List<StudipForumEntry> children = catc.children;
                Collections.sort(children, (o1, o2) -> {
                    try {
                        return Jsoup.parse(o1.subject).wholeText().compareTo(Jsoup.parse(o2.subject).wholeText());
                    } catch (Exception ignored) {
                        return 0;
                    }
                });
                StudipForumEntry e = children.toArray(new StudipForumEntry[0])[position];
                t.setText(Jsoup.parse(e.subject).wholeText());
                t.setOnClickListener(v1 -> m.f.setEntry(requireActivity(), new ForumResource.ForumEntry(e.topic_id, ForumResource.ForumEntry.Type.ENTRY)));
            }
            if (o instanceof StudipForumEntryWithChildren) {
                StudipForumEntryWithChildren ec = (StudipForumEntryWithChildren) o;
                StudipForumEntry e = ec.entry;
                List<StudipForumEntry> children = ec.children;
                if (! e.depth.equals("1")) {
                    Collections.sort(children, (o1, o2) -> {
                        try {
                            return Integer.parseInt(o1.mkdate) - Integer.parseInt(o2.mkdate);
                        } catch (Exception ignored) {
                            return 0;
                        }
                    });
                    if (position != 0) {
                        e = children.toArray(new StudipForumEntry[0])[position - 1];
                    }
                    t.setText(Jsoup.parse(e.content).wholeText());
                } else {
                    Collections.sort(children);
                    e = children.toArray(new StudipForumEntry[0])[position];
                    t.setText(Jsoup.parse(e.subject).wholeText());
                    final StudipForumEntry finalE = e;
                    t.setOnClickListener(v1 -> m.f.setEntry(requireActivity(), new ForumResource.ForumEntry(finalE.topic_id, ForumResource.ForumEntry.Type.ENTRY)));
                }
            }
            return t;
        }
    }
    
    
    
    
    
}
