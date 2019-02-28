package org.iblog.enhance.gateway.core;

import lombok.Data;

import java.util.List;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.iblog.enhance.gateway.util.ObjectUpdateUtil;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author shaoxiao.xu
 * @date 2018/12/26 15:23
 */
@Data
@Document(collection = "logging_records")
@JsonInclude
public class LoggingRecord {
    private String id; // logistic primary key and must be unique
    private String interrelatedId; // for rehandled logging, related to failed request logging.

    private String uri; // for match apiCode
    private String apiCode;
    private String businessType;

    private String clientId; // lessee
    private String secretKey; // appKey
    private String warehouse; // belong to lessee
    private String from; // where the request from
    private String to; // where the request to
    private boolean externalized; // true mark the request from external
    private boolean async; // mark whether the business is handled asynchronously
    private boolean rehandled; // mark whether the request is rehandled.
    private Error error; // error type (e.g. routing exception or convert exception)

    // TODO prd: keywords
    private List<KeyWord> tags; // determined by the business side plug-in

    private String ip; // logging request sender's host ip address
    private String url; // {ip}:{port}/{uri} useful for retry or other ?
    private String realUrl; // the real path of request to the downstream service
    private HttpMethod method; // request method type (e.g. GET or POST)
    private RequestType requestType; // request type (e.g. HTTP or Socket)
    private String requestHeaders; // logging request headers
    private String responseHeaders; // logging response headers
    private String queries; // logging request queries
    private int statusCode; // response code
    private String requestBody; // logging request body
    private String requestConvertedBody; // logging request converted body
    private String responseBody; // logging response body
    private String responseConvertedBody; // logging response converted body
    private long startTime; // request receiving time
    private long endTime; // request response time

    private long createdAt;
    private String createdBy;
    private long lastUpdatedAt;
    private String lastUpdatedBy;

    public boolean updateTags(List<KeyWord> tags) {
        if (this.tags == null) {
            this.tags = tags;
            return true;
        }
        MutableBoolean updated = new MutableBoolean();
        for (KeyWord keyWord : tags) {
            KeyWord target = this.tags.stream()
                    .filter(t -> t.getKey().equals(keyWord.getKey()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                this.tags.add(keyWord);
                updated.setValue(true);
            } else {
                target.getValues().addAll(keyWord.getValues());
                updated.setValue(true);
            }
        }
        return updated.booleanValue();
    }

    public boolean updateFrom(LoggingRecord other) {
        MutableBoolean updated = new MutableBoolean();
        interrelatedId = ObjectUpdateUtil.updateField(
                interrelatedId, other.getInterrelatedId(), updated);
        uri = ObjectUpdateUtil.updateField(uri, other.getUri(), updated);
        apiCode = ObjectUpdateUtil.updateField(apiCode, other.getApiCode(), updated);
        businessType = ObjectUpdateUtil.updateField(
                businessType, other.getBusinessType(), updated);
        clientId = ObjectUpdateUtil.updateField(clientId, other.getClientId(), updated);
        secretKey = ObjectUpdateUtil.updateField(secretKey, other.getSecretKey(), updated);
        warehouse = ObjectUpdateUtil.updateField(warehouse, other.getWarehouse(), updated);
        from = ObjectUpdateUtil.updateField(from, other.getFrom(), updated);
        to = ObjectUpdateUtil.updateField(to, other.getTo(), updated);
        externalized = ObjectUpdateUtil.updateField(externalized, other.isExternalized(), updated);
        async = ObjectUpdateUtil.updateField(async, other.isAsync(), updated);
        error = ObjectUpdateUtil.updateField(error, other.getError(), updated);
        ip = ObjectUpdateUtil.updateField(ip, other.getIp(), updated);
        url = ObjectUpdateUtil.updateField(url, other.getUrl(), updated);
        realUrl = ObjectUpdateUtil.updateField(realUrl, other.getRealUrl(), updated);
        method = ObjectUpdateUtil.updateField(method, other.getMethod(), updated);
        requestType = ObjectUpdateUtil.updateField(requestType, other.getRequestType(), updated);
        requestHeaders = ObjectUpdateUtil.updateField(
                requestHeaders, other.getRequestHeaders(), updated);
        responseHeaders = ObjectUpdateUtil.updateField(
                responseHeaders, other.getResponseHeaders(), updated);
        queries = ObjectUpdateUtil.updateField(queries, other.getQueries(), updated);
        statusCode = ObjectUpdateUtil.updateField(statusCode, other.getStatusCode(), updated);
        requestBody = ObjectUpdateUtil.updateField(requestBody, other.getRequestBody(), updated);
        requestConvertedBody = ObjectUpdateUtil.updateField(requestConvertedBody,
                other.getRequestConvertedBody(), updated);
        responseBody = ObjectUpdateUtil.updateField(
                responseBody, other.getResponseBody(), updated);
        responseConvertedBody = ObjectUpdateUtil.updateField(responseConvertedBody,
                other.getResponseConvertedBody(), updated);
        startTime = ObjectUpdateUtil.updateField(startTime, other.getStartTime(), updated);
        endTime = ObjectUpdateUtil.updateField(endTime, other.getEndTime(), updated);
        createdAt = ObjectUpdateUtil.updateField(createdAt, other.getCreatedAt(), updated);
        lastUpdatedAt = ObjectUpdateUtil.updateField(
                lastUpdatedAt, other.getLastUpdatedAt(), updated);
        lastUpdatedBy = ObjectUpdateUtil.updateField(
                lastUpdatedBy, other.getLastUpdatedBy(), updated);
        return updated.booleanValue();
    }
}
