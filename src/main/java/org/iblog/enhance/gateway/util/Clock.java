package org.iblog.enhance.gateway.util;

/**
 * An abstraction for how time passes.
 *
 * @author lance xu
 * @date2018年12月06日 14:53
 */
public abstract class Clock {
    public abstract long getTick();

    public long getTime() {
        return System.currentTimeMillis();
    }

    public static Clock defaultClock() {
        return UserTimeClockHolder.DEFAULT;
    }

    public static class UserTimeClock extends Clock {
        @Override
        public long getTick() {
            return System.nanoTime();
        }
    }

    private static class UserTimeClockHolder {
        private static final Clock DEFAULT = new UserTimeClock();
    }
}
