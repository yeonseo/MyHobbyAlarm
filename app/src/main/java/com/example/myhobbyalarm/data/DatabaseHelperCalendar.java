package com.example.myhobbyalarm.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;
import com.example.myhobbyalarm.util.AlarmUtils;
import com.example.myhobbyalarm.util.JournalUtils;

import java.util.List;

public final class DatabaseHelperCalendar extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "journal.db";
    private static final int SCHEMA = 1;

    private static final String TABLE_NAME = "journal";

    public static final String _ID = "_id";
    public static final String COL_DAY = "day";
    public static final String COL_CONTENT = "content";
    public static final String COL_COLOR = "color_title";

    private static DatabaseHelperCalendar sInstance = null;

    public static synchronized DatabaseHelperCalendar getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelperCalendar(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelperCalendar(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.i(getClass().getSimpleName(), "Creating database... journal");

        final String CREATE_ALARMS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DAY + " INTEGER NOT NULL, " +
                COL_CONTENT + " TEXT, " +
                COL_COLOR + " TEXT" +
                ");";

        sqLiteDatabase.execSQL(CREATE_ALARMS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i(getClass().getSimpleName(), "onUpgrade()...");
        throw new UnsupportedOperationException("This shouldn't happen yet!");
    }

    public long addJournal() {
        Log.i(getClass().getSimpleName(), "add journal...");
        return addJournal(new Journal());
    }

    long addJournal(Journal journal) {
        Log.i(getClass().getSimpleName(), "addJournal(Journal journal) ...");
        return getWritableDatabase().insert(TABLE_NAME, null, JournalUtils.toContentValues(journal));
    }

    public int updateJournal(Journal journal) {
        final String where = _ID + "=?";
        final String[] whereArgs = new String[] { Long.toString(journal.getId()) };
        Log.i(getClass().getSimpleName(), "updateAlarm...");
        return getWritableDatabase()
                .update(TABLE_NAME, JournalUtils.toContentValues(journal), where, whereArgs);
    }

    public int deleteJournal(Journal journal) {
        return deleteJournal(journal.getId());
    }

    int deleteJournal(long id) {
        final String where = _ID + "=?";
        final String[] whereArgs = new String[] { Long.toString(id) };
        return getWritableDatabase().delete(TABLE_NAME, where, whereArgs);
    }

    public List<Journal> getJournals() {
        Log.i(getClass().getSimpleName(), "getJournals()...");
        Cursor c = null;

        try{
            c = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
            return JournalUtils.buildJournalList(c);
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }

    }

}
