package com.example.myhobbyalarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myhobbyalarm.model.Alarm;

import java.util.ArrayList;

public final class LoadAlarmsReceiver extends BroadcastReceiver {

    private OnAlarmsLoadedListener mListener;

    @SuppressWarnings("unused")
    public LoadAlarmsReceiver(){}

    public LoadAlarmsReceiver(OnAlarmsLoadedListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(getClass().getSimpleName(), "onReceive ...");
        final ArrayList<Alarm> alarms =
                intent.getParcelableArrayListExtra(LoadAlarmsService.ALARMS_EXTRA);
        mListener.onAlarmsLoaded(alarms);
    }

    public void setOnAlarmsLoadedListener(OnAlarmsLoadedListener listener) {
        mListener = listener;
    }

    public interface OnAlarmsLoadedListener {
        void onAlarmsLoaded(ArrayList<Alarm> alarms);
    }

}
