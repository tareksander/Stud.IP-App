package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import java.lang.reflect.InvocationTargetException;

public class StringSavedStateViewModelFactory extends AbstractSavedStateViewModelFactory
{
    private final Application mApplication;
    private final String mParam;
    
    public StringSavedStateViewModelFactory(@NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs, Application application, String param) {
        super(owner, defaultArgs);
        mApplication = application;
        mParam = param;
    }
    
    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        try
        {
            return modelClass.getConstructor(Application.class,String.class, SavedStateHandle.class).newInstance(mApplication,mParam, handle);
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
