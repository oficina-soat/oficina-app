package br.com.oficina.common;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        long total,
        int page,
        int size,
        int totalPages) {
    public PageResult(int size, int page, long total, List<T> items) {
        this(items, total, page, size, size <= 0 ? 0 : (int) Math.ceil((double) total / (double) size));
    }
}
