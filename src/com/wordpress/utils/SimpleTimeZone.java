package com.wordpress.utils;


import java.util.TimeZone;


public class SimpleTimeZone extends TimeZone {


    public final static String[] TIME_ZONE_IDS = new String[] {
        "+12:00",
        "+11:30",
        "+11:00",
        "+10:30",
        "+10:00",
        "+09:30",
        "+09:00",
        "+08:30",
        "+08:00",
        "+07:30",
        "+07:00",
        "+06:30",
        "+06:00",
        "+05:30",
        "+05:00",
        "+04:30",
        "+04:00",
        "+03:30",
        "+03:00",
        "+02:30",
        "+02:00",
        "+01:30",
        "+01:00",
        "+00:30",
        " 00:00",
        "-00:30",
        "-01:00",
        "-01:30",
        "-02:00",
        "-02:30",
        "-03:00",
        "-03:30",
        "-04:00",
        "-04:30",
        "-05:00",
        "-05:30",
        "-06:00",
        "-06:30",
        "-07:00",
        "-07:30",
        "-08:00",
        "-08:30",
        "-09:00",
        "-09:30",
        "-10:00",
        "-10:30",
        "-11:00",
        "-11:30"
    };

    private int mOffsetIndex;

    private final static int GMT_INDEX = 24;
    private final static int MILLIS_PER_HOUR = 3600000;
    private final static int MILLIS_PER_HALF_HOUR = 1800000;
    private final static int[] OFFSETS = {
        12 * MILLIS_PER_HOUR,
        (11 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        11 * MILLIS_PER_HOUR,
        (10 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        10 * MILLIS_PER_HOUR,
        (9 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        9 * MILLIS_PER_HOUR,
        (8 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        8 * MILLIS_PER_HOUR,
        (7 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        7 * MILLIS_PER_HOUR,
        (6 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        6 * MILLIS_PER_HOUR,
        (5 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        5 * MILLIS_PER_HOUR,
        (4 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        4 * MILLIS_PER_HOUR,
        (3 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        3 * MILLIS_PER_HOUR,
        (2 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        2 * MILLIS_PER_HOUR,
        (1 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        1 * MILLIS_PER_HOUR,
        (0 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        0,
        (0 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -1 * MILLIS_PER_HOUR,
        (1 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -2 * MILLIS_PER_HOUR,
        (2 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -3 * MILLIS_PER_HOUR,
        (3 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -4 * MILLIS_PER_HOUR,
        (4 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -5 * MILLIS_PER_HOUR,
        (5 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -6 * MILLIS_PER_HOUR,
        (6 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -7 * MILLIS_PER_HOUR,
        (7 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -8 * MILLIS_PER_HOUR,
        (8 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -9 * MILLIS_PER_HOUR,
        (9 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -10 * MILLIS_PER_HOUR,
        (10 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR,
        -11 * MILLIS_PER_HOUR,
        (11 * MILLIS_PER_HOUR) + MILLIS_PER_HALF_HOUR
    };
    

    public SimpleTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        int selected = SimpleTimeZone.getIndexForOffset(tz.getRawOffset());
       //#mdebug
        System.out.println("tz.getRawOffset(): "+ tz.getRawOffset());
        System.out.println("tz.getID(): "+ tz.getID());
      //#enddebug
        mOffsetIndex = selected;
    }

    public SimpleTimeZone(int aOffsetIndex) {
        mOffsetIndex = aOffsetIndex;
    }

    public String getID() {
        return TIME_ZONE_IDS[mOffsetIndex];
    }

    public int getOffset(int aEra,
                         int aYear,
                         int aMonth,
                         int aDay,
                         int aDayOfWeek,
                         int aMillis) {
        return OFFSETS[mOffsetIndex];
    }

    public int getRawOffset() {
        return OFFSETS[mOffsetIndex];
    }

    public boolean useDaylightTime() {
        return false;
    }

    public static int getIndexForOffset(int aOffset) {
        for (int i = 0; i < OFFSETS.length; i++) {
            if (aOffset == OFFSETS[i]) {
                return i;
            }
        }

        return GMT_INDEX;
    }
}

