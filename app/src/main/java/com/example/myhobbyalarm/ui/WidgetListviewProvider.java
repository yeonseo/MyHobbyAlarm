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

/**
 * Implementation of App Widget functionality.
 */
public class WidgetListviewProvider extends AppWidgetProvider {

    public static final String ACTION_TOAST = "actionToast";
    public static final String EXTRA_ITEM_POSITION = "extraItemPosition";
    public static final int WIDGET_BUTTON_EVENT_CHECK_VALUE = -1;
    public static final String WIDGET_BUTTON_EVENT_CHECK_STRING = "buttonEventKey";
    public static final String WIDGET_LIST_ITEM_EVENT_CHECK_STRING = "ListItemEventKey";

    static AppWidgetManager staticAppWidgetManager;
    static int[] staticAppWidgetIds;
    public ArrayList<Alarm> alarms= new ArrayList<Alarm>();
    List<Alarm> alarmsDataList;
    Context context;

    /**
     * 위젯의 크기 및 옵션이 변경될 때마다 호출되는 함수
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d("ㅁㅁ","WidgetListviewProvider 의 updateAppWidget");

//        여기부분 다 사용할 일 없어져서 주석처리함!
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_view_widget);
        views.setTextViewText(R.id.widget_test_textview, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        ClickEventOpenFragment(views, context);

    }


    /**
     * 위젯이 바탕화면에 설치될 때마다 호출되는 함수
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        staticAppWidgetManager = appWidgetManager;
        staticAppWidgetIds = appWidgetIds;
        Log.d("ㅁㅁ","WidgetListviewProvider 의 onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

            // RemoteViewsService 실행 등록시키는 함수
            Intent serviceIntent = new Intent(context, MyService.class);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_view_widget);
            views.setRemoteAdapter(R.id.widget_listview, serviceIntent);
            //맨 위 텍스트 뷰에 현재 시간 출력
            Calendar mCalendar = Calendar.getInstance();
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    Locale.KOREA);
            views.setTextViewText(R.id.widget_test_textview,
                    mFormat.format(mCalendar.getTime()));

            //클릭 이벤트로 액티비티열기
//        Intent intent = new Intent(context, MainActivity.class); //실행할 액티비티의 클래스
//        intent.putExtra("key","key");
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//        widget.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
//클릭 이벤트로 프래그먼트열기

            ClickEventOpenFragment(views, context);
//////////////////////////////////////////////////////////////////////////////////
//        int value=0;
//        Intent configIntent = new Intent(context, MainActivity.class);


            Intent clickIntent = new Intent(context, WidgetListviewProvider.class);
            clickIntent.setAction(ACTION_TOAST);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context,
                    0, clickIntent, 0);
            views.setPendingIntentTemplate(R.id.widget_listview, clickPendingIntent);


//        Bundle bundle = new Bundle();
//        bundle.putInt("key", value);

//        configIntent.putExtra("key", value);
//        configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
//        widget.setOnClickPendingIntent(R.id.widgetButton, configPendingIntent);
///////////////////////////////////////////////////////////////////////////////////////////////


//        클릭이벤트 인텐트 유보.
            //보내기
            Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            resizeWidget(appWidgetOptions, views);
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d("ㅁㅁ","WidgetListviewProvider 의 onAppWidgetOptionsChanged");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_view_widget);

        resizeWidget(newOptions, views);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void resizeWidget(Bundle appWidgetOptions, RemoteViews views) {
        Log.d("ㅁㅁ","WidgetListviewProvider 의 resizeWidget");
        int minWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        if (maxHeight > 300) {
            views.setViewVisibility(R.id.widget_test_textview, View.VISIBLE);
            views.setViewVisibility(R.id.centerLinear, View.VISIBLE);
            views.setViewVisibility(R.id.widgetButton, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_test_textview, View.GONE);
            views.setViewVisibility(R.id.centerLinear, View.GONE);
            views.setViewVisibility(R.id.widgetButton, View.GONE);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ㅁㅁ","WidgetListviewProvider 의 onReceive");

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
//            Toast.makeText(context, "Clicked position: " + clickedPosition, Toast.LENGTH_SHORT).show();
////            ClickEventOpenFragmentAdd(clickedPosition,context);
////            ClickEventOpenFragmentAdd(clickedPosition,context);
//            Intent configIntent = new Intent(context, MainActivity.class);
//            Log.d("ㅁㅁ", "WidgetListviewProvider 의 WIDGET_LIST_ITEM_EVENT_CHECK_STRING = " + WIDGET_LIST_ITEM_EVENT_CHECK_STRING);
//            Log.d("ㅁㅁ", "WidgetListviewProvider 의 clickedPosition = " + clickedPosition);
//            configIntent.putExtra(WIDGET_LIST_ITEM_EVENT_CHECK_STRING, clickedPosition);
//            configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(context, configIntent, null);
        }

        super.onReceive(context, intent);
    }

    private static void ClickEventOpenFragmentAdd(int clickedPosition, Context context) {
//        //클릭 이벤트로 프래그먼트열기
//        Intent configIntent = new Intent(context, MainActivity.class);
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_collection);
//        Log.d("ㅁㅁ","WidgetListviewProvider 의 WIDGET_LIST_ITEM_EVENT_CHECK_STRING = "+WIDGET_LIST_ITEM_EVENT_CHECK_STRING);
//        Log.d("ㅁㅁ","WidgetListviewProvider 의 clickedPosition = "+clickedPosition);
//        configIntent.putExtra(WIDGET_LIST_ITEM_EVENT_CHECK_STRING, clickedPosition);
//        configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
//        views.setOnClickPendingIntent(R.id.widgetButton, configPendingIntent);
    }

    public static void ClickEventOpenFragment(RemoteViews views, Context context) {
        //클릭 이벤트로 프래그먼트열기
//        Intent configIntent = new Intent(context, MainActivity.class);
//
//        Log.d("ㅁㅁ", "WidgetListviewProvider 의 WIDGET_BUTTON_EVENT_CHECK_STRING = " + WIDGET_BUTTON_EVENT_CHECK_STRING);
//        Log.d("ㅁㅁ", "WidgetListviewProvider 의 WIDGET_BUTTON_EVENT_CHECK_VALUE = " + WIDGET_BUTTON_EVENT_CHECK_VALUE);
//        configIntent.putExtra(WIDGET_BUTTON_EVENT_CHECK_STRING, WIDGET_BUTTON_EVENT_CHECK_VALUE);
//        configIntent.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
//        views.setOnClickPendingIntent(R.id.widgetButton, configPendingIntent);


        final Intent i = new Intent(context, AddEditAlarmActivity.class);
        i.putExtra(MODE_EXTRA, ADD_ALARM);


        i.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.widgetButton, configPendingIntent);
    }

    public void setData() {
        Log.d("ㅁㅁ","WidgetListviewProvider 의 setData");

        alarmsDataList = DatabaseHelper.getInstance(context).getAlarms();
        alarms.removeAll(alarms);
        for (Alarm alarm : alarmsDataList){
            alarms.add(alarm);
        }
    }
}
