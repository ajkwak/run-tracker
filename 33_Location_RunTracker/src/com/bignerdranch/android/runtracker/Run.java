package com.bignerdranch.android.runtracker;

import java.util.Date;

/**
 * A class representing a single run being tracked by this application.
 *
 * @author Bill Phillips
 * @author Brian Hardy
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class Run {
    private long mId;
    private Date mStartDate;

    /**
     * Creates a new {@code Run} with an invalid ID. By default, the start date of the newly created
     * run is set to the time at which this constructor is called.
     */
    public Run() {
        mId = -1;
        mStartDate = new Date();
    }

    /**
     * Gets the ID of this run.
     *
     * @return the ID of this run
     */
    public long getId() {
        return mId;
    }

    /**
     * Sets the unique ID of this run to the given value.
     *
     * @param id the ID to set
     */
    public void setId(long id) {
        mId = id;
    }

    /**
     * Gets the start date of this run.
     *
     * @return the start date of this run
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Sets the start date of this run to the given value.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    /**
     * Gets the duration of this run, in milliseconds, with the given run end time.
     *
     * @param endMillis the end time of the run (in millisecods)
     * @return the duration of the run (in milliseconds)
     */
    public int getDurationSeconds(long endMillis) {
        return (int)((endMillis - mStartDate.getTime()) / 1000);
    }

    /**
     * Formats the given run duration as an easily human-readable string with the format
     * {@code HH:MM:SS} (where {@code HH} is the number of hours in the run, {@code MM} is the
     * number of minutes in the run, and {@code SS} is the number of seconds in the run).
     *
     * @param durationSeconds the duration of the run, in seconds
     * @return the string representation of the run duration, formatted as
     *         [hours]:[minutes]:[seconds]
     */
    public static String formatDuration(int durationSeconds) {
        int seconds = durationSeconds % 60;
        int minutes = ((durationSeconds - seconds) / 60) % 60;
        int hours = (durationSeconds - (minutes * 60) - seconds) / 3600;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
