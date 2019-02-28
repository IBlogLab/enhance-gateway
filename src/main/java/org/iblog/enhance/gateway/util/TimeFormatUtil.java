package org.iblog.enhance.gateway.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Optional;

/**
 * @author lance
 */
public class TimeFormatUtil {

    public static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");
    public static final ZoneId ZONE_ID_CST = ZoneId.of("Asia/Shanghai");
    public static final ZoneId ZONE_ID_PST = ZoneId.of("America/Los_Angeles");

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_FORMAT);

    public static String timeInCST(long millis) {
        return timeInCST(millis, DEFAULT_FORMATTER);
    }

    public static String timeInCST(long millis, DateTimeFormatter formatter) {
        ZonedDateTime cst = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(millis), ZONE_ID_CST);
        return formatter.format(cst);
    }

    public static ZonedDateTime dateTimeInCST(long millis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID_CST);
    }

    public static ZonedDateTime dateTimeInUTC(long millis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE_ID_UTC);
    }

    public static String timeInUTC(long millis) {
        return timeInUTC(millis, DEFAULT_FORMATTER);
    }

    public static String timeInUTC(long millis, DateTimeFormatter formatter) {
        ZonedDateTime utc = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(millis), ZONE_ID_UTC);
        return formatter.format(utc);
    }

    public static long mustParseCST(String ts) {
        Optional<Long> r = parseCST(ts);
        if (!r.isPresent()) {
            throw new IllegalArgumentException("Unable to parse timestamp " + ts);
        }
        return r.get();
    }

    public static long mustParseUTC(String ts) {
        Optional<Long> r = parseUTC(ts);
        if (!r.isPresent()) {
            throw new IllegalArgumentException("Unable to parse timestamp " + ts);
        }
        return r.get();
    }

    public static Optional<Long> parseCST(String ts) {
        return parseCST(ts, DEFAULT_FORMATTER);
    }

    public static Optional<Long> parseCST(String ts, DateTimeFormatter formatter) {
        try {
            Instant instant = Instant.from(formatter.withZone(ZONE_ID_CST).parse(ts));
            return Optional.of(instant.toEpochMilli());
        } catch (DateTimeException e) {
            return Optional.absent();
        }
    }

    public static Optional<Long> parseUTC(String ts) {
        return parseUTC(ts, DEFAULT_FORMATTER);
    }

    public static Optional<Long> parseUTC(String ts, DateTimeFormatter formatter) {
        try {
            Instant instant = Instant.from(formatter.withZone(ZONE_ID_UTC).parse(ts));
            return Optional.of(instant.toEpochMilli());
        } catch (DateTimeException e) {
            return Optional.absent();
        }
    }

    public static Optional<Long> parsePST(String ts) {
        return parsePST(ts, DEFAULT_FORMATTER);
    }

    public static long mustParsePST(String ts) {
        Optional<Long> r = parsePST(ts);
        if (!r.isPresent()) {
            throw new IllegalArgumentException("Unable to parse timestamp " + ts);
        }
        return r.get();
    }

    public static Optional<Long> parsePST(String ts, DateTimeFormatter formatter) {
        try {
            Instant instant = Instant.from(DEFAULT_FORMATTER.withZone(ZONE_ID_PST).parse(ts));
            return Optional.of(instant.toEpochMilli());
        } catch (DateTimeException e) {
            return Optional.absent();
        }
    }

    private static final long[] TIME_UNIT_SLOTS = new long[] {
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1),
    };

    private static final String[] TIME_UNITS_EN = new String[] {
            "days",
            "hours",
            "minutes",
            "seconds",
            "millis"
    };

    public static String millisToDuration(long millis) {
        for (int i = 0; i < TIME_UNIT_SLOTS.length; ++i) {
            double r = ((double) millis) / TIME_UNIT_SLOTS[i];
            if (r >= 1) {
                if (millis % TIME_UNIT_SLOTS[i] == 0) {
                    return String.format("%d %s", (long) r, TIME_UNITS_EN[i]);
                } else {
                    return String.format("%.1f %s", r, TIME_UNITS_EN[i]);
                }
            }
        }
        return String.format("%d %s", millis, TIME_UNITS_EN[TIME_UNITS_EN.length - 1]);
    }

    public static String millisToDuration(long millis, boolean shortFormat) {
        String res = millisToDuration(millis);
        if (!shortFormat) {
            return res;
        }
        res = res.replace("seconds", "s");
        res = res.replace("second", "s");
        res = res.replace("minutes", "m");
        res = res.replace("minute", "m");
        res = res.replace("hours", "h");
        res = res.replace("hour", "h");
        res = res.replace("millis", "ms");
        return res;
    }
}
