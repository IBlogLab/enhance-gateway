package org.iblog.enhance.gateway.core;

import lombok.Data;

import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * wrapper of the request necessary information.
 *
 * @author lance
 */
@Data
public class Cube {
    private String url;
    private HttpMethod method;
    private Map<String, Object> headers;
    private String body;
    private Cube reply;
}
