package com.android.lm.tasker.utils;

/**
 * Created by Lara on 6/22/2016.
 */
public class Task {
    private String mName;
    private String mNotes;
    private boolean mPriority;
    private long mDateNum;
    private String mDate;
    private long mDbRowId;
    private boolean mIsChecked;

    public Task(String name, String notes, boolean priority, long dateNum, String date,
                long dbRowId, boolean status) {
        super();
        this.mName = name;
        this.mNotes = notes;
        this.mPriority = priority;
        this.mDateNum = dateNum;
        this.mDate = date;
        this.mDbRowId = dbRowId;

        this.mIsChecked = status;
    }

    public Task(String name, String notes, boolean priority, long dateNum, String date) {
        super();
        this.mName = name;
        this.mNotes = notes;
        this.mPriority = priority;
        this.mDateNum = dateNum;
        this.mDate = date;

        this.mIsChecked = false;
    }

    public String getName() {
        return mName;
    }
    public String getNotes() {
        return mNotes;
    }
    public boolean getPriority() {
        return mPriority;
    }
    public long getDateNum() { return mDateNum; }
    public String getDate() {
        return mDate;
    }
    public boolean getIsChecked() {return mIsChecked; }
    public long getDbRowId() { return mDbRowId; }

    public void setName(String newName) {
        mName = newName;
    }
    public void setNotes(String newNotes) {
        mNotes = newNotes;
    }
    public void setPriority(boolean newPriority) {
        mPriority = newPriority;
    }
    public void setDateNum(long newDateNum) { mDateNum = newDateNum; }
    public void setDate(String newDate) {
        mDate = newDate;
    }
    public void setIsChecked(boolean newIsChecked) { mIsChecked = newIsChecked; }
    public void setDbRowId(long newDbRowId) { mDbRowId = newDbRowId; }
}
