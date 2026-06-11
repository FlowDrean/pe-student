package com.student.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

@Data
public class PageVO<T> {
    private List<T> list;
    private long total;
    private long page;
    private long size;
    private long pages;

    public static <T> PageVO<T> from(IPage<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.list = page.getRecords();
        vo.total = page.getTotal();
        vo.page = page.getCurrent();
        vo.size = page.getSize();
        vo.pages = page.getPages();
        return vo;
    }
}
