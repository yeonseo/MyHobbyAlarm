package com.example.myhobbyalarm.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.data.DatabaseHelperCalendar;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;

import java.util.ArrayList;
import java.util.List;

public class LoadJournalsService extends IntentService {

    private static final String TAG = LoadJournalsService.class.getSimpleName();
    public static final String ACTION_COMPLETE = TAG + ".ACTION_COMPLETE";
    public static final String JOURNALS_EXTRA = "journals_extra";

    @SuppressWarnings("unused")
    public LoadJournalsService() {
        this(TAG);
    }

    public LoadJournalsService(String name){
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent ...");
        final List<Journal> journals = DatabaseHelperCalendar.getInstance(this).getJournals();

        final Intent i = new Intent(ACTION_COMPLETE);
        i.putParcelableArrayListExtra(JOURNALS_EXTRA, new ArrayList<>(journals));
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

    }

    public static void launchLoadJournalsService(Context context) {
        Log.i(TAG, "launchLoadJournalsService ...");
        final Intent launchLoadJournalsServiceIntent = new Intent(context, LoadJournalsService.class);
        context.startService(launchLoadJournalsServiceIntent);
    }
}
