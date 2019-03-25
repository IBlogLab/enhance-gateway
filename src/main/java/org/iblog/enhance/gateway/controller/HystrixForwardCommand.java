package org.iblog.enhance.gateway.controller;

import reactor.core.publisher.Mono;

import org.iblog.enhance.gateway.core.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shaoxiao.xu
 * @date 2019/3/21 11:35
 */
@RestController
@RequestMapping("/api/hystrix/service-unavailable")
public class HystrixForwardCommand {

    @GetMapping
    public Mono<Result> hystrixGet() {
        // TODO need permission validation here
        return Mono.just(Result.failure("Get hystrix: 服务不可达, 请稍后重试"));
    }

    @PostMapping
    public Mono<Result> hystrixPost() {
        // TODO need permission validation here
        return Mono.just(Result.failure("Post hystrix: 服务不可达, 请稍后重试"));
    }

    @PatchMapping
    public Mono<Result> hystrixPatch() {
        // TODO need permission validation here
        return Mono.just(Result.failure("Patch hystrix: 服务不可达, 请稍后重试"));
    }

    @PutMapping
    public Mono<Result> hystrixPut() {
        // TODO need permission validation here
        return Mono.just(Result.failure("Put hystrix: 服务不可达, 请稍后重试"));
    }

    @DeleteMapping
    public Mono<Result> hystrixDelete() {
        // TODO need permission validation here
        return Mono.just(Result.failure("Delete hystrix: 服务不可达, 请稍后重试"));
    }
}
