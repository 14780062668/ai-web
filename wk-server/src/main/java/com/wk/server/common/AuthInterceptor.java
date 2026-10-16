package com.wk.server.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录校验拦截器：/api/** 除 /api/auth/** 外都要求 Bearer 令牌，无效或过期返回 401。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

	private final TokenService tokenService;

	public AuthInterceptor(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 跨域预检请求直接放行
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		String header = request.getHeader("Authorization");
		String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
		if (tokenService.verify(token) != null) {
			return true;
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"code\":1,\"msg\":\"未登录或登录已过期\"}");
		return false;
	}

}
