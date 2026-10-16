package com.wk.server.common;

import java.util.List;

/**
 * 分页结果。size 是当前页实际条数。
 */
public record PageResult<T>(long total, int page, int size, List<T> records) {
}
