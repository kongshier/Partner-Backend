# 伙伴匹配系统

## 项目介绍
一个帮助大家匹配到志同道合的校园伙伴移动端网站，主要包括用户登录注册、修改个人信息、按标签搜索校园伙伴、推荐相以校园伙伴、用户创建队伍、队长修改队伍信息，加入公开或加密的队伍等功能。
## 项目起因
出于我的观察发现，有些同学是喜欢一直待在宿舍的，希望在网上交友，其他都很少社交，所以设计这款移动端校园寻找志同道合的伙伴网站。

## 技术选型

### 后端
1. Java SpringBoot2 框架
2. MySQL 数据库
3. MyBatis-Plus
4. MyBatis X 自动生成代码
5. Redis 缓存（Spring Data Redis 等多种实现方式）
6. Redisson 分布式锁
7. Easy Excel 数据导入
8. Spring Scheduler 定时任务
9. Swagger + Knife4j 接口文档
10. Gson：JSON 序列化库
11. 相似度匹配算法

### 前端
1. Vue 3 开发框架（提高页面开发的效率）
2. Vant UI（基于 Vue 的移动端组件库、React 版 Zent）
3. Vite 2
4. Nginx

### 部署上线
1. 宝塔

## 项目功能
1. 用户可以自行注册、上传个人头像、用户登录
2. 用户修改个人信息
3. 用户可以开启智能匹配搜索，根据标签推荐最佳用户
4. 用户可以创建队伍，创建者即为队伍队长，若队长退出，如果队伍还有人，则队长讲转移到第一个加入队伍的用户
5. 队伍队长可以解散队伍，修改队伍信息

## 项目效果界面

### 网站首页
![MWKL( Q3(9F7 GH AD21}JY](https://github.com/kongshier/Partner-Backend/assets/94662685/42f743dd-2432-414c-89e9-7b60830622f4)

#### 首页的联系我界面

![DU)9Y3)3%XQY5GHS~QL4SQH](https://github.com/kongshier/Partner-Backend/assets/94662685/8dde7004-b50c-4e38-a75b-764192a9c6c0)


### 登录注册界面
![)8U%2H{GC)32TQK`60AE2{5](https://github.com/kongshier/Partner-Backend/assets/94662685/e92f121b-5707-400b-ab52-c0813da3821f)

![LJ$(WSGD (EVLWRD0 B 286](https://github.com/kongshier/Partner-Backend/assets/94662685/19acc70b-7619-47c0-8859-8ec3b1effd10)


### 个人信息界面
![6J1SRSG9T648CKVMHN5KPWT](https://github.com/kongshier/Partner-Backend/assets/94662685/b86164c4-418e-47ae-8332-9363727d6bdc)

#### 个人信息维护
![77(OLSPAM~EON30G)5HX~X1](https://github.com/kongshier/Partner-Backend/assets/94662685/8810db4b-2559-4c2e-9959-4da22e735d2d)

### 按照标签进行搜索
![}B3F~1UI8IN5AJNMR BZN{4](https://github.com/kongshier/Partner-Backend/assets/94662685/44c00d41-49f5-437b-a4b6-59e98d620095)

### 队伍界面
![B83G7 MLFQ1F6GW~69JHR7Y](https://github.com/kongshier/Partner-Backend/assets/94662685/55be06fc-8019-4c63-ba6e-730220d44bcc)

#### 创建队伍界面
![Y9LJ2IH$PND7(`$WXGDU6Y](https://github.com/kongshier/Partner-Backend/assets/94662685/28713f06-e943-4b94-8aae-115eeaad4b6f)


## 后续
1. 修改队伍图片变化的bug
2. 加入聊天室
3. 美化前端界面
4. 队长可以邀请用户、剔除用户
