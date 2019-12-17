package com.example.myhobbyalarm.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.adapter.AlarmsAdapter;
import com.example.myhobbyalarm.adapter.JournalAdapter;
import com.example.myhobbyalarm.calendar.decorators.EventDecorator;
import com.example.myhobbyalarm.calendar.decorators.OneDayDecorator;
import com.example.myhobbyalarm.calendar.decorators.SaturdayDecorator;
import com.example.myhobbyalarm.calendar.decorators.SundayDecorator;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;
import com.example.myhobbyalarm.service.LoadAlarmsService;
import com.example.myhobbyalarm.service.LoadJournalsReceiver;
import com.example.myhobbyalarm.service.LoadJournalsService;
import com.example.myhobbyalarm.ui.AddEditAlarmActivity;
import com.example.myhobbyalarm.ui.AddEditJournalActivity;
import com.example.myhobbyalarm.view.DividerItemDecoration;
import com.example.myhobbyalarm.view.EmptyRecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import static com.example.myhobbyalarm.ui.AddEditJournalActivity.buildAddEditJournalActivityIntent;
import static com.example.myhobbyalarm.ui.AddEditJournalActivity.ADD_JOURNAL;

public class CalendarFragment extends Fragment implements
        LoadJournalsReceiver.OnJournalsLoadedListener {
    private static final String TAG = "CalendarFragment";
    private LoadJournalsReceiver mReceiver;
    private JournalAdapter mAdapter;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new LoadJournalsReceiver(this);
        Log.d(TAG,"onCreate");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        setHasOptionsMenu(true);

        final EmptyRecyclerView rv = v.findViewById(R.id.recycler);
        mAdapter = new JournalAdapter();
        rv.setEmptyView(v.findViewById(R.id.empty_view));
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getContext()));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());

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
        final Journal journal = getJournal();

        eventDay.put("2019,03,18", "TEST");
        eventDay.put("2019,10,18", "HIHIHI");
        eventDay.put("2019,10,18", ":-)");
        eventDay.put("2019,11,8", "HAHAHA");
        eventDay.put("2019,11,20", "YAY");
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

        final FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setBackgroundResource(R.drawable.custom_gradients_color_1);
        fab.setOnClickListener(view -> {
            final Intent i = buildAddEditJournalActivityIntent(getContext(), ADD_JOURNAL);
            startActivity(i);
            Log.d(TAG,"onCreateView, FloatingActionButton");
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(LoadAlarmsService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
        LoadJournalsService.launchLoadJournalsService(getContext());
        Log.d(TAG,"onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        Log.d(TAG,"onStop");
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
                break;
            case R.id.action_delete:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Journal getJournal() {
        return getArguments().getParcelable(AddEditJournalActivity.JOURNAL_EXTRA);
    }

    private void eventDrow() {
        for (String mapkey : eventDay.keySet()) {
            result.add(mapkey);
            Log.i("shot_Day test", mapkey + "");
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

    @Override
    public void onJournalsLoaded(ArrayList<Journal> journals) {

    }
}
