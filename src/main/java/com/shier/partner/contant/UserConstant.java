package com.shier.partner.contant;

/**
 * 用户常量
 *
 * @author shier
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    //  ------- 权限 --------

    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;

    /**
     * 加入队伍锁
     */
    String JOIN_TEAM = "partner:join_team";

    /**
     * 预加载锁
     */
    String PRE_CACHE_JOB = "shier:precachejob:docache:lock";
}
