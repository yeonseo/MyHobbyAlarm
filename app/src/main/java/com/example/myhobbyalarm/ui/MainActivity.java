package com.example.myhobbyalarm.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.example.myhobbyalarm.R;

public class MainActivity extends AppCompatActivity {

        private static final String TAG = "MainActivity";
//        private static ArrayList<ToDoItem> mToDoItemsArrayList = new ArrayList<ToDoItem>();
//
//        private FrameLayout frameLayout;
//        private ListView list;
//
//        private SQLiteDatabase db;
//        private DbHelper mDbHelper;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            getSupportActionBar().setTitle("Task Reminder");
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

//            replaceFragment(new CalendarFragment());

        }

    /**
     * Fragment에서 Fragment를 화면 전환하기 위한 함수 선언
     */
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();      // Fragment로 사용할 MainActivity내의 layout공간을 선택합니다.
    }

//    @Override
//    public void onCalendarFragmentInteraction(ArrayList<ToDoItem> list) {
//        this.mToDoItemsArrayList = list;
//    }
//
//    @Override
//    public void onDayListFragmentInteraction(ArrayList<ToDoItem> list) {
//        this.mToDoItemsArrayList = list;
//    }
//
//    @Override
//    public void onTodoAddFragmentInteraction(ArrayList<ToDoItem> list) {
//        this.mToDoItemsArrayList = list;
//    }
}
