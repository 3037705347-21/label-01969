# 🐾 宠物领养系统

## 1 How to Run

### 使用 Docker Compose 一键启动（推荐）

```bash
# 构建并启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止服务并删除数据卷（重置数据库）
docker-compose down -v
```

### 手动启动（开发环境）

#### 1. 数据库初始化
```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

#### 2. 后端启动
```bash
cd backend
# 修改 application.yml 中的数据库配置
mvn spring-boot:run
```

#### 3. 前端启动
```bash
cd frontend-admin
npm install
npm run dev
```

---

## 2 Services

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend-admin | 8084 | 管理后台前端 |
| backend | 8080 | 后端 API 服务 |
| mysql | 3306 | MySQL 数据库 |

**访问地址：**
- 管理后台：http://localhost:8084

---

## 3 测试账号

| 角色 | 用户名 | 密码 | 功能权限 |
|------|--------|------|----------|
| 管理员 | admin | 123456 | 用户管理、宠物管理、领养复核、家访管理、跟进管理、黑名单、知识库 |
| 救助方 | rescuer | 123456 | 发布宠物、审核领养申请、查看跟进记录 |
| 领养人 | adopter | 123456 | 浏览宠物、申请领养、提交跟进记录 |

> 注：也可通过注册页面自行注册救助方或领养人账号

---

## 4 题目内容

个宠物领养系统核心是连接宠物救助方与领养人，实现 “宠物展示 - 申请审核 - 领养匹配 - 后续跟进” 的闭环，核心需求的可拆解为以下 6 类（极简且贴合实际使用，无冗余）：
1. 核心角色与权限（基础）
3 类核心角色：管理员（统筹）、救助方 / 机构（发布宠物）、领养人（申请领养）
权限区分：管理员审核双方资质、管理数据；救助方上传宠物信息、审核领养申请；领养人提交资料、查看宠物、跟踪进度
2. 宠物管理模块（核心功能）
宠物信息录入：基础信息（品种、年龄、性别、健康状况）、照片 / 视频、性格标签（粘人 / 独立）、领养要求（是否允许独居、是否有养宠经验）
宠物状态管理：待领养、审核中、已领养、暂不领养（生病 / 寄养），支持状态实时更新
3. 领养流程模块（核心闭环）
领养人申请：填写个人资料（住址、职业、是否有养宠史、家庭环境）、上传佐证材料（居住证明、收入证明）
审核流程：救助方初审（匹配领养要求）→ 管理员复核（资质真实性）→ 线下家访（可选，核心用于确认环境）
匹配与通知：系统自动匹配（按宠物要求筛选领养人），通过短信 / 站内信推送审核结果、领养须知
4. 资质审核与安全管控（关键）
双方资质校验：救助方需上传机构资质 / 个人救助证明；领养人需实名认证、佐证材料审核
黑名单管理：拉黑恶意弃养、虚假申请的领养人，拉黑虚假发布宠物的救助方
5. 后续跟进模块（保障宠物权益）
领养后反馈：领养人定期上传宠物生活照片 / 视频（按约定周期，如 1 周 / 1 个月）
异常处理：救助方 / 管理员可跟踪宠物状态，对弃养、虐待等情况介入，回收宠物
6. 辅助功能（提升使用体验）
检索筛选：领养人按品种、年龄、领养地区筛选宠物
消息中心：审核通知、领养提醒、后续跟进提醒
知识库：发布养宠常识、领养注意事项、宠物疾病基础护理（完成系统设计，要求开发语言JAVA，数据库：MySQL；）

---

## 5 项目介绍

一个完整的宠物领养管理平台，连接宠物救助方与领养人，实现"宠物展示 - 申请审核 - 领养匹配 - 后续跟进"的闭环。

### 功能特性

#### 核心功能
- **三类角色管理**：管理员、救助方/机构、领养人
- **宠物管理**：发布宠物信息、状态管理、照片上传
- **领养流程**：申请提交 → 救助方初审 → 管理员复核 → 家访确认
- **智能匹配**：根据宠物领养要求（养宠经验、独居环境、地区等）自动筛选推荐合适的领养人
- **资质审核**：实名认证、机构资质校验
- **黑名单管理**：恶意用户拉黑机制
- **后续跟进**：领养后定期反馈、异常处理、宠物回收
- **消息通知**：站内信推送审核结果、跟进提醒（多角色同步通知）
- **知识库**：养宠常识、领养须知、疾病护理

> **说明**：短信通知功能因需要接入第三方短信服务商（如阿里云、腾讯云短信）并产生费用，当前版本暂未实现。如需启用，可在 `MessageService` 中扩展短信发送接口，对接相应的短信 SDK。

#### 技术特性
- JWT 身份认证
- 全局异常处理
- 操作日志 AOP 记录
- 参数校验
- 文件上传（支持图片预览）
- Docker 容器化部署
- 多平台支持（ARM/X86）

### 技术栈

#### 后端
- Java 17
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- JWT (jjwt 0.12.3)
- Hutool 工具包

#### 前端
- Vue 3
- Vite 5
- Element Plus
- Pinia
- Vue Router 4
- Axios
- SCSS

#### 部署
- Docker
- Docker Compose
- Nginx

### 项目结构

```
├── backend/                          # 后端项目
│   ├── src/main/java/com/petadopt/
│   │   ├── PetAdoptApplication.java       # 启动类
│   │   ├── aspect/                        # AOP切面
│   │   │   ├── OperLog.java              # 操作日志注解
│   │   │   └── OperLogAspect.java        # 操作日志切面
│   │   ├── common/                        # 通用类
│   │   │   ├── enums/                    # 枚举
│   │   │   │   ├── ApplicationStatus.java
│   │   │   │   ├── PetStatus.java
│   │   │   │   └── RoleType.java
│   │   │   ├── exception/                # 异常处理
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── result/                   # 响应结果
│   │   │       ├── PageResult.java
│   │   │       └── Result.java
│   │   ├── config/                        # 配置类
│   │   │   ├── AuthInterceptor.java      # 认证拦截器
│   │   │   ├── MybatisPlusConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/                    # 控制器
│   │   │   ├── AdoptionController.java   # 领养申请
│   │   │   ├── BlacklistController.java  # 黑名单
│   │   │   ├── FileController.java       # 文件上传
│   │   │   ├── FollowUpController.java   # 跟进管理
│   │   │   ├── KnowledgeController.java  # 知识库
│   │   │   ├── MatchController.java      # 领养匹配
│   │   │   ├── MessageController.java    # 消息通知
│   │   │   ├── PetController.java        # 宠物管理
│   │   │   └── UserController.java       # 用户管理
│   │   ├── dto/                           # 数据传输对象
│   │   │   ├── AdoptionApplyDTO.java
│   │   │   ├── FollowUpDTO.java
│   │   │   ├── KnowledgeDTO.java
│   │   │   ├── LoginDTO.java
│   │   │   ├── PetDTO.java
│   │   │   ├── PetQueryDTO.java
│   │   │   ├── RegisterDTO.java
│   │   │   ├── ReviewDTO.java
│   │   │   └── VerifyDTO.java
│   │   ├── entity/                        # 实体类
│   │   │   ├── AdoptionApplication.java
│   │   │   ├── Blacklist.java
│   │   │   ├── FollowUpRecord.java
│   │   │   ├── Knowledge.java
│   │   │   ├── Message.java
│   │   │   ├── OperationLog.java
│   │   │   ├── Pet.java
│   │   │   └── User.java
│   │   ├── mapper/                        # MyBatis Mapper
│   │   │   ├── AdoptionApplicationMapper.java
│   │   │   ├── BlacklistMapper.java
│   │   │   ├── FollowUpRecordMapper.java
│   │   │   ├── KnowledgeMapper.java
│   │   │   ├── MessageMapper.java
│   │   │   ├── OperationLogMapper.java
│   │   │   ├── PetMapper.java
│   │   │   └── UserMapper.java
│   │   ├── service/                       # 服务层
│   │   │   ├── impl/                     # 服务实现
│   │   │   │   ├── AdoptionServiceImpl.java
│   │   │   │   ├── BlacklistServiceImpl.java
│   │   │   │   ├── FollowUpServiceImpl.java
│   │   │   │   ├── KnowledgeServiceImpl.java
│   │   │   │   ├── MatchServiceImpl.java
│   │   │   │   ├── MessageServiceImpl.java
│   │   │   │   ├── PetServiceImpl.java
│   │   │   │   └── UserServiceImpl.java
│   │   │   ├── AdoptionService.java
│   │   │   ├── BlacklistService.java
│   │   │   ├── FollowUpService.java
│   │   │   ├── KnowledgeService.java
│   │   │   ├── MatchService.java
│   │   │   ├── MessageService.java
│   │   │   ├── PetService.java
│   │   │   └── UserService.java
│   │   ├── util/                          # 工具类
│   │   │   ├── JwtUtil.java
│   │   │   └── UserContext.java
│   │   └── vo/                            # 视图对象
│   │       ├── AdopterMatchVO.java
│   │       ├── AdoptionApplicationVO.java
│   │       ├── FollowUpRecordVO.java
│   │       ├── LoginVO.java
│   │       ├── PetVO.java
│   │       └── UserVO.java
│   ├── src/main/resources/
│   │   ├── application.yml                # 配置文件
│   │   ├── schema.sql                     # 数据库表结构
│   │   ├── init-data.sql                  # 初始化数据
│   │   └── mysql-init/                    # MySQL初始化配置
│   │       └── my.cnf                     # MySQL配置文件
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── frontend-admin/                         # 前端管理后台
│   ├── src/
│   │   ├── api/                           # API接口
│   │   │   ├── adoption.js
│   │   │   ├── blacklist.js
│   │   │   ├── follow.js
│   │   │   ├── knowledge.js
│   │   │   ├── message.js
│   │   │   ├── pet.js
│   │   │   └── user.js
│   │   ├── components/                    # 公共组件(预留)
│   │   ├── router/                        # 路由
│   │   │   └── index.js
│   │   ├── store/                         # Pinia状态管理
│   │   │   └── user.js
│   │   ├── styles/                        # 样式
│   │   │   ├── main.scss
│   │   │   └── variables.scss
│   │   ├── utils/                         # 工具类
│   │   │   ├── eventBus.js
│   │   │   └── request.js
│   │   ├── views/                         # 页面
│   │   │   ├── adoption/                 # 领养管理
│   │   │   │   ├── AdoptionReview.vue    # 领养审核(管理员)
│   │   │   │   ├── MyApplications.vue    # 我的申请(领养人)
│   │   │   │   └── ReceivedApplications.vue # 收到的申请(救助方)
│   │   │   ├── follow/                   # 跟进管理
│   │   │   │   ├── FollowManage.vue      # 跟进管理(管理员)
│   │   │   │   └── MyFollowUp.vue        # 我的跟进(领养人)
│   │   │   ├── knowledge/                # 知识库
│   │   │   │   ├── KnowledgeDetail.vue
│   │   │   │   ├── KnowledgeList.vue
│   │   │   │   └── KnowledgeManage.vue
│   │   │   ├── pet/                      # 宠物管理
│   │   │   │   ├── MyPets.vue            # 我的宠物(救助方)
│   │   │   │   ├── PetDetail.vue         # 宠物详情
│   │   │   │   ├── PetList.vue           # 宠物列表
│   │   │   │   └── PetManage.vue         # 宠物管理(管理员)
│   │   │   ├── system/                   # 系统管理
│   │   │   │   └── Blacklist.vue         # 黑名单管理
│   │   │   ├── user/                     # 用户管理
│   │   │   │   ├── UserList.vue          # 用户列表(管理员)
│   │   │   │   └── UserProfile.vue       # 个人中心
│   │   │   ├── Home.vue                  # 首页
│   │   │   ├── Layout.vue                # 布局组件
│   │   │   ├── Login.vue                 # 登录
│   │   │   ├── Message.vue               # 消息中心
│   │   │   └── Register.vue              # 注册
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html                         # HTML入口
│   ├── nginx.conf                         # Nginx配置
│   ├── package.json
│   ├── vite.config.js
│   ├── Dockerfile
│   └── .dockerignore
├── docs/                                   # 文档
│   └── project_design.md                  # 项目设计文档
├── docker-compose.yml                      # Docker编排文件
├── .gitignore
└── README.md
```

### API 接口

#### 用户模块
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/user/register | 用户注册 |
| POST | /api/user/login | 用户登录 |
| GET | /api/user/info | 获取当前用户信息 |
| PUT | /api/user/update | 更新用户信息 |
| POST | /api/user/verify | 提交实名认证 |
| GET | /api/user/list | 用户列表(管理员) |
| PUT | /api/user/status | 更新用户状态 |

#### 宠物模块
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/pet/create | 发布宠物信息 |
| PUT | /api/pet/update | 更新宠物信息 |
| GET | /api/pet/detail/{id} | 宠物详情 |
| GET | /api/pet/list | 宠物列表(支持筛选) |
| PUT | /api/pet/status | 更新宠物状态 |
| DELETE | /api/pet/delete/{id} | 删除宠物 |
| GET | /api/pet/my-list | 我发布的宠物 |

#### 领养申请模块
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/adoption/apply | 提交领养申请 |
| GET | /api/adoption/my-applications | 我的申请列表 |
| GET | /api/adoption/received | 收到的申请(救助方) |
| GET | /api/adoption/pending-review | 待复核列表(管理员) |
| PUT | /api/adoption/rescue-review | 救助方审核 |
| PUT | /api/adoption/admin-review | 管理员复核 |
| PUT | /api/adoption/home-visit | 更新家访状态 |

#### 其他模块
详见 `docs/project_design.md`

### UI 规范

- **主色调**：#FF6B35 (温暖橙色)
- **成功色**：#52C41A
- **警告色**：#FAAD14
- **错误色**：#F5222D
- **圆角**：8px (基础)
- **间距**：8px / 16px / 24px

---

## License

MIT License
