package com.example.hadon.customwidgetlistview;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class MyService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("ㅁㅁ","Myserv의 RemoteViewsFactory");
        return new MyRemoteViewsFactory(this.getApplicationContext());
    }

}
