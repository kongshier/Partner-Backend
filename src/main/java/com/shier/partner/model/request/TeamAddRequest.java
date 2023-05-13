package com.shier.partner.model.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = -4162304142710323660L;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 队伍头像
     */
    private String avatarUrl;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     * 格式化后端的接口
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}