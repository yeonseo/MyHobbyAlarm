package com.example.myhobbyalarm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhobbyalarm.R;

import static com.example.myhobbyalarm.ui.AddEditJournalActivity.ADD_JOURNAL;
import static com.example.myhobbyalarm.ui.AddEditJournalActivity.buildAddEditJournalActivityIntent;

public final class AlarmLandingPageFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_alarm_landing_page, container, false);

        final Button launchMainActivityBtn = (Button) v.findViewById(R.id.load_main_activity_btn);
        final Button calendar_btn = (Button) v.findViewById(R.id.calendar_btn);
        final Button dismiss = (Button) v.findViewById(R.id.dismiss_btn);

        launchMainActivityBtn.setOnClickListener(this);
        calendar_btn.setOnClickListener(this);
        dismiss.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            //메인가기
            case R.id.load_main_activity_btn:
                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
                break;
            //기록하기로 수정 예정
            case R.id.calendar_btn:
                final Intent i = buildAddEditJournalActivityIntent(getContext(), ADD_JOURNAL);
                startActivity(i);
//                startActivity(new Intent(getContext(), AddEditJournalActivity.class));
                getActivity().finish();
                break;
                //취소
            case R.id.dismiss_btn:
                getActivity().finish();
                break;
        }

    }
}
