package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.InvocationTargetException;

public class StringViewModelFactory implements ViewModelProvider.Factory
{
    private final Application mApplication;
    private final String mParam;
    public StringViewModelFactory(Application application, String param) {
        mApplication = application;
        mParam = param;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        try
        {
            return modelClass.getConstructor(Application.class,String.class).newInstance(mApplication,mParam);
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
