package com.shier.partner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shier.partner.model.domain.Team;
import com.shier.partner.model.domain.User;
import com.shier.partner.model.dto.TeamQuery;
import com.shier.partner.model.request.TeamJoinRequest;
import com.shier.partner.model.request.TeamQuitRequest;
import com.shier.partner.model.request.TeamUpdateRequest;
import com.shier.partner.model.vo.TeamUserVO;

import java.util.List;

/**
* @author Shier
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-02-05 16:00:59
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     * @param teamQuery
     * @param isAdmin 管理员
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 修改队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 通过id队伍
     *
     * @param id
     * @return
     */
    TeamUserVO getTeamById(long id, boolean isAdmin, User loginUser);

    List<Team> getTeamByUserId(Long userId);
}
