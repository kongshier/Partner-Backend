package com.shier.partner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shier.partner.common.ErrorCode;
import com.shier.partner.common.TeamStatusEnum;
import com.shier.partner.contant.UserConstant;
import com.shier.partner.exception.BusinessException;
import com.shier.partner.mapper.TeamMapper;
import com.shier.partner.model.domain.Team;
import com.shier.partner.model.domain.User;
import com.shier.partner.model.domain.UserTeam;
import com.shier.partner.model.dto.TeamQuery;
import com.shier.partner.model.request.TeamJoinRequest;
import com.shier.partner.model.request.TeamQuitRequest;
import com.shier.partner.model.request.TeamUpdateRequest;
import com.shier.partner.model.vo.TeamUserVO;
import com.shier.partner.model.vo.UserVO;
import com.shier.partner.service.TeamService;
import com.shier.partner.service.UserService;
import com.shier.partner.service.UserTeamService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Shier
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-02-05 16:00:59
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 添加队伍业务逻辑判断
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  g. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Resource
    private UserService userService;

    /**
     * 查询队伍
     * @param teamQuery
     * @param isAdmin 管理员
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            // id条件查询
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            // idList 不为空
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("expireTime", searchText));
            }

            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            //查询最大人数相等
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxMum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }

        //不展示已过期的队伍
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> TeamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            Long teamId = team.getId();
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            //拿到每个队伍加入的成员id
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 根据userid 查询user 拿到加入该队伍的成员列表
            List<UserVO> userList = userTeamList.stream().map(userTeam -> {
                Long userId = userTeam.getUserId();
                User user = userService.getById(userId);
                User safetyUser = userService.getSafetyUser(user);
                UserVO UserVo = new UserVO();
                if (safetyUser != null) {
                    BeanUtils.copyProperties(safetyUser, UserVo);
                }
                return UserVo;
            }).filter(userVo -> org.apache.commons.lang3.StringUtils.isNotEmpty(userVo.getUserAccount())).collect(Collectors.toList());

            // 创建返回对象
            TeamUserVO teamUserVo = new TeamUserVO();

            BeanUtils.copyProperties(team, teamUserVo);
            // 设置已加入的成员
            teamUserVo.setJoinUserList(userList);
            userList.forEach(userVo -> {
                if (userVo.getId() == team.getUserId()) {
                    teamUserVo.setCreateUser(userVo);
                }
            });
            // 加入返回列表
            TeamUserVOList.add(teamUserVo);
        }
        return TeamUserVOList;
    }

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        Long id = teamUpdateRequest.getId();
        if (teamUpdateRequest == null || id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 队伍为加密队伍，必须要设置密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        if (teamUpdateRequest.getMaxNum() != null) {
            int maxNum = Optional.ofNullable(teamUpdateRequest.getMaxNum()).orElse(0);
            if (maxNum < 1 || maxNum > 20 || maxNum < oldTeam.getMaxNum()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
            }
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }


    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (teamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 该用户已加入的队伍数量 数据库查询放到下面，减少查询时间
        Long userId = loginUser.getId();

        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock(UserConstant.JOIN_TEAM);
        try {
            // 抢到锁并执行
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock: " + Thread.currentThread().getId());

                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
                    }
                    // 不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        //队伍只剩下一个人，解散
        if (teamHasJoinNum == 1) {
            //删除队伍
            this.removeById(teamId);
        } else {
            //队伍至少还剩下两人
            //是队长
            if (team.getUserId() == userId) {
                //把队伍转移给最早加入的用户
                //1.查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);
    }


    /**
     * 队长解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否为空
        Team team = getTeamById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (team.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 更具id获取队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    @Override
    public TeamUserVO getTeamById(long id, boolean isAdmin, User loginUser) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(id);

        Long teamId = team.getId();
        Long userId = team.getUserId();

        TeamUserVO TeamUserVO = new TeamUserVO();
        BeanUtils.copyProperties(team, TeamUserVO);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        //设置已加入人数
        TeamUserVO.setHasJoinNum(userTeamList.size());
        List<UserVO> memberUser = userTeamList.stream().map(userTeam -> {
            Long memberId = userTeam.getUserId();
            User user = userService.getById(memberId);
            UserVO userVo = new UserVO();
            BeanUtils.copyProperties(user, userVo);
            if (memberId.equals(userId)) {
                //设置创建人信息
                TeamUserVO.setCreateUser(userVo);
            }
            if (loginUser.getId() == memberId) {
                TeamUserVO.setHasJoin(true);
            }
            return userVo;
        }).collect(Collectors.toList());

        if (!(TeamStatusEnum.PUBLIC.getValue() == team.getStatus()) && !TeamUserVO.isHasJoin()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        //设置队伍成员
        TeamUserVO.setJoinUserList(memberUser);
        return TeamUserVO;
    }

    /**
     * 通过用户获取队伍id
     * @param userId
     * @return
     */
    @Override
    public List<Team> getTeamByUserId(Long userId) {
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, UserTeam::getUserId, userId);
        List<UserTeam> userTeams = userTeamService.list(queryWrapper);
        if (userTeams.size() <= 0) {
            return null;
        }
        List<Long> teamIdList = userTeams.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        List<Team> teamList = this.listByIds(teamIdList);
        return teamList;
    }

}




