package com.example.mdp_grp29.ui.arena;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ArenaViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ArenaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}