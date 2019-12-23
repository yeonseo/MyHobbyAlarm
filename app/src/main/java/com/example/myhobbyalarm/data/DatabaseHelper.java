package com.example.myhobbyalarm.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.util.AlarmUtils;

import java.util.List;

public final class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarms.db";
    private static final int SCHEMA = 1;

    private static final String TABLE_NAME = "alarms";

    /**
     * Alarm 기본 정보
     * */
    public static final String _ID = "_id";
    public static final String COL_TIME = "time";
    public static final String COL_LABEL = "label";
    public static final String COL_MON = "mon";
    public static final String COL_TUES = "tues";
    public static final String COL_WED = "wed";
    public static final String COL_THURS = "thurs";
    public static final String COL_FRI = "fri";
    public static final String COL_SAT = "sat";
    public static final String COL_SUN = "sun";
    public static final String COL_IS_ENABLED = "is_enabled";

    /**
     * Add for branch DBSnoozeColorAdd 2019,12,10 by YS
     * */
    public static final String COL_IS_SNOOZE = "is_snooze";
    public static final String COL_COLOR = "color_title";

    private static DatabaseHelper sInstance = null;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.i(getClass().getSimpleName(), "Creating database...");

        final String CREATE_ALARMS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TIME + " INTEGER NOT NULL, " +
                COL_LABEL + " TEXT, " +
                COL_MON + " INTEGER NOT NULL, " +
                COL_TUES + " INTEGER NOT NULL, " +
                COL_WED + " INTEGER NOT NULL, " +
                COL_THURS + " INTEGER NOT NULL, " +
                COL_FRI + " INTEGER NOT NULL, " +
                COL_SAT + " INTEGER NOT NULL, " +
                COL_SUN + " INTEGER NOT NULL, " +
                COL_IS_ENABLED + " INTEGER NOT NULL," +

                /**
                 * Add for branch DBSnoozeColorAdd 2019,12,10 by YS
                 * */
                COL_IS_SNOOZE + " INTEGER NOT NULL, " +
                COL_COLOR + " TEXT" +
                ");";

        sqLiteDatabase.execSQL(CREATE_ALARMS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i(getClass().getSimpleName(), "onUpgrade()...");
        throw new UnsupportedOperationException("This shouldn't happen yet!");
    }


    /**
     * 알람을 추가하는 행위를 할 시 불러지는 함수
     * 새로운 알람을 추가해서 아래의 addAlarm(Alarm alarm)를 호출한다
     * */
    public long addAlarm() {
        Log.i(getClass().getSimpleName(), "addAlarm()...");
        return addAlarm(new Alarm());
    }

    long addAlarm(Alarm alarm) {
        Log.i(getClass().getSimpleName(), "addAlarm(Alarm alarm) ...");
        return getWritableDatabase().insert(TABLE_NAME, null, AlarmUtils.toContentValues(alarm));
    }

    public int updateAlarm(Alarm alarm) {
        final String where = _ID + "=?";
        final String[] whereArgs = new String[] { Long.toString(alarm.getId()) };
        Log.i(getClass().getSimpleName(), "updateAlarm...");
        return getWritableDatabase()
                .update(TABLE_NAME, AlarmUtils.toContentValues(alarm), where, whereArgs);
    }

    public int deleteAlarm(Alarm alarm) {
        return deleteAlarm(alarm.getId());
    }

    int deleteAlarm(long id) {
        final String where = _ID + "=?";
        final String[] whereArgs = new String[] { Long.toString(id) };
        return getWritableDatabase().delete(TABLE_NAME, where, whereArgs);
    }

    public List<Alarm> getAlarms() {
        Log.i(getClass().getSimpleName(), "getAlarms()...");
        Cursor c = null;

        try{
            c = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
            return AlarmUtils.buildAlarmList(c);
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }

    }

}
