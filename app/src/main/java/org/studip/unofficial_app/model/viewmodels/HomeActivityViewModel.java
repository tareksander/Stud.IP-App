package org.studip.unofficial_app.model.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.ui.HomeActivity;

public class HomeActivityViewModel extends ViewModel
{
    public final MutableLiveData<StudipCourse> filesCourse = new MutableLiveData<>(null);
    public final MutableLiveData<Boolean> connectionLostDialogShown = new MutableLiveData<>(false);
}
