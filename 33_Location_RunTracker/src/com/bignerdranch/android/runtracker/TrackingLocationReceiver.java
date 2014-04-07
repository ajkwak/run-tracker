package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.location.Location;

/**
 * A {@link LocationReceiver} that inserts the locations it receives into the database for the
 * current run.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class TrackingLocationReceiver extends LocationReceiver {

    @Override
    protected void onLocationReceived(Context c, Location loc) {
        RunManager.get(c).insertLocation(loc);
    }
}
