package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private long total;      // 总条数
    private List<T> records; // 当前页数据
    private int page;        // 当前页（从1开始）
    private int size;        // 每页大小
}
