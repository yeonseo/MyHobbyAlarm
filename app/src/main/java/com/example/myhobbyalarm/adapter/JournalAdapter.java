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

import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.model.Journal;
import com.example.myhobbyalarm.ui.AddEditAlarmActivity;
import com.example.myhobbyalarm.ui.AddEditJournalActivity;
import com.example.myhobbyalarm.util.AlarmUtils;
import com.example.myhobbyalarm.util.JournalUtils;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    private static final String TAG = "JournalsAdapter";

    private List<Journal> mJournals;

    private int mAccentColor = -1;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context c = parent.getContext();
        final View v = LayoutInflater.from(c).inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Context c = holder.itemView.getContext();

        if (mAccentColor == -1) {
            mAccentColor = ContextCompat.getColor(c, R.color.colorPrimary);
        }

        final Journal journal = mJournals.get(position);

        holder.day.setText(journal.getDay());
        holder.content.setText(journal.getContent());

        Log.d(TAG, journal.toString());
        int colorSet = R.color.softRed;
        if (journal.getColorTitle() == null) journal.setColorTitle("softRed");
        Log.d(TAG, "if after : " + journal.toString());
        switch (journal.getColorTitle()) {
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
            holder.content.setBackgroundTintList(stateList);
        } else {
            holder.content.getBackground().getCurrent().setColorFilter(
                    new PorterDuffColorFilter(c.getResources().getColor(colorSet), PorterDuff.Mode.MULTIPLY));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context c = view.getContext();
                final Intent launchEditJournalIntent =
                        AddEditJournalActivity.buildAddEditJournalActivityIntent(
                                c, AddEditJournalActivity.EDIT_JOURNAL
                        );
                launchEditJournalIntent.putExtra(AddEditJournalActivity.JOURNAL_EXTRA, journal);
                c.startActivity(launchEditJournalIntent);
                Log.d(TAG, "itemView.setOnClickListener");
            }
        });

    }

    @Override
    public int getItemCount() {
        return (mJournals == null) ? 0 : mJournals.size();
    }

    public void setJournals(List<Journal> journals) {
        Log.d(TAG, "setJournals");
        mJournals = journals;
        notifyDataSetChanged();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        final TextView day, content;

        ViewHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.jr_day);
            content = itemView.findViewById(R.id.jr_content);

        }
    }
}
