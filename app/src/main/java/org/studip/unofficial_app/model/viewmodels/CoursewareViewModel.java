package org.studip.unofficial_app.model.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareChapter;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareSection;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareSubchapter;
import org.studip.unofficial_app.model.APIProvider;

public class CoursewareViewModel extends AndroidViewModel
{
    
    private final MutableLiveData<Boolean> status = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final MutableLiveData<CoursewareChapter[]> data;
    private final String cid;
    
    public static final int TYPE_CHAPTERS = 0;
    public static final int TYPE_CHAPTER = 1;
    public static final int TYPE_SUBCHAPTER = 2;
    public static final int TYPE_SECTION = 3;
    
    private final static String DATA_KEY = "data";
    
    public final MutableLiveData<String> selectedChapterData;
    public final MutableLiveData<String> selectedSubchapterData;
    public final MutableLiveData<String> selectedSectionData;
    
    public CoursewareViewModel(@NonNull @NotNull Application application, String cid, SavedStateHandle h) {
        super(application);
        this.cid = cid;
        data = h.getLiveData(DATA_KEY);
        selectedChapterData = h.getLiveData("chapter");
        selectedSubchapterData = h.getLiveData("subchapter");
        selectedSectionData = h.getLiveData("section");
        if (data.getValue() == null) {
            refresh(application, null, TYPE_CHAPTERS);
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void refresh(Context c, String id, int type) {
        API api = APIProvider.getAPI(c);
        if (! refreshing.getValue() && api != null) {
            refreshing.setValue(true);
            if (id == null || type == TYPE_CHAPTERS) {
                api.courseware.getChapters(cid).subscribe((chapters, throwable) -> {
                    refreshing.postValue(false);
                    if (chapters != null) {
                        data.postValue(chapters);
                    } else {
                        status.postValue(true);
                    }
                });
            } else {
                switch (type) {
                    case TYPE_CHAPTER:
                        api.courseware.getSubchapters(cid, id).subscribe((subchapters, throwable) -> {
                            refreshing.postValue(false);
                            if (subchapters != null) {
                                CoursewareChapter[] chapters = data.getValue();
                                if (chapters != null) {
                                    for (CoursewareChapter cc : chapters) {
                                        if (cc != null && cc.id.equals(id)) {
                                            cc.subchapters = subchapters;
                                            break;
                                        }
                                    }
                                }
                                data.postValue(chapters);
                            } else {
                                status.postValue(true);
                            }
                        });
                        break;
                    case TYPE_SUBCHAPTER:
                        api.courseware.getSections(cid, id).subscribe((sections, throwable) -> {
                            refreshing.postValue(false);
                            if (sections != null) {
                                boolean found = false;
                                CoursewareChapter[] chapters = data.getValue();
                                if (chapters != null) {
                                    for (CoursewareChapter cc : chapters) {
                                        if (cc != null && cc.subchapters != null) {
                                            for (CoursewareSubchapter s : cc.subchapters) {
                                                if (s != null && s.id.equals(id)) {
                                                    s.sections = sections;
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (found) break;
                                    }
                                }
                                data.postValue(chapters);
                            } else {
                                throwable.printStackTrace();
                                status.postValue(true);
                            }
                        });
                        break;
                    case TYPE_SECTION:
                        api.courseware.getBlocks(cid, id).subscribe((blocks, throwable) -> {
                            refreshing.postValue(false);
                            if (blocks != null) {
                                boolean found = false;
                                CoursewareChapter[] chapters = data.getValue();
                                if (chapters != null) {
                                    for (CoursewareChapter cc : chapters) {
                                        if (cc != null && cc.subchapters != null) {
                                            for (CoursewareSubchapter s : cc.subchapters) {
                                                if (s != null && s.sections != null) {
                                                    for (CoursewareSection sect : s.sections) {
                                                        if (sect != null && sect.id.equals(id)) {
                                                            sect.blocks = blocks;
                                                            found = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (found) break;
                                            }
                                        }
                                        if (found) break;
                                    }
                                }
                                data.postValue(chapters);
                            } else {
                                status.postValue(true);
                            }
                        });
                        break;
                    default:
                        refreshing.setValue(false);
                }
            }
        }
    }
    
    public void reload(Context c) {
        data.setValue(null);
        selectedChapterData.setValue(null);
        selectedSubchapterData.setValue(null);
        selectedSectionData.setValue(null);
        refresh(c, null, TYPE_CHAPTERS);
    }
    
    public LiveData<CoursewareChapter[]> getChapters() {
        return data;
    }
    
    public LiveData<Boolean> isRefreshing() {
        return refreshing;
    }
    
    public LiveData<Boolean> isError() {
        return status;
    }
}
