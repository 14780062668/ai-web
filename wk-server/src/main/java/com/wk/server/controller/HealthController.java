package com.wk.server.controller;

import com.wk.server.common.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

	@GetMapping("/health")
	public ApiResult<Map<String, String>> health() {
		// 进程已启动即视为服务可用
		return ApiResult.ok(Map.of("status", "UP"));
	}

}
