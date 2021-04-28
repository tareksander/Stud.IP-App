package org.studip.unofficial_app.model.viewmodels;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
public class MkdirDialogViewModel extends ViewModel
{
    public final MutableLiveData<String> dirName = new MutableLiveData<>(null);
}
