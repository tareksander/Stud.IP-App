package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipCourseWithForumCategories;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumCategoryWithEntries;
import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.api.rest.StudipForumEntryWithChildren;
import org.studip.unofficial_app.databinding.DialogForumBinding;
import org.studip.unofficial_app.databinding.DialogForumEntryBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.ForumResource;
import org.studip.unofficial_app.model.viewmodels.ForumViewModel;
import org.studip.unofficial_app.model.viewmodels.StringSavedStateViewModelFactory;
import org.studip.unofficial_app.ui.HelpActivity;
import org.studip.unofficial_app.ui.HomeActivity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseForumDialogFragment extends DialogFragment
{
    public static final String COURSE_ID_KEY = "cid";
    private static final String OBJECT_KEY = "obj";
    
    private ForumViewModel m;
    private DialogForumBinding binding;
    private AlertDialog d;
    private String cid;
    private Object currentObject = null;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        
        Bundle args = getArguments();
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return b.create();
        }
        cid = args.getString(COURSE_ID_KEY);
        m = new ViewModelProvider(this,new StringSavedStateViewModelFactory(this, null, 
                requireActivity().getApplication(),cid)).get(ForumViewModel.class);
        
        
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
        if (savedInstanceState != null) {
            ad.setObject(savedInstanceState.getSerializable(OBJECT_KEY));
        }
        binding.forumList.setAdapter(ad);
        
        m.f.get().observe(this, (fs) -> {
            ad.setObject(fs);
            currentObject = fs;
        });
        if (savedInstanceState == null) {
            m.f.refresh(requireActivity());
        }
    
        
        
        d.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
            {
                switch (m.f.getSelectedEntry().getType()) {
                    case COURSE:
                        return false;
                    case CATEGORY:
                        m.f.setEntry(requireActivity(),null);
                        return true;
                    case ENTRY:
                        //System.out.println("entry");
                        Object o = ad.o;
                        if (o instanceof StudipForumEntryWithChildren) {
                            StudipForumEntryWithChildren ents = (StudipForumEntryWithChildren) o;
                            //System.out.println("parent: "+ents.entry.parent_id);
                            if (ents.entry.depth.equals("1")) {
                                m.f.setEntry(requireActivity(),new ForumResource.ForumEntry(ents.entry.parent_id, ForumResource.ForumEntry.Type.CATEGORY));
                                //System.out.println("category");
                            } else {
                                m.f.setEntry(requireActivity(),new ForumResource.ForumEntry(ents.entry.parent_id, ForumResource.ForumEntry.Type.ENTRY));
                                //System.out.println("entry");
                            }
                        } else {
                            //System.out.println("not loaded yet");
                        }
                        return true;
                }
            }
            return false;
        });
        
        
        binding.forumSubmit.setOnClickListener(v1 -> {
            Object o = ad.o;
            if (o instanceof StudipForumEntryWithChildren) {
                StudipForumEntryWithChildren ents = (StudipForumEntryWithChildren) o;
                String subject = binding.forumSubject.getText().toString();
                String content = binding.forumContent.getText().toString();
                if (subject.equals("") && ents.entry.depth.equals("1")) {
                    Toast.makeText(requireActivity(), R.string.message_no_subject, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (content.equals("")) {
                    Toast.makeText(requireActivity(), R.string.message_no_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                API api = APIProvider.getAPI(requireActivity());
                if (api != null) {
                    binding.forumRefresh.setRefreshing(true);
                    // a workaround, as is seems impossible to create a forum post with the REST-API, it complains about permissions
                    api.dispatch.getForumPage(ents.entry.topic_id,cid).enqueue(new Callback<String>()
                    {
                        @Override
                        public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                            boolean found = false;
                            String res = response.body();
                            //System.out.println(res);
                            if (res != null && response.code() == 200) {
                                Document d = Jsoup.parse(res);
                                Element head = d.head();
                                for (Element h : head.children()) {
                                    if (h.tag().equals(Tag.valueOf("script"))) {
                                        String script = h.data();
                                        //System.out.println(script);
                                        Pattern p = Pattern.compile(".*CSRF_TOKEN: \\{[^}]+?value: \\'([^}]+?)\\'[^}]+?\\}\\,.*",Pattern.DOTALL);
                                        Matcher mat = p.matcher(script);
                                        if (mat.matches()) {
                                            String token = mat.group(1);
                                            //System.out.println("Token: \""+token+"\"");
                                            found = true;
                                            api.dispatch.postForumEntry(cid,ents.entry.topic_id,token,subject,content).enqueue(new Callback<Void>()
                                            {
                                                @Override
                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                    binding.forumRefresh.setRefreshing(false);
                                                    //System.out.println(response.code());
                                                    if (response.code() == 302 || response.code() == 200) {
                                                        m.f.refresh(requireActivity());
                                                        binding.forumSubject.setText("");
                                                        binding.forumContent.setText("");
                                                    }
                                                }
    
                                                @Override
                                                public void onFailure(Call<Void> call, Throwable t) {
                                                    binding.forumRefresh.setRefreshing(false);
                                                }
                                            });
                                        }
                                    }
                                }
                            } else {
                                HomeActivity.onStatusReturn(requireActivity(),response.code());
                            }
                            if (! found) {
                                binding.forumRefresh.setRefreshing(false);
                            }
                        }
    
                        @Override
                        public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                            //t.printStackTrace();
                            binding.forumRefresh.setRefreshing(false);
                        }
                    });
                }
                /*
                APIProvider.getAPI(requireActivity()).forum.addEntry(ents.entry.topic_id,subject,content).enqueue(new Callback<StudipForumEntry>()
                {
                    @Override
                    public void onResponse(@NotNull Call<StudipForumEntry> call, @NotNull Response<StudipForumEntry> response) {
                        System.out.println(response.code());
                        StudipForumEntry e = response.body();
                        if (e != null) {
                            binding.forumContent.setText("");
                            binding.forumSubject.setText("");
                            m.f.setEntry(requireActivity(),new ForumResource.ForumEntry(e.topic_id, ForumResource.ForumEntry.Type.ENTRY));
                        } else {
                            HomeActivity.onStatusReturn(requireActivity(),response.code());
                        }
                    }
    
                    @Override
                    public void onFailure(@NotNull Call<StudipForumEntry> call, @NotNull Throwable t) {}
                });
                */
            }
        });
        
        d.setCanceledOnTouchOutside(false);
        return d;
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentObject instanceof Serializable) {
            outState.putSerializable(OBJECT_KEY, (Serializable) currentObject);
        }
    }
    
    public class ForumAdapter extends ArrayAdapter<Object> {
        private Object o;
        public ForumAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
        
        public void setObject(Object o) {
            this.o = o;
            if (o == null) {
                binding.forumSubject.setVisibility(View.GONE);
                binding.forumContent.setVisibility(View.GONE);
                binding.forumSubmit.setVisibility(View.GONE);
            }
            if (o instanceof StudipCourseWithForumCategories) {
                StudipCourseWithForumCategories cc = (StudipCourseWithForumCategories) o;
                StudipCourse c = cc.c;
                binding.forumParentName.setText(c.title);
                binding.forumSubject.setVisibility(View.GONE);
                binding.forumContent.setVisibility(View.GONE);
                binding.forumSubmit.setVisibility(View.GONE);
            }
            if (o instanceof StudipForumCategoryWithEntries) {
                StudipForumCategoryWithEntries catc = (StudipForumCategoryWithEntries) o;
                StudipForumCategory cat = catc.category;
                binding.forumParentName.setText(cat.entry_name);
                binding.forumSubject.setVisibility(View.GONE);
                binding.forumContent.setVisibility(View.GONE);
                binding.forumSubmit.setVisibility(View.GONE);
            }
            if (o instanceof StudipForumEntryWithChildren) {
                //System.out.println("count: "+(((StudipForumEntryWithChildren)o).children.size()+1));
                //System.out.println(((StudipForumEntryWithChildren)o).entry.depth);
                StudipForumEntryWithChildren e = (StudipForumEntryWithChildren) o;
                binding.forumParentName.setText(Jsoup.parse(e.entry.subject).wholeText());
                if (! ((StudipForumEntryWithChildren) o).entry.depth.equals("1")) {
                    binding.forumSubject.setVisibility(View.GONE);
                } else {
                    binding.forumSubject.setVisibility(View.VISIBLE);
                }
                binding.forumContent.setVisibility(View.VISIBLE);
                binding.forumSubmit.setVisibility(View.VISIBLE);
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
                DialogForumEntryBinding b = DialogForumEntryBinding.inflate(getLayoutInflater());
                t = b.t;
            }
            t.setText("");
            t.setOnClickListener(null);
            if (o instanceof StudipCourseWithForumCategories) {
                StudipCourseWithForumCategories cc = (StudipCourseWithForumCategories) o;
                List<StudipForumCategory> cats = cc.categories;
                Collections.sort(cats);
                StudipForumCategory cat = cats.toArray(new StudipForumCategory[0])[position];
                t.setText(cat.entry_name);
                t.setOnClickListener(v1 -> {
                    if (! binding.forumRefresh.isRefreshing()) {
                        m.f.setEntry(requireActivity(), new ForumResource.ForumEntry(cat.category_id, ForumResource.ForumEntry.Type.CATEGORY));
                    }
                });
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
                t.setOnClickListener(v1 -> {
                    if (! binding.forumRefresh.isRefreshing()) {
                        m.f.setEntry(requireActivity(), new ForumResource.ForumEntry(e.topic_id, ForumResource.ForumEntry.Type.ENTRY));
                    }
                });
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
                    t.setMovementMethod(LinkMovementMethod.getInstance());
                    t.setText(HelpActivity.fromHTML(e.content));
                } else {
                    Collections.sort(children);
                    e = children.toArray(new StudipForumEntry[0])[position];
                    t.setText(Jsoup.parse(e.subject).wholeText());
                    final StudipForumEntry finalE = e;
                    t.setOnClickListener(v1 -> {
                        if (! binding.forumRefresh.isRefreshing()) {
                            m.f.setEntry(requireActivity(), new ForumResource.ForumEntry(finalE.topic_id, ForumResource.ForumEntry.Type.ENTRY));
                        }
                    });
                }
            }
            return t;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        d = null;
        binding = null;
    }
    
}
