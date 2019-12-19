package com.example.myhobbyalarm.ui;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.model.Alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.core.content.ContextCompat.startActivity;
import static com.example.myhobbyalarm.ui.AddEditAlarmActivity.ADD_ALARM;
import static com.example.myhobbyalarm.ui.AddEditAlarmActivity.MODE_EXTRA;

public class HomeWidgetListviewProvider extends AppWidgetProvider {

    public static final String ACTION_TOAST = "actionToast";
    public static final String EXTRA_ITEM_POSITION = "extraItemPosition";
    static AppWidgetManager staticAppWidgetManager;
    static int[] staticAppWidgetIds;
    public ArrayList<Alarm> alarms= new ArrayList<Alarm>();
    List<Alarm> alarmsDataList;
    Context context;
    static RemoteViews views;
    static int[] sappWidgetIds;
    /**
     * 위젯의 크기 및 옵션이 변경될 때마다 호출되는 함수
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_view_widget);
        views.setTextViewText(R.id.widget_test_textview, widgetText);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        ClickEventOpenFragment(views, context);

    }


    /**
     * 위젯이 바탕화면에 설치될 때마다 호출되는 함수
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        sappWidgetIds = appWidgetIds;
        staticAppWidgetManager = appWidgetManager;
        staticAppWidgetIds = appWidgetIds;
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

            // RemoteViewsService 실행 등록시키는 함수
            Intent serviceIntent = new Intent(context, HomeWidgetService.class);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views = new RemoteViews(context.getPackageName(), R.layout.list_view_widget);
            views.setRemoteAdapter(R.id.widget_listview, serviceIntent);
            //맨 위 텍스트 뷰에 현재 시간 출력
            Calendar mCalendar = Calendar.getInstance();
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    Locale.KOREA);
            Log.d("ㅁㅁ",mFormat.format(mCalendar.getTime()));
            views.setTextViewText(R.id.widget_test_textview,
                    mFormat.format(mCalendar.getTime()));

            ClickEventOpenFragment(views, context);
            Intent clickIntent = new Intent(context, HomeWidgetListviewProvider.class);
            clickIntent.setAction(ACTION_TOAST);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context,
                    0, clickIntent, 0);
            views.setPendingIntentTemplate(R.id.widget_listview, clickPendingIntent);
            Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            resizeWidget(appWidgetOptions, views);
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        }
    }
    /**
     *현재 시간 셋팅
     */
    public static void timeTextSet() {
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.KOREA);
        Log.d("ㅁㅁ",mFormat.format(mCalendar.getTime()));
        views.setTextViewText(R.id.widget_test_textview,
                mFormat.format(mCalendar.getTime()));

        staticAppWidgetManager.updateAppWidget(sappWidgetIds, views);
    }

    /**
     *사이즈 변경을 감지하는 함수
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_view_widget);
        resizeWidget(newOptions, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    /**
     *사이즈 변경을 감지한 값으로 높이에 따라 홈위젯의 상단바를 감춘다.
     */
    private void resizeWidget(Bundle appWidgetOptions, RemoteViews views) {
        int minWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        if (maxHeight > 150) {
            views.setViewVisibility(R.id.widget_test_textview, View.VISIBLE);
            views.setViewVisibility(R.id.centerLinear, View.VISIBLE);
            views.setViewVisibility(R.id.widgetButton, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_test_textview, View.GONE);
            views.setViewVisibility(R.id.centerLinear, View.GONE);
            views.setViewVisibility(R.id.widgetButton, View.GONE);
        }
    }
    /**
     *홈 위젯 리스트의 클릭 포지션을 MyRemoteFactory로부터 받아 해당 포지션의 알람객체를 다시 전달
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        setData();
        if (ACTION_TOAST.equals(intent.getAction())) {
            int clickedPosition = intent.getIntExtra(EXTRA_ITEM_POSITION, 0);
            Alarm alarm=alarms.get(clickedPosition);
            final Intent launchEditAlarmIntent =
                    AddEditAlarmActivity.buildAddEditAlarmActivityIntent(
                            context, AddEditAlarmActivity.EDIT_ALARM
                    );
            launchEditAlarmIntent.putExtra(AddEditAlarmActivity.ALARM_EXTRA, alarm);
            launchEditAlarmIntent.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(context,launchEditAlarmIntent,null);
        }
        super.onReceive(context, intent);
    }
    /**
     *추가버튼 클릭시 이벤트
     */
    public static void ClickEventOpenFragment(RemoteViews view, Context context) {
        views=view;
        timeTextSet();
        final Intent i = new Intent(context, AddEditAlarmActivity.class);
        i.putExtra(MODE_EXTRA, ADD_ALARM);
        i.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.widgetButton, configPendingIntent);
    }
    /**
     *DB로 alarms를 셋팅함
     */
    public void setData() {
        alarmsDataList = DatabaseHelper.getInstance(context).getAlarms();
        alarms.removeAll(alarms);
        for (Alarm alarm : alarmsDataList){
            alarms.add(alarm);
        }
    }
}
