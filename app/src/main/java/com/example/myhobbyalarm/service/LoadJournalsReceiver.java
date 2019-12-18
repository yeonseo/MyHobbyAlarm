package com.example.myhobbyalarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;

import java.util.ArrayList;

public final class LoadJournalsReceiver extends BroadcastReceiver {

    private OnJournalsLoadedListener mListener;

    @SuppressWarnings("unused")
    public LoadJournalsReceiver(){}

    public LoadJournalsReceiver(OnJournalsLoadedListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "onReceive ...");
        final ArrayList<Journal> journals =
                intent.getParcelableArrayListExtra(LoadJournalsService.JOURNALS_EXTRA);
        mListener.onJournalsLoaded(journals);
    }

    public void setOnJournalsLoadedListener(OnJournalsLoadedListener listener) {
        mListener = listener;
    }

    public interface OnJournalsLoadedListener {
        void onJournalsLoaded(ArrayList<Journal> journals);
    }

}
