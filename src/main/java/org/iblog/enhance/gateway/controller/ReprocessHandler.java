package org.iblog.enhance.gateway.controller;

import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.Result;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.lifecycle.sched.Schedulable;
import org.iblog.enhance.gateway.lifecycle.sched.SchedulableHandler;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import com.google.common.base.Strings;

/**
 * @author lance
 */
@Component
public class ReprocessHandler implements SchedulableHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReprocessHandler.class);
    private final String TYPE = "internal_async_schedulable";

    private final LoggingRecordService loggingRecordService;
    private final HttpClientProvider httpClientProvider;
    private final ServerProperties serverProperties;

    @Autowired
    public ReprocessHandler(
            final LoggingRecordService loggingRecordService,
            final HttpClientProvider httpClientProvider,
            final ServerProperties serverProperties) {
        this.loggingRecordService = loggingRecordService;
        this.httpClientProvider = httpClientProvider;
        this.serverProperties = serverProperties;
    }

    @Override
    public boolean handle(Schedulable task) {
        if (task == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(task.getType()) || Strings.isNullOrEmpty(task.getData())) {
            return false;
        }
        SchedulableWork work = ObjectMappers.mustReadValue(task.getData(), SchedulableWork.class);
        if (work == null) {
            return false;
        }
        LoggingRecord record = loggingRecordService.find(work.getId()).orNull();
        if (record == null) {
            return false;
        }
        if (record.getStatusCode() >= 200 && record.getStatusCode() < 300) {
            return false;
        }
        if (record.getMethod() == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(record.getUri())) {
            return false;
        }
        String url = locate() + record.getUri();
        HttpHeaders httpHeaders = ObjectMappers.mustReadValue(
                record.getRequestHeaders(), HttpHeaders.class);
        httpHeaders.add(MarkRequestFilter.INTERRELATED_ID, record.getId());
        Result result = httpClientProvider.request(
                record.getMethod(), url, httpHeaders, record.getRequestBody());
        logger.info("ReprocessHandler success:{} code:{} message:{} data:{}",
                result.isSuccess(), result.getCode(), result.getMessage(), result.getData());
        return result.isSuccess() ? true : false;
    }

    @Override
    public String type() {
        return TYPE;
    }

    private String locate() {
        int port = serverProperties.getPort();
        String host;
        if (serverProperties.getAddress() != null) {
            host = serverProperties.getAddress().getHostAddress();
        } else {
            host = "http://127.0.0.1";
        }
        return host + ":" + port;
    }
}
