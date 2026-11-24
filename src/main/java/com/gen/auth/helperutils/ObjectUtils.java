package com.gen.auth.helperutils;

import java.util.Collection;
import java.util.Map;

public final class ObjectUtils {

    private ObjectUtils() {
    }

    public static boolean isNullOrEmpty(Object object) {

        if (object == null) {
            return true;
        }

        if (object instanceof Collection<?>) {
            return ((Collection<?>) object).isEmpty();
        }

        if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>) object).isEmpty();
        }

        return false;
    }
}
