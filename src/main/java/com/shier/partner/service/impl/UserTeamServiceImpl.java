package com.shier.partner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shier.partner.model.domain.UserTeam;
import com.shier.partner.service.UserTeamService;
import com.shier.partner.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Shier
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-02-05 16:02:47
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




