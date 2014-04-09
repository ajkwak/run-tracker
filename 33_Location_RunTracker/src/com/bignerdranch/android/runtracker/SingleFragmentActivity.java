package com.bignerdranch.android.runtracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * An activity with a single fragment.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public abstract class SingleFragmentActivity extends FragmentActivity {
    protected static final String FRAGMENT_TAG = "SingleFragmentActivity.Fragment";
    private static final String TAG = "SingleFragmentActivity";

    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called for " + this.getClass().getCanonicalName());
        FrameLayout fl = new FrameLayout(this);
        fl.setId(R.id.fragmentContainer);
        setContentView(fl);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            Log.d(TAG, "creating new fragment for activity");
            fragment = createFragment();
            int result = manager.beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit();
            Log.d(TAG, "result of adding fragment = " + result);
            fragment = manager.findFragmentById(R.id.fragmentContainer);
            if (fragment != null) {
                Log.d(TAG, "successfully added fragment to FragmentManager");
            } else {
                Log.d(TAG, "failed to add fragment to FragmentManager");
            }
        } else {
            Log.d(TAG, "reusing current fragment for activity");
        }
    }
}
