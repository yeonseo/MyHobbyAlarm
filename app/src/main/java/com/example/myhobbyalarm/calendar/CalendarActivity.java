package com.example.myhobbyalarm.calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.model.Alarm;

import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    public static final String MODE_EXTRA = "calendar_today";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);
        Log.i(getClass().getSimpleName(), "onCreate ...");
        //noinspection ConstantConditions
        getSupportActionBar().setTitle("하루 기록장");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        Alarm today = new Alarm();

        if(getSupportFragmentManager().findFragmentById(R.id.calendar_container) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.calendar_container, CalendarFragment.newInstance(today))
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent buildCalendarActivityIntent(Context context, Date today) {
        final Intent i = new Intent(context, CalendarActivity.class);
        i.putExtra(MODE_EXTRA, today);
        return i;
    }
}
