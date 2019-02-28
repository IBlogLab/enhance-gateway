package org.iblog.enhance.gateway.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.iblog.enhance.gateway.core.Cube;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.Result;
import org.iblog.enhance.gateway.filter.MarkRequestFilter;
import org.iblog.enhance.gateway.lifecycle.sched.XTaskScheduler;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.LoggingUUIDUtil;
import org.iblog.enhance.gateway.util.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Strings;

/**
 * @author shaoxiao.xu
 * @date 2019/2/20 11:21
 */
@RestController
@RequestMapping("/api/interface/externalized")
public class ExternalizedController {
    private static final Logger logger = LoggerFactory.getLogger(ExternalizedController.class);
    private final Clock clock = Clock.defaultClock();

    @Autowired
    private LoggingRecordService loggingRecordService;
    @Autowired
    private XTaskScheduler taskScheduler;

    @PostMapping("/forward")
    public Result receive(@RequestBody Cube cube, ServerWebExchange exchange) {
        // TODO auth ?
        if (!check(cube)) {
            return Result.failure("请求参数错误",
                    String.valueOf(HttpStatus.BAD_REQUEST.value()));
        }
        LoggingRecord record = init(exchange);
        record.setRequestBody(ObjectMappers.mustWriteValue(cube));
        loggingRecordService.create(record);
        taskScheduler.scheduleWithFixedDelay(0, TimeUnit.SECONDS.toMillis(1),
                "internal_async_schedulable", record.getId(),
                0, 0);
        return Result.success(record, "回执请求已成功接收");
    }

    private LoggingRecord init(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String recordId = LoggingUUIDUtil.generateUUID(exchange);
        long start = clock.getTime();
        logger.info("receive forward request {} at {}", recordId, start);
        LoggingRecord record = new LoggingRecord();
        record.setId(recordId);
        record.setStartTime(start);
        record.setClientId(request.getHeaders().getFirst(MarkRequestFilter.X_Session_User));
        record.setSecretKey(request.getHeaders().getFirst(MarkRequestFilter.X_Session_Secret));
        record.setWarehouse(request.getHeaders().getFirst(MarkRequestFilter.X_Session_Warehouse));
        record.setAsync(Boolean.parseBoolean(
                exchange.getRequest().getHeaders().getFirst(MarkRequestFilter.X_Task_Async)));
        record.setUri(request.getURI().getPath());
        record.setMethod(request.getMethod());
        record.setRequestType(exchange.getAttribute(MarkRequestFilter.REQUEST_TYPE_MARK));
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            record.setIp(request.getRemoteAddress().getAddress().getHostAddress());
        }
        record.setRequestHeaders(transit(request.getHeaders()));
        record.setQueries(transit(request.getQueryParams()));
        record.setUrl(request.getURI().toString());
        record.setError(Error.OK);
        record.setCreatedBy(MarkRequestFilter.DEFAULT_CREATED_BY);
        return record;
    }

    private boolean check(Cube cube) {
        if (cube == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(cube.getUrl())) {
            return false;
        }
        if (cube.getMethod() == null) {
            return false;
        }
        return true;
    }

    private String transit(Object obj) {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Map)) {
            throw new DataFormatException("error input");
        }
        return ObjectMappers.mustWriteValue(obj);
    }
}
