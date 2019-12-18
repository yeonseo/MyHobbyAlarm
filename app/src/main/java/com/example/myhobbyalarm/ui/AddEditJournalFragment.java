package com.example.myhobbyalarm.ui;

import android.content.DialogInterface;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.data.DatabaseHelperCalendar;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;
import com.example.myhobbyalarm.service.LoadJournalsService;
import com.example.myhobbyalarm.util.ViewUtils;

import java.util.Calendar;

public final class AddEditJournalFragment extends Fragment {

    private TimePicker mTimePicker;
    private EditText mLabel;
    private CheckBox mMon, mTues, mWed, mThurs, mFri, mSat, mSun;
    private String colorTitleSet;


    /**
     * Add for branch DBSnoozeColorAdd 2019,12,11 by YS
     * about isSnooze,colorTitle
     * "ADD VALUE"
     */
    private Switch edit_alarm_snooze;
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

        final View v = inflater.inflate(R.layout.fragment_add_edit_alarm, container, false);

        setHasOptionsMenu(true);

        final Journal journal = getJournal();

        mTimePicker = (TimePicker) v.findViewById(R.id.edit_alarm_time_picker);

        mLabel = (EditText) v.findViewById(R.id.edit_alarm_label);

        mMon = (CheckBox) v.findViewById(R.id.edit_alarm_mon);
        mTues = (CheckBox) v.findViewById(R.id.edit_alarm_tues);
        mWed = (CheckBox) v.findViewById(R.id.edit_alarm_wed);
        mThurs = (CheckBox) v.findViewById(R.id.edit_alarm_thurs);
        mFri = (CheckBox) v.findViewById(R.id.edit_alarm_fri);
        mSat = (CheckBox) v.findViewById(R.id.edit_alarm_sat);
        mSun = (CheckBox) v.findViewById(R.id.edit_alarm_sun);

        for (int i = 0; i < colorTitle_Id.length; i++) {
            colorTitle[i] = (RadioButton) v.findViewById(colorTitle_Id[i]);
        }
        setDayCheckColorTitle(journal);
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

    private void setDayCheckboxes(Alarm alarm) {
        mMon.setChecked(alarm.getDay(Alarm.MON));
        mTues.setChecked(alarm.getDay(Alarm.TUES));
        mWed.setChecked(alarm.getDay(Alarm.WED));
        mThurs.setChecked(alarm.getDay(Alarm.THURS));
        mFri.setChecked(alarm.getDay(Alarm.FRI));
        mSat.setChecked(alarm.getDay(Alarm.SAT));
        mSun.setChecked(alarm.getDay(Alarm.SUN));
    }

    private void setDayCheckColorTitle(Journal journal) {
        if (journal.getColorTitle() == null) {
            journal.setColorTitle("softRed");
        }
        switch (journal.getColorTitle()) {
            case "lightOrange":
                colorTitle[1].setChecked(true);
                break;
            case "softOrange":
                colorTitle[2].setChecked(true);
                break;
            case "slightlyCyan":
                colorTitle[3].setChecked(true);
                break;
            case "slightlyGreen":
                colorTitle[4].setChecked(true);
                break;
            case "green":
                colorTitle[5].setChecked(true);
                break;
            case "strongCyan":
                colorTitle[6].setChecked(true);
                break;
            case "blue":
                colorTitle[7].setChecked(true);
                break;
            case "moderateBlue":
                colorTitle[8].setChecked(true);
                break;
            case "moderateViolet":
                colorTitle[9].setChecked(true);
                break;
            case "black":
                colorTitle[10].setChecked(true);
                break;
            case "softRed":
            default:
                colorTitle[0].setChecked(true);
                break;
        }
    }

    private void save() {
        // donuni widget listView 새로고침!
        if(HomeWidgetListviewProvider.staticAppWidgetIds == null){

        }else{
            HomeWidgetListviewProvider.staticAppWidgetManager.
                    notifyAppWidgetViewDataChanged(HomeWidgetListviewProvider.staticAppWidgetIds,R.id.widget_listview);

        }


        final Journal journal = getJournal();

        final Calendar time = Calendar.getInstance();
        time.set(Calendar.MINUTE, ViewUtils.getTimePickerMinute(mTimePicker));
        time.set(Calendar.HOUR_OF_DAY, ViewUtils.getTimePickerHour(mTimePicker));

        journal.setColorTitle(colorTitleSet);
        Log.d(getClass().getSimpleName(), "setColorTitle colorTitleSet : " + colorTitleSet);


        getActivity().finish();

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

        // donuni widget listView 새로고침!
        if(HomeWidgetListviewProvider.staticAppWidgetIds == null){

        }else{
            HomeWidgetListviewProvider.staticAppWidgetManager.
                    notifyAppWidgetViewDataChanged(HomeWidgetListviewProvider.staticAppWidgetIds,R.id.widget_listview);

        }


    }

//    @Override
//    public void onCheckedChanged(RadioGroup group, int checkedId) {
//        switch (checkedId) {
//            case R.id.edit_alarm_color_softRed:
//                colorTitleSet = "softRed";
//                break;
//            case R.id.edit_alarm_color_lightOrange:
//                colorTitleSet = "lightOrange";
//                break;
//            case R.id.edit_alarm_color_softOrange:
//                colorTitleSet = "softOrange";
//                break;
//            case R.id.edit_alarm_color_slightlyCyan:
//                colorTitleSet = "slightlyCyan";
//                break;
//            case R.id.edit_alarm_color_slightlyGreen:
//                colorTitleSet = "slightlyGreen";
//                break;
//            case R.id.edit_alarm_color_green:
//                colorTitleSet:
//                colorTitleSet = "green";
//                break;
//            case R.id.edit_alarm_color_strongCyan:
//                colorTitleSet = "strongCyan";
//                break;
//            case R.id.edit_alarm_color_blue:
//                colorTitleSet = "blue";
//                break;
//            case R.id.edit_alarm_color_moderateBlue:
//                colorTitleSet = "moderateBlue";
//                break;
//            case R.id.edit_alarm_color_moderateViolet:
//                colorTitleSet = "moderateViolet";
//                break;
//            case R.id.edit_alarm_color_black:
//                colorTitleSet = "black";
//                break;
//        }
//
//        Log.d(getClass().getSimpleName(), "onCheckedChanged colorTitleSet : " + colorTitleSet);
//    }
}
