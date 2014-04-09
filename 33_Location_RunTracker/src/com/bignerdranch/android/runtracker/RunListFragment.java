package com.bignerdranch.android.runtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.RunDatabaseHelper.RunCursor;

/**
 * Fragment for displaying a list of runs taken and recorded.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class RunListFragment extends ListFragment {
    private static final String TAG = "RunListFragment";
    private static final int REQUEST_NEW_RUN = 0;
    private static final int VIEW_CURRENT_RUN = 1;

    private RunCursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Query the list of runs.
        mCursor = RunManager.get(getActivity()).queryRuns();

        // Create an adapter to point at this cursor.
        RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mCursor);
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "create options menu");
        inflater.inflate(R.menu.run_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_item_new_run:
            Log.d(TAG, "Requested new run");
            Intent i = new Intent(getActivity(), RunActivity.class);
            startActivityForResult(i, REQUEST_NEW_RUN);
            return true;
        case R.id.menu_item_run_status:
            AlertDialog runStatusDialog = createRunStatusDialog();
            runStatusDialog.show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog createRunStatusDialog() {
        FragmentActivity runListActivity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(runListActivity);
        builder.setTitle(R.string.run_status);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, null);

        // Create the dialog's message:
        RunManager runManager = RunManager.get(runListActivity);
        Run currentRun = runManager.getCurrentRun();
        String currentRunString = currentRun == null
                ? "<<NONE>>"
                : getString(R.string.cell_text, currentRun.getStartDate());
        String currentRunStatusString = runManager.isTrackingRun()
                ? getString(R.string.run_status_started)
                : getString(R.string.run_status_stopped);
        builder.setMessage(getString(R.string.run_status_dialog_text, currentRunString,
                currentRunStatusString));

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_NEW_RUN == requestCode) {
            mCursor.requery();
            ((RunCursorAdapter) getListAdapter()).notifyDataSetChanged();
        } else if (VIEW_CURRENT_RUN == requestCode) {
            ((RunCursorAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // The id argument will be the Run ID; CursorAdapter gives us this for free.
        Log.d(TAG, "Clicked on run with ID = " + id);
        Intent i = new Intent(getActivity(), RunActivity.class);
        i.putExtra(RunActivity.EXTRA_RUN_ID, id);
        startActivityForResult(i, VIEW_CURRENT_RUN);
    }

    private static class RunCursorAdapter extends CursorAdapter {
        private RunCursor mRunCursor;

        public RunCursorAdapter(Context context, RunCursor cursor) {
            super(context, cursor, 0);
            mRunCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Use a layout inflater to get a row view.
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Get the run for the current row.
            Run run = mRunCursor.getRun();

            // Set up the start date text view.
            TextView startDateTextView = (TextView) view;
            String cellText = context.getString(R.string.cell_text, run.getStartDate());
            if (RunManager.get(context).isTrackingRun(run)) {
                cellText = "[Tracking] " + cellText;
                startDateTextView.setTextColor(0xFF00CC00 /* green */);
            } else {
                startDateTextView.setTextColor(0xFF000000 /* black */);
            }
            startDateTextView.setText(cellText);
        }
    }
}
