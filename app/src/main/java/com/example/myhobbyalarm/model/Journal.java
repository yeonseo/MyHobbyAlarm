package com.example.myhobbyalarm.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseBooleanArray;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Journal implements Parcelable {

    /**
     * Add for branch DBSnoozeColorAdd 2019,12,16 by YS
     * about isSnooze,colorTitle
     * "ADD VALUE"
     * */

    private static final long NO_ID = -1;

    private long id;
    private String day;
    private String content;
    private String colorTitle;


    private Journal(Parcel in) {
        Log.i(getClass().getSimpleName(), "Creating database...");
        id = in.readLong();
        day = in.readString();
        content = in.readString();
        colorTitle = in.readString();
    }

    public static final Creator<Journal> CREATOR = new Creator<Journal>() {
        @Override
        public Journal createFromParcel(Parcel in) {
            return new Journal(in);
        }

        @Override
        public Journal[] newArray(int size) {
            return new Journal[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(day);
        parcel.writeString(content);
        parcel.writeString(colorTitle);
    }
    public Journal() {
        this(NO_ID);
    }

    public Journal(long id) {
        this(id, null, null, null);
    }

    public Journal(long id, String day, String content, String colorTitle) {
        this.id = id;
        if(day==null){
            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyy,MM,dd", Locale.getDefault()).format(currentTime);
            this.day = date_text;
        }else{
            this.day = day;
        }
        this.content = content;
        this.colorTitle = colorTitle;
    }

    public static long getNoId() {
        return NO_ID;
    }

    public long getId() {
        return id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getColorTitle() {
        return colorTitle;
    }

    public void setColorTitle(String colorTitle) {
        this.colorTitle = colorTitle;
    }

    public static Creator<Journal> getCREATOR() {
        return CREATOR;
    }

    @Override
    public String toString() {
        return "Journal{" +
                "id=" + id +
                ", day='" + day + '\'' +
                ", content='" + content + '\'' +
                ", colorTitle='" + colorTitle + '\'' +
                '}';
    }
}
