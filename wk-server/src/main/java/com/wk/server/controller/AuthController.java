package com.wk.server.controller;

import com.wk.server.common.ApiResult;
import com.wk.server.common.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证接口：账号密码在 application.properties 配置，单用户场景从简。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final TokenService tokenService;
	private final String username;
	private final String password;

	public AuthController(TokenService tokenService,
			@Value("${app.auth.username}") String username,
			@Value("${app.auth.password}") String password) {
		this.tokenService = tokenService;
		this.username = username;
		this.password = password;
	}

	@PostMapping("/login")
	public ApiResult<TokenService.TokenInfo> login(@RequestBody LoginRequest request) {
		if (request == null || !username.equals(request.username()) || !password.equals(request.password())) {
			return ApiResult.fail("用户名或密码错误");
		}
		return ApiResult.ok(tokenService.create(request.username()));
	}

	public record LoginRequest(String username, String password) {
	}

}
