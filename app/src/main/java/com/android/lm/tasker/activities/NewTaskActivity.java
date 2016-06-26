package com.android.lm.tasker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.lm.tasker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Lara on 6/14/2016.
 */

public class NewTaskActivity extends AppCompatActivity {
    private EditText mEtName;
    private EditText mEtNotes;
    private CheckBox mCbHighPriority;
    private CalendarView mCvDate;
    private TextView mTvDateDisplay;

    private String mTaskName;
    private String mTaskNotes;
    private boolean mTaskPriority;
    private long mTaskDateNum;
    private String mTaskDate;
    private TextView mTvErrorMsg;

    private Button mBtnDate;
    private boolean mCalendarVisible;

    private Calendar mDate;

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    static final String[] MYMONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.task_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get the design elements.
        mEtName = (EditText) findViewById(R.id.etName);
        mEtNotes = (EditText) findViewById(R.id.etNotes);
        mCbHighPriority = (CheckBox) findViewById(R.id.cbHighPriority);
        mTvDateDisplay = (TextView) findViewById(R.id.tvDateDisplay);
        mCvDate = (CalendarView) findViewById(R.id.cvDate);
        mTvErrorMsg = (TextView) findViewById(R.id.tvErrorMsg);
        mBtnDate = (Button) findViewById(R.id.btnDate);
        mCalendarVisible = false;

        // Initially define taskDateNum and taskDate.
        // They may get overridden below.
        mTaskDateNum = Calendar.getInstance().getTimeInMillis();
        mTaskDate = DATE_FORMAT.format(mTaskDateNum);

        registerListeners();
        loadData();

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadData() {
        if(getIntent() != null) {
            // If an existing task is being edited, get and display the data.

            // Get the data.
            Bundle extras = getIntent().getExtras();
            mTaskName = extras != null ? extras.getString("existingName") : "";
            mTaskNotes = extras != null ? extras.getString("existingNotes") : "";
            mTaskPriority = extras != null && extras.getBoolean("existingPriority");
            mTaskDateNum = extras != null ? extras.getLong("existingDateNum") : 0L;
            mTaskDate = extras != null ? extras.getString("existingDate") : "";

            // Set the attributes of the design elements.
            mEtName.setText(mTaskName);
            mEtNotes.setText(mTaskNotes);
            mCbHighPriority.setChecked(mTaskPriority);

            if(mTaskDateNum != 0L) {
                // Show calendar, select current date, switch to "clear" button.
                mCalendarVisible = true;
                mTvDateDisplay.setText(mTaskDate);
                mCvDate.setVisibility(View.VISIBLE);
                mCvDate.setDate(mTaskDateNum);
                mBtnDate.setText(getResources().getString(R.string.btnDate_clear));
            }
        }
    }

    private void registerListeners() {
        // For updating the date when the CalendarView fires:
        mCvDate.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day) {
                mTaskDate = MYMONTHS[month] + " " + day + ", " + year;
                mTvDateDisplay.setText(mTaskDate);

                Calendar myCal = Calendar.getInstance();
                myCal.set(year, month, day);
                mTaskDateNum = myCal.getTimeInMillis();

                // Currently can't get CalendarView getDate() method to work ...
                // only ever returns the current date.
            }
        });

        // Control calendar display:
        mBtnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCalendarVisible) {
                    // Clear date, hide calendar, switch to "add" button.
                    mCalendarVisible = false;
                    mTaskDateNum = 0L;
                    mTaskDate = "";
                    mTvDateDisplay.setText(getResources().getString(R.string.empty_string));
                    mCvDate.setVisibility(View.GONE);
                    mBtnDate.setText(getResources().getString(R.string.btnDate_add));
                } else {
                    // Show calendar, select current date, switch to "clear" button.
                    mCalendarVisible = true;
                    mDate = Calendar.getInstance();
                    mTaskDateNum = mDate.getTimeInMillis();
                    mTaskDate = DATE_FORMAT.format(mDate.getTime());
                    mTvDateDisplay.setText(mTaskDate);
                    mCvDate.setDate(mTaskDateNum);
                    mCvDate.setVisibility(View.VISIBLE);
                    mBtnDate.setText(getResources().getString(R.string.btnDate_clear));
                }
            }
        });
    }

    public void onDone(View v) {
        mTaskName = mEtName.getText().toString();
        mTaskName = mTaskName.trim();

        if(mTaskName.trim().isEmpty()) {
            // Show message saying name is required.
            mTvErrorMsg.setText(R.string.taskNameNotOk);
            mTvErrorMsg.setTextColor(Color.RED);
        } else {
            mTaskNotes = mEtNotes.getText().toString();
            mTaskNotes = !mTaskNotes.isEmpty() ? mTaskNotes : "";
            mTaskPriority = mCbHighPriority.isChecked();

            Intent resultIntent = new Intent(); //NewTaskActivity.this, MainActivity.class);
            resultIntent.putExtra("taskName", mTaskName);
            resultIntent.putExtra("taskNotes", mTaskNotes);
            resultIntent.putExtra("taskPriority", mTaskPriority);
            resultIntent.putExtra("taskDateNum", mTaskDateNum);
            resultIntent.putExtra("taskDate", mTaskDate);

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    public void onCancel(View v) {
        // Do nothing. Return to main activity.
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Currently using buttons instead of menu items.

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.new_task_menu, menu);
        return true;
    }
}


