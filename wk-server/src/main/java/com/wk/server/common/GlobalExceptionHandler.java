package com.wk.server.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;

/**
 * 全局异常处理：所有未捕获异常统一按 ApiResult 结构返回，
 * 避免前端收到 Spring 默认错误页导致解析失败。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 参数错误：类型不匹配（page=abc）、缺少必填参数、请求体 JSON 解析失败
	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class,
			HttpMessageNotReadableException.class
	})
	public ApiResult<Void> badRequest(Exception e) {
		return ApiResult.fail("请求参数不正确");
	}

	// 兜底：未知异常不向外暴露堆栈
	@ExceptionHandler(Exception.class)
	public ApiResult<Void> error(Exception e) {
		return ApiResult.fail("服务器内部错误");
	}

}
