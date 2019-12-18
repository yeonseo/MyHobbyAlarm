package com.example.myhobbyalarm.ui;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.data.DatabaseHelperCalendar;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;
import com.example.myhobbyalarm.service.LoadJournalsService;
import com.example.myhobbyalarm.util.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

public final class AddEditJournalFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private TextView mDay,pv_day, pv_content;
    private EditText mContent;
    private ImageButton pvBtn;
    private String colorTitleSet;
    private static final int[] colorTitle_Id = {
            R.id.edit_alarm_color_softRed,
            R.id.edit_alarm_color_lightOrange,
            R.id.edit_alarm_color_softOrange,
            R.id.edit_alarm_color_slightlyCyan,
            R.id.edit_alarm_color_slightlyGreen,
            R.id.edit_alarm_color_green,
            R.id.edit_alarm_color_strongCyan,
            R.id.edit_alarm_color_blue,
            R.id.edit_alarm_color_moderateBlue,
            R.id.edit_alarm_color_moderateViolet,
            R.id.edit_alarm_color_black};
    private RadioGroup edit_alarm_rdo_g;
    private RadioButton[] colorTitle = new RadioButton[colorTitle_Id.length];

    private Journal journal;
    private String dateJDB = "";

    public static Fragment newInstance(Journal journal) {
        Bundle args = new Bundle();
        args.putParcelable(AddEditJournalActivity.JOURNAL_EXTRA, journal);

        AddEditJournalFragment fragment = new AddEditJournalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_add_edit_journal, container, false);

        //타이틀 바에 옵션나타내기
        setHasOptionsMenu(true);

        //선택한 Journal 가져오기
        journal = getJournal();

        mDay = v.findViewById(R.id.edit_journal_day);
        mContent = v.findViewById(R.id.edit_journal_content);
        pv_day = v.findViewById(R.id.edit_journal_pv_day);
        pv_content = v.findViewById(R.id.edit_journal_pv_content);
        edit_alarm_rdo_g = v.findViewById(R.id.edit_alarm_rdo_g);
        for (int i = 0; i < colorTitle_Id.length; i++) {
            colorTitle[i] = (RadioButton) v.findViewById(colorTitle_Id[i]);
        }
        pvBtn = v.findViewById(R.id.edit_journal_ivbtn);


        //Journal Date에서 가져온 날짜 셋팅
        String[] dateSet = journal.getDay().split(",");

        for(int i = 0 ; i < dateSet.length ; i++){
            dateJDB = dateJDB+" "+dateSet[i];
        }
        mDay.setText(dateJDB);

        //저장된 일기 셋팅하기

        mContent.setText(journal.getContent());
        //저장된 타이틀 색을 불러와 셋팅하기
        setDayCheckColorTitle(journal);

        //라디오 그룹 체크에 따른 타이틀 색 저장하기 위해 변수에 색 이름 담기
        edit_alarm_rdo_g.setOnCheckedChangeListener(this);

        onClick(pvBtn);

        pvBtn.setOnClickListener(this);
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

    private Journal getJournal() {
        return getArguments().getParcelable(AddEditJournalActivity.JOURNAL_EXTRA);
    }

    private void save() {
        final Journal journal = getJournal();
        Log.d(getClass().getSimpleName(), "journal ID : " + journal.getId());

        journal.setColorTitle(colorTitleSet);
        journal.setDay(mDay.getText().toString());
        journal.setContent(mContent.getText().toString());
        journal.setColorTitle(colorTitleSet);
        Log.d(getClass().getSimpleName(), "setColorTitle colorTitleSet : " + colorTitleSet);

        final int rowsUpdated = DatabaseHelperCalendar.getInstance(getContext()).updateJournal(journal);
        final int messageId = (rowsUpdated == 1) ? R.string.update_complete : R.string.update_failed;

        Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();


        getActivity().finish();

//        // donuni widget listView 새로고침!
//        if (HomeWidgetListviewProvider.staticAppWidgetIds == null) {
//
//        } else {
//            HomeWidgetListviewProvider.staticAppWidgetManager.
//                    notifyAppWidgetViewDataChanged(HomeWidgetListviewProvider.staticAppWidgetIds, R.id.widget_listview);
//
//        }
    }

    private void delete() {
        final Journal journal = getJournal();

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext(), R.style.DeleteAlarmDialogTheme);
        builder.setTitle(R.string.delete_dialog_title);
        builder.setMessage(R.string.delete_dialog_content);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Cancel any pending notifications for this alarm
                final int rowsDeleted = DatabaseHelperCalendar.getInstance(getContext()).deleteJournal(journal);
                int messageId;
                if (rowsDeleted == 1) {
                    messageId = R.string.delete_complete;
                    Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                    LoadJournalsService.launchLoadJournalsService(getContext());
                    getActivity().finish();
                } else {
                    messageId = R.string.delete_failed;
                    Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();

//        // donuni widget listView 새로고침!
//        if (HomeWidgetListviewProvider.staticAppWidgetIds == null) {
//
//        } else {
//            HomeWidgetListviewProvider.staticAppWidgetManager.
//                    notifyAppWidgetViewDataChanged(HomeWidgetListviewProvider.staticAppWidgetIds, R.id.widget_listview);
//        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.edit_alarm_color_softRed:
                colorTitleSet = "softRed";
                break;
            case R.id.edit_alarm_color_lightOrange:
                colorTitleSet = "lightOrange";
                break;
            case R.id.edit_alarm_color_softOrange:
                colorTitleSet = "softOrange";
                break;
            case R.id.edit_alarm_color_slightlyCyan:
                colorTitleSet = "slightlyCyan";
                break;
            case R.id.edit_alarm_color_slightlyGreen:
                colorTitleSet = "slightlyGreen";
                break;
            case R.id.edit_alarm_color_green:
                colorTitleSet:
                colorTitleSet = "green";
                break;
            case R.id.edit_alarm_color_strongCyan:
                colorTitleSet = "strongCyan";
                break;
            case R.id.edit_alarm_color_blue:
                colorTitleSet = "blue";
                break;
            case R.id.edit_alarm_color_moderateBlue:
                colorTitleSet = "moderateBlue";
                break;
            case R.id.edit_alarm_color_moderateViolet:
                colorTitleSet = "moderateViolet";
                break;
            case R.id.edit_alarm_color_black:
                colorTitleSet = "black";
                break;
        }

        Log.d(getClass().getSimpleName(), "onCheckedChanged colorTitleSet : " + colorTitleSet);
    }

    private void setDayCheckColorTitle(Journal journal) {
        int colorSet = R.color.softRed;
        if (journal.getColorTitle() == null) {
            journal.setColorTitle("softRed");
        }
        switch (journal.getColorTitle()) {
            case "lightOrange":
                colorTitle[1].setChecked(true);
                colorTitleSet = "lightOrange";
                colorSet = R.color.lightOrange;
                break;
            case "softOrange":
                colorTitle[2].setChecked(true);
                colorTitleSet = "softOrange";
                colorSet = R.color.softOrange;
                break;
            case "slightlyCyan":
                colorTitle[3].setChecked(true);
                colorTitleSet = "slightlyCyan";
                colorSet = R.color.slightlyCyan;
                break;
            case "slightlyGreen":
                colorTitle[4].setChecked(true);
                colorTitleSet = "slightlyGreen";
                colorSet = R.color.slightlyGreen;
                break;
            case "green":
                colorTitle[5].setChecked(true);
                colorTitleSet = "green";
                colorSet = R.color.green;
                break;
            case "strongCyan":
                colorTitle[6].setChecked(true);
                colorTitleSet = "strongCyan";
                colorSet = R.color.strongCyan;
                break;
            case "blue":
                colorTitle[7].setChecked(true);
                colorTitleSet = "blue";
                colorSet = R.color.blue;
                break;
            case "moderateBlue":
                colorTitle[8].setChecked(true);
                colorTitleSet = "moderateBlue";
                colorSet = R.color.moderateBlue;
                break;
            case "moderateViolet":
                colorTitle[9].setChecked(true);
                colorTitleSet = "moderateViolet";
                colorSet = R.color.moderateViolet;
                break;
            case "black":
                colorTitle[10].setChecked(true);
                colorTitleSet = "black";
                colorSet = R.color.black;
                break;
            case "softRed":
            default:
                colorTitle[0].setChecked(true);
                colorTitleSet = "softRed";
                colorSet = R.color.softRed;
                break;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ColorStateList stateList = ColorStateList.valueOf(getContext().getResources().getColor(colorSet));
            pv_content.setBackgroundTintList(stateList);
        } else {
            pv_content.getBackground().getCurrent().setColorFilter(
                    new PorterDuffColorFilter(getContext().getResources().getColor(colorSet), PorterDuff.Mode.MULTIPLY));
        }
    }

    @Override
    public void onClick(View v) {
        pv_day.setText(dateJDB);
        pv_content.setText(mContent.getText().toString());
        journal.setColorTitle(colorTitleSet);
        setDayCheckColorTitle(journal);
    }
}
