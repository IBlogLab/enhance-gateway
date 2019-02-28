package org.iblog.enhance.gateway.core;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author lance
 */
@Data
@Document(collection = "open_apis")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class OpenApi {
    @Id
    private String id;
    @NotBlank
    private String uriPattern;
    @NotBlank
    private String method;
    @NotBlank
    private String apiCode;
    private String from; // where the request from
    private String to; // where the request to
    private boolean regex;
    @NotBlank
    private String businessType;
    private List<FilterConfig> filters;

    private long createdAt;
    private String createdBy;
    private long lastUpdatedAt;
    private String lastUpdatedBy;


    @Data
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class FilterConfig {
        private String name;
        private boolean enable;
    }
}
