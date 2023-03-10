package com.shier.partner.common;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * 通用删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1787902631969457554L;

    private long id;
}