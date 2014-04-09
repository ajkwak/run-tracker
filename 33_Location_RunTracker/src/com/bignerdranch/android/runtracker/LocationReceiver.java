package com.bignerdranch.android.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * A {@link BroadcastReceiver} that receives broadcasts about location information (like when the
 * location of the android tablet has changed, during a run, e.g.)
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Location loc = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (loc != null) {
            onLocationReceived(context, loc);
            return;
        }
        // If we get here, something else has happened.
        if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
            boolean enabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled);
        }
    }

    /**
     * The method that is called when the {@code LocationReceiver} receives a location broadcast.
     *
     * @param context the context in which the receiver is running
     * @param loc the location that was received
     */
    protected void onLocationReceived(Context context, Location loc) {
        Log.d(TAG, this + " Got location from " + loc.getProvider() + ": " + loc.getLatitude()
                + ", " + loc.getLongitude());
    }

    /**
     * The method that is called when a provider for the location broadcasts is enabled/disabled.
     *
     * @param enabled {@code true} if the provider has been enabled; {@code false} if the provider
     *        has been disabled
     */
    protected void onProviderEnabledChanged(boolean enabled) {
        Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
    }
}
