package org.iblog.enhance.gateway.resources;

import java.util.List;
import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.core.ResultCode;
import org.iblog.enhance.gateway.db.filter.OpenApiFilter;
import org.iblog.enhance.gateway.service.OpenApiService;
import org.iblog.enhance.gateway.util.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import com.google.common.base.Strings;
import org.iblog.enhance.gateway.core.Error;

import static org.iblog.enhance.gateway.filter.MarkRequestFilter.X_Session_User;

/**
 * TODO auth ?
 *
 * @author shaoxiao.xu
 * @date 2018/12/27 16:05
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping(
        value = "/api/interface/managed/open-api",
        consumes = "application/json; charset=utf-8",
        produces = "application/json; charset=utf-8")
public class OpenApiResource {
    @Autowired
    private OpenApiService openApiService;
    private final Clock clock = Clock.defaultClock();

    @GetMapping("/{id}")
    public PageResult<OpenApi> getOpenApi(ServerWebExchange exchange, @PathVariable("id") String id) {
        ServerHttpRequest request = exchange.getRequest();
        // TODO auth
        PageResult.Builder builder = new PageResult.Builder();
        OpenApi exist = openApiService.find(id).orNull();
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
    public PageResult<List<OpenApi>> listOpenApis(
            ServerWebExchange exchange,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "s", defaultValue = "0") Long start,
            @RequestParam(value = "e", defaultValue = "0") Long end,
            @RequestParam(value = "uri", defaultValue = "") List<String> uris,
            @RequestParam(value = "method", defaultValue = "") List<String> methods,
            @RequestParam(value = "code", defaultValue = "") List<String> codes,
            @RequestParam(value = "type", defaultValue = "") List<String> types) {
        PageResult.Builder builder = new PageResult.Builder();
        int from = (page - 1) * pageSize;
        int to = page * pageSize;
        if (from > to) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("分页信息错误")
                    .build();
        }
        if (start.compareTo(end) > 0) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("时间条件错误")
                    .build();
        }
        builder = openApiService.list(new OpenApiFilter.Builder()
                .setStart(start == 0 ? Long.MIN_VALUE : start)
                .setEnd(end == 0 ? Long.MAX_VALUE : end)
                .setFrom(from)
                .setTo(to)
                .setUris(uris)
                .setMethods(methods)
                .setCodes(codes)
                .setTypes(types)
                .build());
        return builder.setCode(ResultCode.OK)
                .setSuccess(true)
                .setMessage("success")
                .build();
    }

    @PostMapping
    public PageResult<OpenApi> postOpenApi(ServerWebExchange exchange, @RequestBody OpenApi openApi) {
        PageResult.Builder builder = new PageResult.Builder();
        openApi.setCreatedBy(exchange.getRequest().getHeaders().getFirst(X_Session_User));
        openApi.setLastUpdatedBy(openApi.getCreatedBy());
        OpenApi insert = openApiService.create(openApi).orNull();
        if (insert == null) {
            return builder.setCode(ResultCode.CONFLICT)
                    .setMessage("新增api的url和method已存在，不能重复新增。")
                    .setSuccess(false)
                    .build();
        }
        return builder.setCode(ResultCode.OK)
                .setData(insert)
                .setSuccess(true)
                .setMessage("success")
                .build();
    }

    @PatchMapping("/{id}")
    public PageResult<OpenApi> updateOpenApi(
            ServerWebExchange exchange, @PathVariable("id") String id, @RequestBody OpenApi openApi) {
        PageResult.Builder builder = new PageResult.Builder();
        if (Strings.isNullOrEmpty(id)) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("参数 id 不能为空")
                    .build();
        }
        if (!openApiService.exist(id)) {
            return builder.setCode(ResultCode.NOT_FOUND)
                    .setSuccess(false)
                    .setMessage("目标对象不存在")
                    .build();
        }
        openApi.setId(id);
        openApi.setLastUpdatedBy(exchange.getRequest().getHeaders().getFirst(X_Session_User));
        OpenApi update = openApiService.update(openApi).orNull();
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

    @DeleteMapping("/{id}")
    public PageResult<OpenApi> deleteOpenApi(
            ServerWebExchange exchange, @PathVariable("id") String id) {
        PageResult.Builder builder = new PageResult.Builder();
        if (Strings.isNullOrEmpty(id)) {
            return builder.setCode(ResultCode.BAD_REQUEST)
                    .setSuccess(false)
                    .setMessage("参数 id 不能为空")
                    .build();
        }
        OpenApi delete = openApiService.delete(id).orNull();
        if (delete == null) {
            return builder.setCode(ResultCode.NOT_FOUND)
                    .setSuccess(false)
                    .setMessage("目标对象不存在")
                    .build();
        }
        return builder.setCode(ResultCode.OK)
                .setSuccess(true)
                .setData(delete)
                .setMessage("success")
                .build();
    }
}
