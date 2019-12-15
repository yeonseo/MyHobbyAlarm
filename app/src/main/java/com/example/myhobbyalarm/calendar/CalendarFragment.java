package com.example.myhobbyalarm.calendar;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.calendar.decorators.EventDecorator;
import com.example.myhobbyalarm.calendar.decorators.OneDayDecorator;
import com.example.myhobbyalarm.calendar.decorators.SaturdayDecorator;
import com.example.myhobbyalarm.calendar.decorators.SundayDecorator;
import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.service.AlarmReceiver;
import com.example.myhobbyalarm.service.LoadAlarmsService;
import com.example.myhobbyalarm.ui.AddEditAlarmActivity;
import com.example.myhobbyalarm.ui.AddEditAlarmFragment;
import com.example.myhobbyalarm.ui.MainActivity;
import com.example.myhobbyalarm.util.ViewUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment {

    /**
     * Add for branch DBSnoozeColorAdd 2019,12,15 by YS
     * about calendar
     * "ADD VALUE"
     */
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private MaterialCalendarView materialCalendarView;
    private EditText edtContent;
    private ArrayList<String> result = new ArrayList<String>();
    private Map<String, String> eventDay = new HashMap<>();
    private ApiSimulator apiSimulator;


    public static Fragment newInstance(Alarm calendar) {

        Bundle args = new Bundle();
        args.putParcelable(CalendarActivity.MODE_EXTRA, calendar);

        CalendarFragment fragment = new CalendarFragment();
        fragment.setArguments(args);
        return fragment;
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        setHasOptionsMenu(true);

        materialCalendarView = (MaterialCalendarView) v.findViewById(R.id.calendarView);
        edtContent = v.findViewById(R.id.edtContent);
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);
        final Alarm alarm = getAlarm();

        eventDay.put("2019,03,18", "");
        eventDay.put("2019,10,18", "");
        eventDay.put("2019,10,18", "");
        eventDay.put("2019,11,8", "");
        eventDay.put("2019,11,20", "");
        eventDrow();

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                String shot_Day = Year + "," + Month + "," + Day;
                String content = eventDay.get(shot_Day);


                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

                Toast.makeText(getActivity(), shot_Day + "\n" + content, Toast.LENGTH_SHORT).show();
                saveEvent(shot_Day);
            }
        });

        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_alarm_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                break;
            case R.id.action_delete:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Alarm getAlarm() {
        return getArguments().getParcelable(AddEditAlarmActivity.ALARM_EXTRA);
    }

    private void eventDrow() {
        for (String mapkey : eventDay.keySet()) {
            result.add(mapkey);
        }
        apiSimulator = (ApiSimulator) new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    private void saveEvent(String shot_Day) {
        if (edtContent.getText().equals("")) {
            Toast.makeText(getContext(), "내용을 적어주세요~~~", Toast.LENGTH_SHORT).show();
        } else {
            if (eventDay.get(shot_Day)==null||eventDay.get(shot_Day).equals("")) {
                result.add(shot_Day);
                eventDay.put(shot_Day, edtContent.getText().toString());
            }
            Toast.makeText(getContext(), shot_Day + "\n" + eventDay.get(shot_Day), Toast.LENGTH_SHORT).show();
        }
        edtContent.setText("");
        eventDrow();
    }

    private void save() {

        final Alarm alarm = getAlarm();

//        final Calendar time = Calendar.getInstance();
//        time.set(Calendar.MINUTE, ViewUtils.getTimePickerMinute(mTimePicker));
//        time.set(Calendar.HOUR_OF_DAY, ViewUtils.getTimePickerHour(mTimePicker));
//        alarm.setTime(time.getTimeInMillis());
//
//        alarm.setLabel(mLabel.getText().toString());
//
//        alarm.setDay(Alarm.MON, mMon.isChecked());
//        alarm.setDay(Alarm.TUES, mTues.isChecked());
//        alarm.setDay(Alarm.WED, mWed.isChecked());
//        alarm.setDay(Alarm.THURS, mThurs.isChecked());
//        alarm.setDay(Alarm.FRI, mFri.isChecked());
//        alarm.setDay(Alarm.SAT, mSat.isChecked());
//        alarm.setDay(Alarm.SUN, mSun.isChecked());
//
//        //ADD VALUE
//        alarm.setSnooze(edit_alarm_snooze.isChecked());
//        alarm.setColorTitle(colorTitleSet);
//        Log.d(getClass().getSimpleName(), "setColorTitle colorTitleSet : " + colorTitleSet);
//
//        final int rowsUpdated = DatabaseHelper.getInstance(getContext()).updateAlarm(alarm);
//        final int messageId = (rowsUpdated == 1) ? R.string.update_complete : R.string.update_failed;
//
//        Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
//
//        AlarmReceiver.setReminderAlarm(getContext(), alarm);

        getActivity().finish();

    }

    private void delete() {

        final Alarm alarm = getAlarm();

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext(), R.style.DeleteAlarmDialogTheme);
        builder.setTitle(R.string.delete_dialog_title);
        builder.setMessage(R.string.delete_dialog_content);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Cancel any pending notifications for this alarm
                AlarmReceiver.cancelReminderAlarm(getContext(), alarm);

                final int rowsDeleted = DatabaseHelper.getInstance(getContext()).deleteAlarm(alarm);
                int messageId;
                if (rowsDeleted == 1) {
                    messageId = R.string.delete_complete;
                    Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                    LoadAlarmsService.launchLoadAlarmsService(getContext());
                    getActivity().finish();
                } else {
                    messageId = R.string.delete_failed;
                    Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();

    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        ArrayList<String> Time_Result;

        ApiSimulator(ArrayList<String> Time_Result) {
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for (int i = 0; i < Time_Result.size(); i++) {
                CalendarDay day = CalendarDay.from(calendar);
                String[] time = Time_Result.get(i).split(",");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(year, month - 1, dayy);
            }
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (getActivity().isFinishing()) {
                return;
            }
            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, getActivity()));
        }
    }
}
