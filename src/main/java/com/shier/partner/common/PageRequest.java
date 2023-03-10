package com.shier.partner.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用新建分页请求参数包装类
 *
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -4162304142710323660L;

    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前是第几页
     */
    protected int pageNum;
}