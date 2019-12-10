package com.example.myhobbyalarm.calendar;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.ui.MainActivity;
import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.decorators.EventDecorator;
import com.example.myhobbyalarm.decorators.OneDayDecorator;
import com.example.myhobbyalarm.decorators.SaturdayDecorator;
import com.example.myhobbyalarm.decorators.SundayDecorator;
import com.example.myhobbyalarm.ui.MainFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment {
// implements View.OnClickListener {
//    private String TAG = "CalendarFragment : ";
//
//    private static ArrayList<Alarm> mToDoItemsArrayList = new ArrayList<Alarm>();
//    private OnCalendarFragmentInteractionListener mListListener;
//    private static ArrayList<Alarm> mToDoItemsSelected;
//
//    /**
//     * For MaterialCalendarView
//     */
//    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
//    MaterialCalendarView materialCalendarView;
//    ArrayList<String> result = new ArrayList<String>();
//    String shot_Day;
//    ApiSimulator apiSimulator;
//
//    Context context;
//
//    /**
//     * Intent Code
//     */
//    private static final int REQUEST_ID_TODO_ITEM = 100;
//
//    public static final String DATE_TIME_FORMAT_12_HOUR = "MMM d, yyyy  h:mm a";
//    public static final String DATE_TIME_FORMAT_24_HOUR = "MMM d, yyyy  k:mm";
//
//    /**
//     * File Name
//     */
//    public static final String FILENAME = "todoitems.json";
//
//    /**
//     * File Code
//     */
//    public static final String SHARED_PREF_DATA_SET_CHANGED = "com.example.myminimaltest.datasetchanged";
//    public static final String CHANGE_OCCURED = "com.example.myminimaltest.changeoccured";
//    public static final String TODOITEM = "com.example.myminimaltest.MainActivity";
//    public static final String THEME_PREFERENCES = "com.example.myminimaltest.themepref";
//
//    public static final String THEME_SAVED = "com.example.myminimaltest.savedtheme";
//    public static final String LIGHTTHEME = "com.example.myminimaltest.lighttheme";
//    public static final String DARKTHEME = "com.example.myminimaltest.darktheme";
//
//    /**
//     * Calendar View *
//     */
//    public CalendarFragment() {
//    }
//
//    public static Fragment newInstance(ArrayList<Alarm> list) {
//        CalendarFragment calendarFragment = new CalendarFragment();
//        mToDoItemsArrayList = list;
//        return calendarFragment;
//    }
//
//
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof OnCalendarFragmentInteractionListener) {
//            mListListener = (OnCalendarFragmentInteractionListener) context;
//            mListListener.onCalendarFragmentInteraction(mToDoItemsArrayList);
//            mToDoItemsArrayList = ((MainActivity) getActivity()).mToDoItemsArrayListEventSetting();
//        } else {
//            throw new RuntimeException(context.toString() + "OnFragmentInteractionListener 구현");
//        }
//
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.calendar_fragment, container, false);
//        Log.d(TAG, "onCreateView");
//
//        materialCalendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView);
//        Button btnDay = view.findViewById(R.id.btnDay);
//
//        materialCalendarView.state().edit()
//                .setFirstDayOfWeek(Calendar.SUNDAY)
//                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
//                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
//                .setCalendarDisplayMode(CalendarMode.MONTHS)
//                .commit();
//
//        materialCalendarView.addDecorators(
//                new SundayDecorator(),
//                new SaturdayDecorator(),
//                oneDayDecorator);
//
//        eventDrow();
////        setAlarms();
//        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
//            @Override
//            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
//                int Year = date.getYear();
//                int Month = date.getMonth() + 1;
//                int Day = date.getDay();
//
//                Log.i("Year test", Year + "");
//                Log.i("Month test", Month + "");
//                Log.i("Day test", Day + "");
//
//                shot_Day = Year + "," + Month + "," + Day;
//
//
//                Log.i("shot_Day test", shot_Day + "");
//                materialCalendarView.clearSelection();
//
//                Toast.makeText(getActivity(), shot_Day + "\n" + getContext(), Toast.LENGTH_SHORT).show();
//
//                for (Alarm list : mToDoItemsArrayList) {
//                    Alarm toDoItem = list;
//                    if (list.getDays().equals(shot_Day)) {
//                        mToDoItemsSelected.add(toDoItem);
//                    }
//                }
//
//                /**
//                 * list fragment
//                 * */
////                ((MainActivity) getActivity()).replaceFragment(MainFragment.newInstance(mToDoItemsSelected, shot_Day));
//            }
//        });
//
//        btnDay.setOnClickListener(this);
//        return view;
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        /**
//         * Fragment에서 Fragment를 화면 전환하기 위한
//         * MainActivity에 선언된 함수 사용
//         * 새로 불러올 Fragment의 Instance를 Main으로 전달
//         * */
//
//        //추가 버튼을 누를시, 현재 날짜로 추가하기
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//
//        int mYear = calendar.get(Calendar.YEAR);
//        int mMonth = calendar.get(Calendar.MONTH);
//        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
//
//        shot_Day = mYear + "," + mMonth + "," + mDay;
//        ((MainActivity) getActivity()).replaceFragment(TodoAddFragment.newInstance(mToDoItemsArrayList, shot_Day));
//
//    }
//
//    private void eventDrow() {
//        for (ToDoItem list : mToDoItemsArrayList) {
//            String[] getDate = list.getDATE().split("/");
//            if (getDate.length <= 1) {
//            } else {
//                String date = getDate[2] + "," + getDate[1] + "," + getDate[0];
//                Log.d("eventDrow", date);
//                result.add(date);
//            }
//        }
//        apiSimulator = (ApiSimulator) new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());
//    }
//
//    /**
//     * MainActivity와 Fragment간의 데이터 전달하기 위한 인터페이스 선언
//     */
//    public interface OnCalendarFragmentInteractionListener {
//        void onCalendarFragmentInteraction(ArrayList<ToDoItem> list);
//    }
//
//    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {
//
//        ArrayList<String> Time_Result;
//
//        ApiSimulator(ArrayList<String> Time_Result) {
//            this.Time_Result = Time_Result;
//        }
//
//        @Override
//        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            Calendar calendar = Calendar.getInstance();
//            ArrayList<CalendarDay> dates = new ArrayList<>();
//
//            /*특정날짜 달력에 점표시해주는곳*/
//            /*월은 0이 1월 년,일은 그대로*/
//            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
//            for (int i = 0; i < Time_Result.size(); i++) {
//                CalendarDay day = CalendarDay.from(calendar);
//                String[] time = Time_Result.get(i).split(",");
//                int year = Integer.parseInt(time[0]);
//                int month = Integer.parseInt(time[1]);
//                int dayy = Integer.parseInt(time[2]);
//
//                dates.add(day);
//                calendar.set(year, month - 1, dayy);
//            }
//            return dates;
//        }
//
//        @Override
//        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
//            super.onPostExecute(calendarDays);
//
//            if (getActivity().isFinishing()) {
//                return;
//            }
//            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, getActivity()));
//        }
//    }
}
