package com.example.hadon.customwidgetlistview;

import android.util.Log;

public class WidgetItemData {
    String date;
    String toDo;

    public WidgetItemData(String date, String toDo) {
        this.date = date;
        this.toDo = toDo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getToDo() {
        return toDo;
    }

    public void setToDo(String toDo) {
        this.toDo = toDo;
    }
}