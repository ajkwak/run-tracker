package com.bignerdranch.android.runtracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.bignerdranch.android.runtracker.RunDatabaseHelper.LocationCursor;
import com.bignerdranch.android.runtracker.RunDatabaseHelper.RunCursor;

/**
 * Singleton class that manages the communication with the {@link LocationManager} and details about
 * the current run.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class RunManager {
    private static final String TAG = "RunManager";

    private static final String PREFS_FILE = "runs";
    private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";

    public static final String ACTION_LOCATION = "com.bignerdranch.android.runtracker.ACTION_LOCATION";

    private static final String TEST_PROVIDER = "TEST_PROVIDER";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;
    private RunDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentRunId;

    private RunManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
        mHelper = new RunDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentRunId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
    }

    /**
     * Gets the singleton {@code RunManager} instance. If no instance currently exists, creates a
     * new {@link RunManager} instance for the given context.
     *
     * @param c the context for the {@code RunManager}
     * @return the current {@code RunManager} instance
     */
    public static RunManager get(Context c) {
        if (sRunManager == null) {
            // We use the application context to avoid leaking activities.
            sRunManager = new RunManager(c.getApplicationContext());
        }
        return sRunManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    /**
     * Request to start receiving location updates from the {@link LocationManager}.
     */
    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;
        // If we have the test provider and it's enabled, use it.
        if (mLocationManager.getProvider(TEST_PROVIDER) != null &&
                mLocationManager.isProviderEnabled(TEST_PROVIDER)) {
            provider = TEST_PROVIDER;
        }
        Log.d(TAG, "Using provider " + provider);

        // Get the last known location and broadcast it if we have one.
        Location lastKnown = mLocationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
            // Reset the time to now.
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }

        // Start updates from the location manager.
        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
    }

    /**
     * Request to stop receiving location updates from the {@link LocationManager}.
     */
    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    /**
     * Determine whether a {@link Run} is currently being tracked.
     *
     * @return {@code true}, if a run is currently being tracked; otherwise {@code false}
     */
    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

    /**
     * Determine whether the given {@link Run} is being currently tracked by the {@code RunManager}.
     *
     * @param run the run to check
     * @return {@code true} if the given {@link Run} is currently being tracked; otherwise
     *         {@code false}
     */
    public boolean isTrackingRun(Run run) {
        return run != null && run.getId() == mCurrentRunId;
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }

    /**
     * Start a new run, and begin tracking (receiving current location updates for) that run.
     *
     * @return the newly started run
     */
    public Run startNewRun() {
        // Insert a run into the dB.
        Run run = insertRun();

        // Start tracking the run.
        startTrackingRun(run);
        return run;
    }

    /**
     * Begin tracking the current location as part of the given run.
     *
     * @param run the run to track
     */
    public void startTrackingRun(Run run) {
        mCurrentRunId = run.getId(); // Keep the ID.

        // Store the ID in shared preferences.
        mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).commit();

        // Start location updates.
        startLocationUpdates();
    }

    /**
     * Stop the current run.
     */
    public void stopRun() {
        stopLocationUpdates();
        mCurrentRunId = -1;
        mPrefs.edit().remove(PREF_CURRENT_RUN_ID).commit();
    }

    private Run insertRun() {
        Run run = new Run();
        run.setId(mHelper.insertRun(run));
        return run;
    }

    /**
     * Gets a cursor for all of the runs currently in the database.
     *
     * @return a cursor for the runs currently stored in the Run database
     */
    public RunCursor queryRuns() {
        return mHelper.queryRuns();
    }

    /**
     * Gets the run with the given ID, if any exists
     *
     * @param id the ID of the run to get
     * @return the run with the given ID, if such exists; otherwise {@code null}
     */
    public Run getRun(long id) {
        Run run = null;
        RunCursor cursor = mHelper.queryRun(id);
        cursor.moveToFirst();

        // If we got a row, get a run.
        if (!cursor.isAfterLast()) {
            run = cursor.getRun();
        }
        cursor.close();
        return run;
    }

    /**
     * Gets the current run, if there is one
     *
     * @return the current run, or {@code null}, if no current run exists
     */
    public Run getCurrentRun() {
        return getRun(mCurrentRunId);
    }

    /**
     * Inserts the given location into the database as part of the current run.
     *
     * @param loc the location to insert
     */
    public void insertLocation(Location loc) {
        if (mCurrentRunId != -1) {
            mHelper.insertLocation(mCurrentRunId, loc);
        } else {
            Log.e(TAG, "Location received with no tracking run; ignoring.");
        }
    }

    /**
     * Gets the last location associated with the run with the given ID
     *
     * @param runId the ID of the run to query
     * @return the last location of the run with the given ID, if such exists
     */
    public Location getLastLocationForRun(long runId) {
        Location location = null;
        LocationCursor cursor = mHelper.queryLastLocationForRun(runId);
        cursor.moveToFirst();

        // If we got a row, get a location.
        if (!cursor.isAfterLast()) {
            location = cursor.getLocation();
        }
        cursor.close();
        return location;
    }
}