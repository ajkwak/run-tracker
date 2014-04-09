package com.bignerdranch.android.runtracker;

import android.support.v4.app.Fragment;

/**
 * Activity for displaying the status of a single run.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class RunActivity extends SingleFragmentActivity {
    /**
     * A key for passing a run ID as a long.
     */
    public static final String EXTRA_RUN_ID = "com.bignerdranch.android.runtracker.run_id";

    @Override
    protected Fragment createFragment() {
        long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        if (runId != -1) {
            return RunFragment.newInstance(runId);
        } else {
            return new RunFragment();
        }
    }
}
