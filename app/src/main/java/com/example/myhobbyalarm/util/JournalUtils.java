package com.example.myhobbyalarm.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.util.SparseBooleanArray;

import androidx.core.app.ActivityCompat;

import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.data.DatabaseHelperCalendar;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public final class JournalUtils {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy,MM,dd", Locale.getDefault());

    private JournalUtils() {
        throw new AssertionError();
    }


    /**
     * Add for branch DBSnoozeColorAdd 2019,12,11 by YS
     * about isSnooze,colorTitle
     * "ADD VALUE"
     */

    public static ContentValues toContentValues(Journal journal) {

//        final ContentValues cv = new ContentValues(10);
        final ContentValues cv = new ContentValues(3);

        cv.put(DatabaseHelperCalendar.COL_DAY, journal.getDay());
        cv.put(DatabaseHelperCalendar.COL_CONTENT, journal.getContent());
        cv.put(DatabaseHelperCalendar.COL_COLOR, journal.getColorTitle());
        return cv;

    }

    public static ArrayList<Journal> buildJournalList(Cursor c) {

        if (c == null) return new ArrayList<>();

        final int size = c.getCount();

        final ArrayList<Journal> journals = new ArrayList<>(size);

        if (c.moveToFirst()) {
            do {

                final long id = c.getLong(c.getColumnIndex(DatabaseHelperCalendar._ID));
                final String day = c.getString(c.getColumnIndex(DatabaseHelperCalendar.COL_DAY));
                final String content = c.getString(c.getColumnIndex(DatabaseHelperCalendar.COL_CONTENT));
                final String color = c.getString(c.getColumnIndex(DatabaseHelperCalendar.COL_COLOR));

                final Journal journal = new Journal(id, day, content, color);
                journal.setDay(day);
                journal.setContent(content);
                journal.setColorTitle(color);

                journals.add(journal);

            } while (c.moveToNext());
        }
        return journals;
    }


}
