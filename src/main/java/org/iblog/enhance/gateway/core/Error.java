package org.iblog.enhance.gateway.core;

/**
 * @author lance
 */
public enum Error {
    UNKNOWN,
    OK,
    DOWNSTREAM_SERVER_AVAILABLE,
    DOWNSTREAM_SERVER_INTERNAL_ERROR,
    BAD_REQUEST, // 400
    UNAUTHORIZED, // 401
    FORBIDDEN, // 403
    DOWNSTREAM_SERVICE_NOT_FOUND, // 404
}
