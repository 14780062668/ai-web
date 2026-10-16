package com.wk.server.config;

import com.wk.server.common.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;

	public CorsConfig(AuthInterceptor authInterceptor) {
		this.authInterceptor = authInterceptor;
	}

	// 登录校验：除登录接口外全部要求令牌
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/api/**")
				.excludePathPatterns("/api/auth/**");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// 允许本地 Vue 开发服务访问接口
		// 开发时 Vite 端口可能不是 5173，本地任意端口都放行
		registry.addMapping("/api/**")
				.allowedOriginPatterns("http://localhost:*")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
	}

}
