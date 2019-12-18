package com.example.myhobbyalarm.ui;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class HomeWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HomeWidgetRemoteViewsFactory(this.getApplicationContext());
    }

}
