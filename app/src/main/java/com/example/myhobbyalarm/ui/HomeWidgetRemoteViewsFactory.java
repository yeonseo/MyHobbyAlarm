package com.example.myhobbyalarm.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.util.AlarmUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.myhobbyalarm.ui.HomeWidgetListviewProvider.EXTRA_ITEM_POSITION;
import static com.example.myhobbyalarm.ui.HomeWidgetListviewProvider.timeTextSet;

/**
 * 런처 앱에 리스트뷰의 어뎁터 역할을 해주는 클래스
 */
class HomeWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{
    //요일
    private String[] mDays;
    private int mAccentColor = -1;

    //context 설정하기
    public Context context = null;
    public ArrayList<Alarm> alarms= new ArrayList<Alarm>();
    List<Alarm> alarmsDataList;

    public HomeWidgetRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        setData();
    }

    @Override
    public void onDestroy() {
    }

    public void setData() {
        alarmsDataList = DatabaseHelper.getInstance(context).getAlarms();
        alarms.removeAll(alarms);
        for (Alarm alarm : alarmsDataList){
            alarms.add(alarm);
        }
        timeTextSet();
    }

    /**
     *항목 추가 및 제거 등 데이터 변경이 발생했을 때 호출되는 함수
     * 브로드캐스트 리시버에서 notifyAppWidgetViewDataChanged()가 호출 될 때 자동 호출
     */
    @Override
    public void onDataSetChanged() {
        setData();
    }

    // 항목 개수를 반환하는 함수
    @Override
    public int getCount() {
        return alarms.size();
    }
    /**
     * 각 항목을 구현하기 위해 호출, 매개변수 값을 참조하여 각 항목을 구성하기위한 로직이 담긴다.
     * 항목 선택 이벤트 발생 시 인텐트에 담겨야 할 항목 데이터를 추가해주어야 하는 함수
     */
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews listviewWidget = new RemoteViews(context.getPackageName(), R.layout.item_collection);
        listviewWidget.setTextViewText(R.id.txToDo, alarms.get(position).getLabel());
        String date = AlarmUtils.getReadableTime(alarms.get(position).getTime())+"";
        if(date.length()==4){
            date = "  "+AlarmUtils.getReadableTime(alarms.get(position).getTime())+"";
        }
        listviewWidget.setTextViewText(R.id.txDate, date);
        listviewWidget.setTextViewText(R.id.ar_am_pm, AlarmUtils.getAmPm(alarms.get(position).getTime())+"");
        listviewWidget.setTextViewText(R.id.ar_days, buildSelectedDays(alarms.get(position)));
        int alarmColorImageResource = alarmColorSetting(alarms.get(position));
        listviewWidget.setImageViewResource(R.id.widget_ar_icon,
                alarmColorImageResource);
        Intent fillIntent = new Intent();
        fillIntent.putExtra(EXTRA_ITEM_POSITION,position);
        listviewWidget.setOnClickFillInIntent(R.id.lL,fillIntent);
        return listviewWidget;
    }

    private int alarmColorSetting(Alarm alarm) {

        int colorSet = R.drawable.ic_alarm_lightorange_24dp;
        if (alarm.getColorTitle() == null) alarm.setColorTitle("softRed");
        switch (alarm.getColorTitle()) {
            case "lightOrange":
                colorSet = R.drawable.ic_alarm_lightorange_24dp;
                break;
            case "softOrange":
                colorSet = R.drawable.ic_alarm_softorange_24dp;
                break;
            case "slightlyCyan":
                colorSet = R.drawable.ic_alarm_slightlycyan_24dp;
                break;
            case "slightlyGreen":
                colorSet = R.drawable.ic_alarm_slightlycyan_24dp;
                break;
            case "green":
                colorSet = R.drawable.ic_alarm_green_24dp;
                break;
            case "strongCyan":
                colorSet = R.drawable.ic_alarm_strongcyan_24dp;
                break;
            case "blue":
                colorSet = R.drawable.ic_alarm_blue_24dp;
                break;
            case "moderateBlue":
                colorSet = R.drawable.ic_alarm_moderateblue_24dp;
                break;
            case "moderateViolet":
                colorSet = R.drawable.ic_alarm_moderateviolet_24dp;
                break;
            case "black":
                colorSet = R.drawable.ic_alarm_black_24dp;
                break;
            default:
                break;
        }
        return colorSet;
    }

    /**
     *로딩 뷰를 표현하기 위해 호출, 없으면 null
     */
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    /**
     *항목의 타입 갯수를 판단하기 위해 호출, 모든 항목이 같은 뷰 타입이라면 1을 반환하면 된다.
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     *각 항목의 식별자 값을 얻기 위해 호출
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     *같은 ID가 항상 같은 개체를 참조하면 true 반환하는 함수
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     *리스트 항목에 요일 셋팅
     */
    private Spannable buildSelectedDays(Alarm alarm) {
        if (mAccentColor==-1){
            mAccentColor= ContextCompat.getColor(context, R.color.colorPrimary);
        }
        mDays = context.getResources().getStringArray(R.array.days_abbreviated);
        final int numDays = 7;
        final SparseBooleanArray days = alarm.getDays();
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        ForegroundColorSpan span;
        int startIndex, endIndex;
        for (int i = 0; i < numDays; i++) {
            startIndex = builder.length();
            final String dayText = mDays[i];
            builder.append(dayText);
            builder.append(" ");
            endIndex = startIndex + dayText.length();
            final boolean isSelected = days.valueAt(i);
            if (isSelected) {
                span = new ForegroundColorSpan(mAccentColor);
                builder.setSpan(span, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }
}
