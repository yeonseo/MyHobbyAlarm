package com.example.myhobbyalarm.ui;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class MyService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("ㅁㅁ","Myserv의 RemoteViewsFactory");
        return new MyRemoteViewsFactory(this.getApplicationContext());
    }

}
