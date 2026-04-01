package com.petadopt.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Long pages;
    private Long pageNum;
    private Long pageSize;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.list = page.getRecords();
        result.total = page.getTotal();
        result.pages = page.getPages();
        result.pageNum = page.getCurrent();
        result.pageSize = page.getSize();
        return result;
    }

    public List<T> getList() {
        return list;
    }

    public long getTotal() {
        return total != null ? total : 0;
    }
}
