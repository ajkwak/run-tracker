package com.bignerdranch.android.runtracker;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

/**
 * Creates and upgrades a database for storing {@link Run}s and the locations associated with
 * {@link Run}s.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class RunDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "runs.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_RUN = "run";
    private static final String COLUMN_RUN_ID = "_id";
    private static final String COLUMN_RUN_START_DATE = "start_date";

    private static final String TABLE_LOCATION = "location";
    private static final String COLUMN_LOCATION_LATITUDE = "latitude";
    private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
    private static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
    private static final String COLUMN_LOCATION_PROVIDER = "provider";
    private static final String COLUMN_LOCATION_RUN_ID = "run_id";

    /**
     * Creates a new {@code RunDatabaseHelper} with the given context.
     *
     * @param context the context to use
     */
    public RunDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the "run" table.
        db.execSQL("create table run (_id integer primary key autoincrement, start_date integer)");
        // Create the "location" table.
        db.execSQL("create table location (" +
                " timestamp integer, latitude real, longitude real, altitude real," +
                " provider varchar(100), run_id integer references run(_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement schema changes and data massage here when upgrading.
    }

    /**
     * Inserts the given run into the database.
     *
     * @param run the run to insert
     * @return the ID of the row at which the run was inserted into the database, or {@code -1} if
     *         an error occurred
     */
    public long insertRun(Run run) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
        return getWritableDatabase().insert(TABLE_RUN, null, cv);
    }

    /**
     * Inserts the given location into the database as a location from the given run.
     *
     * @param runId the ID of the run associated with this location
     * @param location the location to insert
     * @return the ID of the row at which the location was inserted into the database, or {@code -1}
     *         if an error occurred
     */
    public long insertLocation(long runId, Location location) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        cv.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
        cv.put(COLUMN_LOCATION_TIMESTAMP, location.getTime());
        cv.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
        cv.put(COLUMN_LOCATION_RUN_ID, runId);
        return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
    }

    /**
     * Gets a cursor for all of the runs currently in the database.
     *
     * @return a cursor for the runs currently stored in the Run database
     */
    public RunCursor queryRuns() {
        // Equivalent to "select * from run order by start_date asc".
        Cursor wrapped = getReadableDatabase().query(TABLE_RUN,
                null, null, null, null, null, COLUMN_RUN_START_DATE + " asc");
        return new RunCursor(wrapped);
    }

    /**
     * Gets a cursor for the run with the given ID in the database.
     *
     * @param id the ID of the run to retrieve
     * @return a cursor for the run with the given ID in the database
     */
    public RunCursor queryRun(long id) {
        Cursor wrapped = getReadableDatabase().query(TABLE_RUN,
                null, // All columns
                COLUMN_RUN_ID + " = ?", // Look for a run ID.
                new String[] { String.valueOf(id) }, // With this value
                null, // Group by
                null, // Order by
                null, // Having
                "1"); // Limit = 1 row
        return new RunCursor(wrapped);
    }

    /**
     * Gets a cursor for the last location associated with the given run.
     *
     * @param runId the ID of the run to query
     * @return the last location associated with the given run
     */
    public LocationCursor queryLastLocationForRun(long runId) {
        Cursor wrapped = getReadableDatabase().query(TABLE_LOCATION,
                null, // All columns
                COLUMN_LOCATION_RUN_ID + " = ?", // Limit to the given run
                new String[]{ String.valueOf(runId) },
                null, // Group by
                null, // Having
                COLUMN_LOCATION_TIMESTAMP + " desc", // Order by latest first
                "1"); // Limit = 1 row
        return new LocationCursor(wrapped);
    }

    /**
     * A convenience class to wrap a cursor that returns rows from the "run" table. The
     * {@link #getRun()} method will give you a Run instance representing the current row.
     */
    public static class RunCursor extends CursorWrapper {

        /**
         * Creates a {@code RunCursor} wrapping the given cursor.
         *
         * @param c the cursor to wrap
         */
        public RunCursor(Cursor c) {
            super(c);
        }

        /**
         * Returns a Run object configured for the current row, or null if the current row is invalid.
         */
        public Run getRun() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            Run run = new Run();
            run.setId(getLong(getColumnIndex(COLUMN_RUN_ID)));
            run.setStartDate(new Date(getLong(getColumnIndex(COLUMN_RUN_START_DATE))));
            return run;
        }
    }

    /**
     * A convenience class to wrap a cursor that returns rows from the "location" table. The
     * {@link #getLocation()} method will give you a Location instance representing the current row.
     */
    public static class LocationCursor extends CursorWrapper {

        /**
         * Creates a {@code LocationCursor} wrapping the given cursor.
         *
         * @param c the cursor to wrap
         */
        public LocationCursor(Cursor c) {
            super(c);
        }

        /**
         * Returns a Location object configured for the current row, or null if the current row is
         * invalid.
         */
        public Location getLocation() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            // First get the provider out so we can use the constructor.
            String provider = getString(getColumnIndex(COLUMN_LOCATION_PROVIDER));
            Location loc = new Location(provider);
            // Populate the remaining properties.
            loc.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_LONGITUDE)));
            loc.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_LATITUDE)));
            loc.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_ALTITUDE)));
            loc.setTime(getLong(getColumnIndex(COLUMN_LOCATION_TIMESTAMP)));
            return loc;
        }
    }

}
