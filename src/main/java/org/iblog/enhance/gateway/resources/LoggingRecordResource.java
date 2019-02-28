package org.iblog.enhance.gateway.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.KeyWord;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.core.ResultCode;
import org.iblog.enhance.gateway.db.filter.LoggingRecordFilter;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.util.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.iblog.enhance.gateway.core.Error;

import static org.iblog.enhance.gateway.filter.MarkRequestFilter.X_Session_User;

/**
 * @author lance
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping(
        value = "/api/enhance-gateway/managed/logging-record",
        consumes = "application/json; charset=utf-8",
        produces = "application/json; charset=utf-8")
public class LoggingRecordResource {

    @Autowired
    private LoggingRecordService loggingRecordService;
    private final Clock clock = Clock.defaultClock();

    @GetMapping("/{id}")
    public PageResult<LoggingRecord> getLoggingRecord(
            ServerWebExchange exchange, @PathVariable("id") String id) {
        ServerHttpRequest request = exchange.getRequest();
        // TODO auth
        PageResult.Builder builder = new PageResult.Builder();
        LoggingRecord exist = loggingRecordService.find(id).orNull();
        if (exist == null) {
            return builder.setCode(ResultCode.NOT_FOUND)
                    .setSuccess(true)
                    .setMessage("未查询到对应记录")
                    .build();
        }
        return builder.setCode(ResultCode.OK)
                .setData(exist)
                .setSuccess(true)
                .setMessage("success")
                .build();
    }

    @GetMapping
    public PageResult<List<LoggingRecord>> listLoggingRecords(
            ServerWebExchange exchange,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "s", defaultValue = "0") Long start,
            @RequestParam(value = "e", defaultValue = "0") Long end,
            @RequestParam(value = "reqRangeLeft", defaultValue = "0") Long reqRangeLeft,
            @RequestParam(value = "reqRangeRight", defaultValue = "0") Long reqRangeRight,
            @RequestParam(value = "respRangeLeft", defaultValue = "0") Long respRangeLeft,
            @RequestParam(value = "respRangeRight", defaultValue = "0") Long respRangeRight,
            @RequestParam(value = "uri", defaultValue = "") List<String> uris,
            @RequestParam(value = "tp", defaultValue = "") List<String> types,
            @RequestParam(value = "eas", defaultValue = "false") Boolean excludeAsync,
            @RequestParam(value = "es", defaultValue = "false") Boolean excludeSync,
            @RequestParam(value = "hs", defaultValue = "false") Boolean handleSuccess,
            @RequestParam(value = "hf", defaultValue = "false") Boolean handleFail,
            @RequestParam(value = "er", defaultValue = "") List<String> errors,
            @RequestParam(value = "md", defaultValue = "") List<String> methods,
            @RequestParam(value = "st", defaultValue = "-1") List<Integer> statuses,
            @RequestParam(value = "fm", defaultValue = "") List<String> froms,
            @RequestParam(value = "to", defaultValue = "") List<String> tos,
            @RequestParam(value = "tag", defaultValue = "") List<String> tags) {
        PageResult.Builder builder = new PageResult.Builder();
        int from = (page - 1) * pageSize;
        int to = page * pageSize;
        if (from > to) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("分页信息错误")
                    .build();
        }
        if (start.compareTo(end) > 0
                || reqRangeLeft.compareTo(reqRangeRight) > 0
                || respRangeLeft.compareTo(respRangeRight) > 0) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("时间条件错误")
                    .build();
        }
        if ((handleSuccess && !handleFail) || (!handleSuccess && handleFail)) {
            if (statuses == null) {
                statuses = Lists.newArrayList();
            }
            statuses.clear();
            statuses.add(200);
        }
        LoggingRecordFilter.Builder filterBuilder = new LoggingRecordFilter.Builder()
                .setStart(start == 0 ? Long.MIN_VALUE : start)
                .setEnd(end == 0 ? Long.MAX_VALUE : end)
                .setReqRange(
                        reqRangeLeft == 0 ? Long.MIN_VALUE : reqRangeLeft,
                        reqRangeRight == 0 ? Long.MAX_VALUE : reqRangeRight)
                .setRespRange(
                        respRangeLeft == 0 ? Long.MIN_VALUE : respRangeLeft,
                        respRangeRight == 0 ? Long.MAX_VALUE : respRangeRight)
                .setFrom(from)
                .setTo(to)
                .setUris(uris)
                .setTypes(types)
                .setExcludeAsync(excludeAsync)
                .setExcludeSync(excludeSync)
                .setExcludeHandleSuccess(handleFail)
                .setExcludeHandleFail(handleSuccess)
                .setFroms(froms)
                .setTos(tos);

        if (CollectionUtils.isNotEmpty(errors)) {
            List<Error> errorsList = errors.stream()
                    .filter(Objects::nonNull)
                    .map(String::toUpperCase)
                    .map(error -> Error.valueOf(error))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(errorsList)) {
                filterBuilder.setErrors(errorsList);
            }
        }

        if (CollectionUtils.isNotEmpty(methods)) {
            List<HttpMethod> methodList = methods.stream()
                    .filter(Objects::nonNull)
                    .map(String::toUpperCase)
                    .map(method -> HttpMethod.valueOf(method))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(methodList)) {
                filterBuilder.setMethods(methodList);
            }
        }

        if (CollectionUtils.isNotEmpty(statuses)) {
            List<Integer> statusList = statuses.stream()
                    .filter(status -> status > 0)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(statusList)) {
                filterBuilder.setStatuses(statusList);
            }
        }
        if (CollectionUtils.isNotEmpty(tags)) {
            List<KeyWord> keyWords = new ArrayList<>(tags.size());
            tags.forEach(tag -> {
                String[] split = tag.split(":");
                if (split.length != 2) {
                    return;
                }
                keyWords.add(KeyWord.build(
                        split[0], Arrays.copyOfRange(split, 1, 2)));
            });
            filterBuilder.setTags(keyWords);
        }

        builder = loggingRecordService.list(filterBuilder.build());
        return builder.setCode(ResultCode.OK)
                .setSuccess(true)
                .setMessage("success")
                .build();
    }

    @PatchMapping("/{id}")
    public PageResult<LoggingRecord> updateLoggingRecord(
            ServerWebExchange exchange, @RequestBody LoggingRecord record) {
        PageResult.Builder builder = new PageResult.Builder();
        if (Strings.isNullOrEmpty(record.getId())) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("参数 id 不能为空")
                    .build();
        }
        if (!loggingRecordService.exist(record.getId())) {
            return builder.setCode(ResultCode.NOT_FOUND)
                    .setSuccess(false)
                    .setMessage("目标对象不存在")
                    .build();
        }
        record.setLastUpdatedBy(exchange.getRequest().getHeaders().getFirst(X_Session_User));
        LoggingRecord update = loggingRecordService.update(record).orNull();
        if (update == null) {
            return builder.setCode(ResultCode.NOT_FOUND)
                    .setSuccess(false)
                    .setMessage("目标对象不存在")
                    .build();
        }
        return builder.setCode(ResultCode.OK)
                .setSuccess(true)
                .setData(update)
                .setMessage("success")
                .build();
    }

    @DeleteMapping
    public PageResult<LoggingRecord> deleteLoggingRecord(
            ServerWebExchange exchange, @RequestBody DeleteBatch batch) {
        PageResult.Builder builder = new PageResult.Builder();
        if (batch == null || CollectionUtils.isEmpty(batch.ids)) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("参数 id 不能为空")
                    .build();
        }
        long deleted = loggingRecordService.delete(batch.ids);
        if (deleted == 0) {
            return builder.setCode(ResultCode.NOT_FOUND)
                    .setSuccess(false)
                    .setMessage("目标对象不存在")
                    .build();
        }
        return builder.setCode(ResultCode.OK)
                .setSuccess(true)
                .setData(deleted)
                .setMessage("success")
                .build();
    }

    public static class DeleteBatch {
        public List<String> ids;
    }
}
