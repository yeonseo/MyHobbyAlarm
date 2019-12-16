package com.example.myhobbyalarm.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhobbyalarm.ui.AddEditAlarmActivity;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.util.AlarmUtils;
import com.example.myhobbyalarm.R;

import java.util.List;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {
    private static final String TAG = "AlarmsAdapter";

    private List<Alarm> mAlarms;

    private String[] mDays;
    private int mAccentColor = -1;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context c = parent.getContext();
        final View v = LayoutInflater.from(c).inflate(R.layout.alarm_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Context c = holder.itemView.getContext();

        if (mAccentColor == -1) {
            mAccentColor = ContextCompat.getColor(c, R.color.colorPrimary);
        }

        if (mDays == null) {
            mDays = c.getResources().getStringArray(R.array.days_abbreviated);
        }

        final Alarm alarm = mAlarms.get(position);

        holder.time.setText(AlarmUtils.getReadableTime(alarm.getTime()));
        holder.amPm.setText(AlarmUtils.getAmPm(alarm.getTime()));
        holder.label.setText(alarm.getLabel());
        holder.days.setText(buildSelectedDays(alarm));

        Log.d(TAG, alarm.toString());
        int colorSet = R.color.softRed;
        if (alarm.getColorTitle() == null) alarm.setColorTitle("softRed");
        Log.d(TAG, "if after : " + alarm.toString());
        switch (alarm.getColorTitle()) {
            case "lightOrange":
                colorSet = R.color.lightOrange;
                break;
            case "softOrange":
                colorSet = R.color.softOrange;
                break;
            case "slightlyCyan":
                colorSet = R.color.slightlyCyan;
                break;
            case "slightlyGreen":
                colorSet = R.color.slightlyGreen;
                break;
            case "green":
                colorSet = R.color.green;
                break;
            case "strongCyan":
                colorSet = R.color.strongCyan;
                break;
            case "blue":
                colorSet = R.color.blue;
                break;
            case "moderateBlue":
                colorSet = R.color.moderateBlue;
                break;
            case "moderateViolet":
                colorSet = R.color.moderateViolet;
                break;
            case "black":
                colorSet = R.color.black;
                break;
            default:
                break;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ColorStateList stateList = ColorStateList.valueOf(c.getResources().getColor(colorSet));
            holder.ar_color.setBackgroundTintList(stateList);
            holder.ar_icon.setImageTintList(stateList);
        } else {
            holder.ar_color.getBackground().getCurrent().setColorFilter(
                    new PorterDuffColorFilter(c.getResources().getColor(colorSet), PorterDuff.Mode.MULTIPLY));
            holder.ar_icon.getBackground().getCurrent().setColorFilter(
                    new PorterDuffColorFilter(c.getResources().getColor(colorSet), PorterDuff.Mode.OVERLAY));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context c = view.getContext();
                final Intent launchEditAlarmIntent =
                        AddEditAlarmActivity.buildAddEditAlarmActivityIntent(
                                c, AddEditAlarmActivity.EDIT_ALARM
                        );
                launchEditAlarmIntent.putExtra(AddEditAlarmActivity.ALARM_EXTRA, alarm);
                c.startActivity(launchEditAlarmIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (mAlarms == null) ? 0 : mAlarms.size();
    }

    private Spannable buildSelectedDays(Alarm alarm) {

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

    public void setAlarms(List<Alarm> alarms) {
        Log.d(TAG, "setAlarms");
        mAlarms = alarms;
        notifyDataSetChanged();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        final TextView time, amPm, label, days;
        final ImageView ar_color,ar_icon;

        ViewHolder(View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.ar_time);
            amPm = itemView.findViewById(R.id.ar_am_pm);
            label = itemView.findViewById(R.id.ar_label);
            days = itemView.findViewById(R.id.ar_days);
            ar_color = itemView.findViewById(R.id.ar_color);
            ar_icon = itemView.findViewById(R.id.ar_icon);

        }
    }
}
