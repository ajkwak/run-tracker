package com.bignerdranch.android.runtracker;

import android.support.v4.app.Fragment;

/**
 * Activity for displaying a list of runs.
 *
 * @author Brian Hardy
 * @author Bill Phillips
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class RunListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
