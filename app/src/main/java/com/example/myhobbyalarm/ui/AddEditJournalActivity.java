package com.example.myhobbyalarm.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.data.DatabaseHelperCalendar;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;
import com.example.myhobbyalarm.service.LoadAlarmsService;
import com.example.myhobbyalarm.service.LoadJournalsService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AddEditJournalActivity extends AppCompatActivity {

    public static final String JOURNAL_EXTRA = "journal_extra";
    public static final String MODE_EXTRA = "mode_extra";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_JOURNAL,ADD_JOURNAL,UNKNOWN})
    @interface Mode{}
    public static final int EDIT_JOURNAL = 1;
    public static final int ADD_JOURNAL = 2;
    public static final int UNKNOWN = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Log.i(getClass().getSimpleName(), "onCreate ...");
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getToolbarTitle());

        final Journal journal = getJournal();

        if(getSupportFragmentManager().findFragmentById(R.id.edit_alarm_frag_container) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.edit_alarm_frag_container, AddEditJournalFragment.newInstance(journal))
                    .commit();
        }

    }

    private Journal getJournal() {
        switch (getMode()) {
            case EDIT_JOURNAL:
                return getIntent().getParcelableExtra(JOURNAL_EXTRA);
            case ADD_JOURNAL:
                final long id = DatabaseHelperCalendar.getInstance(this).addJournal();
                LoadJournalsService.launchLoadJournalsService(this);
                return new Journal(id);
            case UNKNOWN:
            default:
                throw new IllegalStateException("Mode supplied as intent extra for " +
                        AddEditJournalActivity.class.getSimpleName() + " must match value in " +
                        Mode.class.getSimpleName());
        }
    }

    private @Mode int getMode() {
        final @Mode int mode = getIntent().getIntExtra(MODE_EXTRA, UNKNOWN);
        return mode;
    }

    private String getToolbarTitle() {
        int titleResId;
        switch (getMode()) {
            case EDIT_JOURNAL:
                titleResId = R.string.edit_alarm;
                break;
            case ADD_JOURNAL:
                titleResId = R.string.add_alarm;
                break;
            case UNKNOWN:
            default:
                throw new IllegalStateException("Mode supplied as intent extra for " +
                        AddEditJournalActivity.class.getSimpleName() + " must match value in " +
                        Mode.class.getSimpleName());
        }
        return getString(titleResId);
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

    public static Intent buildAddEditJournalActivityIntent(Context context, @Mode int mode) {
        final Intent i = new Intent(context, AddEditJournalActivity.class);
        i.putExtra(MODE_EXTRA, mode);
        return i;
    }
}
