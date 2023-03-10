package com.shier.partner.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = -2038884913144640407L;
    /**
     * id
     */
    private Long teamId;
}