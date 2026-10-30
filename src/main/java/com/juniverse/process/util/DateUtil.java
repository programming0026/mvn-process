package com.juniverse.process.util;

import java.time.Year;

public final class DateUtil {
    /**
     * Instantiates a new date util.
     */
    private DateUtil() {
    }
    /**
     * Gets the year.
     *
     * @return the year
     */
    public static int getYear() {
        return Year.now().getValue();
    }
}
