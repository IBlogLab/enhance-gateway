package org.iblog.enhance.gateway.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableBoolean;

/**
 * @author shaoxiao.xu
 * @date 2019/1/4 10:42
 */
public class ObjectUpdateUtil {
    public static <T> T updateField(T original, T value, MutableBoolean updated) {
        if (value != null && !value.equals(original)) {
            updated.setValue(true);
            return value;
        } else {
            return original;
        }
    }

    public static long updateField(long original, long value, MutableBoolean updated) {
        if (value != original && value != 0) {
            updated.setValue(true);
            return value;
        } else {
            return original;
        }
    }

    /**
     * @param zeroValue The special value to signal that the intention of the update
     *                  is to change value of the field to <tt>0</tt>.  E.g.,
     *                  if <code>original</code> is 1, <code>value</code> is -1,
     *                  <code>zeroValue</code> is -1, then this method would
     *                  return <tt>0</tt>, and set <code>updated</code> to
     *                  <tt>true</tt>.
     * @return
     */
    public static long updateField(
            long original, long value, long zeroValue, MutableBoolean updated) {
        if (value == zeroValue) {
            if (original != 0) {
                updated.setValue(true);
            }
            return 0;
        }
        return updateField(original, value, updated);
    }

    public static int updateField(int original, int value, MutableBoolean updated) {
        if (value != original && value != 0) {
            updated.setValue(true);
            return value;
        } else {
            return original;
        }
    }

    /**
     * @param zeroValue The special value to signal that the intention of the update
     *                  is to change value of the field to <tt>0</tt>.  E.g.,
     *                  if <code>original</code> is 1, <code>value</code> is -1,
     *                  <code>zeroValue</code> is -1, then this method would
     *                  return <tt>0</tt>, and set <code>updated</code> to
     *                  <tt>true</tt>.
     * @return
     */
    public static int updateField(
            int original, int value, int zeroValue, MutableBoolean updated) {
        if (value == zeroValue) {
            if (original != 0) {
                updated.setValue(true);
            }
            return 0;
        }
        return updateField(original, value, updated);
    }

    public static float updateField(
            float original, float value, float zeroValue, MutableBoolean updated) {
        if (value == zeroValue) {
            if (original != 0) {
                updated.setValue(true);
            }
            return 0;
        }
        final float epsilon = (float) 1.0e-4;
        if (Math.abs(original - value) < epsilon || value == 0) {
            return original;
        }
        updated.setTrue();
        return value;
    }

    public static <T> List<T> updateField(
            List<T> original, List<T> value, MutableBoolean updated) {
        if (value != null) {
            updated.setValue(true);
            return new ArrayList<T>(value);
        } else {
            return original;
        }
    }
}
