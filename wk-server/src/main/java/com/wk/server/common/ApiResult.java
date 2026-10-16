package com.wk.server.common;

/**
 * 统一接口返回。
 */
public record ApiResult<T>(int code, String msg, T data) {

	public static <T> ApiResult<T> ok(T data) {
		return new ApiResult<>(0, "success", data);
	}

	public static <T> ApiResult<T> fail(String msg) {
		return new ApiResult<>(1, msg, null);
	}

}
