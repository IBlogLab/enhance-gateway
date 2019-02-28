package org.iblog.enhance.gateway.controller;

import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.Result;
import org.iblog.enhance.gateway.core.ResultCode;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Strings;

/**
 * @author lance
 */
@RestController
@RequestMapping("/api/enhance-gateway/reprocess/{id}")
public class ReprocessController {
    private static final Logger logger = LoggerFactory.getLogger(ReprocessController.class);

    @Autowired
    private LoggingRecordService loggingRecordService;
    @Autowired
    private HttpClientProvider httpClientProvider;
    @Autowired
    private ServerProperties serverProperties;

    @PostMapping
    public Result process(@PathVariable("id") String id) {
        LoggingRecord exist = loggingRecordService.find(id).orNull();
        if (exist == null) {
            return Result.failure("Not found record " + id, ResultCode.NOT_FOUND.toString());
        }
        if (exist.getStatusCode() >= 200 && exist.getStatusCode() < 300) {
            return Result.failure("当前请求状态为成功，不可操作",
                    String.valueOf(HttpStatus.BAD_REQUEST.value()));
        }
        if (exist.getMethod() == null) {
            return Result.failure("当前日志method字段为空",
                    ResultCode.BAD_REQUEST.toString());
        }
        if (Strings.isNullOrEmpty(exist.getUri())) {
            return Result.failure("当前日志uri字段为空",
                    ResultCode.BAD_REQUEST.toString());
        }
        String url = locate() + exist.getUri();
        HttpHeaders httpHeaders = ObjectMappers.mustReadValue(
                exist.getRequestHeaders(), HttpHeaders.class);
        httpHeaders.add(MarkRequestFilter.INTERRELATED_ID, exist.getId());
        Result result = httpClientProvider.request(
                exist.getMethod(), url, httpHeaders, exist.getRequestBody());
        if (result.isSuccess()) {
            exist.setRehandled(true);
            loggingRecordService.update(exist);
        }
        // TODO logger operation info
        logger.info("reprocess logging record {} / {}", exist.getId(), result.getCode());
        return Result.build(result.getCode() ,result.getData(), result.getMessage(), result.isSuccess());
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