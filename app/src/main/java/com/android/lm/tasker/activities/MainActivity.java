package com.android.lm.tasker.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.lm.tasker.R;
import com.android.lm.tasker.adapters.TaskAdapter;
import com.android.lm.tasker.utils.Task;
import com.android.lm.tasker.utils.TaskDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static final int NEW_TASK_CODE = 1;
    static final int EDIT_TASK_CODE = 2;

    final static String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

    private ArrayList<Task> mTaskData;
    private TaskAdapter mAdapter;
    private ListView mListView;
    private TaskDbHelper mDbHelper;     // Database helper
    private Task mSelectedTask;          // Keep track of currently selected task

    // Still need to deal with task name and notes that are "too long".

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Create database helper and retrieve saved tasks from database:
        mDbHelper = new TaskDbHelper(this);
        mTaskData = mDbHelper.readAllTasks();

        // Retrieve list view:
        mListView = (ListView) findViewById(R.id.lvTasks);

        // Create adapter and set list view:
        mAdapter = new TaskAdapter(this, R.layout.task_row_layout, mTaskData);
        mAdapter.setNotifyOnChange(true);
        mListView.setAdapter(mAdapter);

        setupListListeners();
        //registerForContextMenu(listView);
    }

    private void setupListListeners() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long id) {
                mSelectedTask = (Task) adapter.getItemAtPosition(pos);

                // Creating the instance of PopupMenu:
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.selected_task_menu, popup.getMenu());

                // Register popup with OnMenuItemClickListener:
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.popup_menu_edit:
                                // Make sure that the response code knows the db row id of this task:
                                Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                                intent.putExtra("existingName", mSelectedTask.getName());
                                intent.putExtra("existingNotes", mSelectedTask.getNotes());
                                intent.putExtra("existingPriority", mSelectedTask.getPriority());
                                intent.putExtra("existingDateNum", mSelectedTask.getDateNum());
                                intent.putExtra("existingDate", mSelectedTask.getDate());

                                MainActivity.this.startActivityForResult(intent, EDIT_TASK_CODE);
                                return true;

                            case R.id.popup_menu_delete:
                                new DeleteExistingTask(mSelectedTask).execute();
                                return true;
                        }

                        return true;
                    }
                });

                // Show popup menu:
                popup.show();

                return false;
            }
        });
    }


    // Menu at the top of the main activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
//            case R.id.menu_main_edit:
////                if(selectedTaskPos > -1) {
////                    Intent intent = new Intent(this, NewTaskActivity.class);
////                    intent.putExtra("existingName", selectedTask.getName());
////                    intent.putExtra("existingNotes", selectedTask.getNotes());
////                    intent.putExtra("existingPriority", selectedTask.getPriority());
////                    intent.putExtra("existingDate", selectedTask.getDate());
////                    this.startActivityForResult(intent, EDIT_TASK_CODE);
////                }
//                break;
            case R.id.menu_main_add:
                Intent intent = new Intent(this, NewTaskActivity.class);
                this.startActivityForResult(intent, NEW_TASK_CODE);
                break;
//            case R.id.menu_main_save:
//                // another startActivity, this is for item with id "menu_item2"
//                break;
//            case R.id.menu_main_action_settings:
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    // For when the NewTaskActivity finishes.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case(EDIT_TASK_CODE) : {
                if(resultCode == Activity.RESULT_OK) {
                    // Extract the data returned from the child Activity:
                    if(getIntent() != null) {
                        //!!!!!!!!! NOT COMPLETELY DONE. MAYBE, if an extra is
                        // not found, just don't change the field.
                        Bundle extras = data.getExtras();
                        String taskName = extras != null ? extras.getString("taskName") : "";
                        String taskNotes = extras != null ? extras.getString("taskNotes") : "";
                        boolean taskPriority = extras != null && extras.getBoolean("taskPriority");
                        Long taskDateNum = extras != null ? extras.getLong("taskDateNum") : 0L;
                        String taskDate = extras != null ? extras.getString("taskDate") : "";
                        boolean taskStatus = extras != null && extras.getBoolean("taskStatus");

                        // Update the task in the list view.
                        mSelectedTask.setName(taskName);
                        mSelectedTask.setNotes(taskNotes);
                        mSelectedTask.setPriority(taskPriority);
                        mSelectedTask.setDateNum(taskDateNum);
                        mSelectedTask.setDate(taskDate);
                        mSelectedTask.setIsChecked(taskStatus);

                        mAdapter.notifyDataSetChanged();
                        new UpdateExistingTask(mSelectedTask).execute();
                    }
                }
                break;
            }
            case(NEW_TASK_CODE) : {
                if(resultCode == Activity.RESULT_OK) {
                    // Extract the data returned from the child Activity:
                    if(getIntent() != null) {
                        Bundle extras = data.getExtras();
                        String taskName = extras != null ? extras.getString("taskName") : "";
                        String taskNotes = extras != null ? extras.getString("taskNotes") : "";
                        boolean taskPriority = extras != null && extras.getBoolean("taskPriority");
                        Long taskDateNum = extras != null ? extras.getLong("taskDateNum") : 0L;
                        String taskDate = extras != null ? extras.getString("taskDate") : "";
                        //boolean taskStatus = extras != null && extras.getBoolean("taskStatus");

                        Task newTask = new Task(taskName, taskNotes, taskPriority, taskDateNum, taskDate);
                        new SaveNewTask(newTask).execute();
                    }
                }
                break;
            }
        }
    }

    public void onPause() {
        super.onPause();

        // Get all current task statuses.
        mDbHelper.updateAllTaskStatus(mTaskData);
    }


    private class SaveNewTask extends AsyncTask {
        private ProgressDialog progressDialog;
        private Task newTask;
        private long newRowId;

        public SaveNewTask(Task newTask) {
            this.newTask = newTask;
        }

        @Override
        protected Object doInBackground(final Object... objects) {
            try {
                Thread.sleep(0);
                newRowId = mDbHelper.createTask(newTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                public void run() {
                    progressDialog = ProgressDialog.show(
                            MainActivity.this, "Save", "Saving task, please wait", true);
                }
            });
        }

        @Override
        protected void onPostExecute(Object object) {
            super.onPostExecute(object);
            progressDialog.dismiss();

            // Set rowId for new task then display it:
            newTask.setDbRowId(newRowId);
            mTaskData.add(0, newTask);
            mAdapter.notifyDataSetChanged();
        }
    }


    private class UpdateExistingTask extends AsyncTask {
        private ProgressDialog progressDialog;
        private Task updatedTask;
        private int status;

        public UpdateExistingTask(Task updatedTask) {
            this.updatedTask = updatedTask;
            this.status = 0;
        }

        @Override
        protected Object doInBackground(final Object... objects) {
            try {
                Thread.sleep(0);
                status = mDbHelper.updateTask(updatedTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                public void run() {
                    progressDialog = ProgressDialog.show(
                            MainActivity.this, "Update", "Updating task, please wait", true);
                }
            });
        }

        @Override
        protected void onPostExecute(Object object) {
            super.onPostExecute(object);
            progressDialog.dismiss();
        }
    }


    private class DeleteExistingTask extends AsyncTask {
        private ProgressDialog progressDialog;
        private Task currentTask;

        public DeleteExistingTask(Task currentTask) {
            this.currentTask = currentTask;
        }

        @Override
        protected Object doInBackground(final Object... objects) {
            try {
                Thread.sleep(0);
                mDbHelper.deleteTask(currentTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                public void run() {
                    progressDialog = ProgressDialog.show(
                            MainActivity.this, "Delete", "Deleting task, please wait", true);
                }
            });
        }

        @Override
        protected void onPostExecute(Object object) {
            super.onPostExecute(object);
            progressDialog.dismiss();

            MainActivity.this.mAdapter.remove(currentTask);
            MainActivity.this.mAdapter.notifyDataSetChanged();
        }
    }
}
