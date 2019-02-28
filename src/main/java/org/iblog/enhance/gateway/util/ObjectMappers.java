package org.iblog.enhance.gateway.util;

import javax.annotation.Nullable;
import java.io.IOException;
import org.iblog.enhance.gateway.exception.DataFormatException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMappers {
    private static final ObjectMapper DEFAULT_INSTANCE = new ObjectMapper();

    static {
        DEFAULT_INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        DEFAULT_INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DEFAULT_INSTANCE.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    public static ObjectMapper get() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Deserialize <code>json</code> to an object of type <code>clazz</code>.
     *
     * @throws DataFormatException
     */
    public static <T> T mustReadValue(@Nullable String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readValue(json, clazz);
        } catch (IOException e) {
            throw new DataFormatException(e);
        }
    }



    /**
     * Deserialize <code>json</code> to an object, using <code>typeRef</code>
     * to determine class of the object.
     *
     * @throws DataFormatException
     */
    public static <T> T mustReadValue(@Nullable String json, TypeReference<T> typeRef) {
        if (json == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readValue(json, typeRef);
        } catch (IOException e) {
            throw new DataFormatException(e);
        }
    }

    /**
     * Serialize <code>o</code> to a string.
     *
     * @throws DataFormatException
     */
    public static String mustWriteValue(@Nullable Object o) {
        if (o == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.writeValueAsString(o);
        } catch (IOException e) {
            throw new DataFormatException(e);
        }
    }

    public static String mustWriteValuePretty(@Nullable Object o) {
        if (o == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (IOException e) {
            throw new DataFormatException(e);
        }
    }

    public static JsonNode mustReadTree(@Nullable String json) {
        if (json == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readTree(json);
        } catch (IOException e) {
            throw new DataFormatException(e);
        }
    }
}
