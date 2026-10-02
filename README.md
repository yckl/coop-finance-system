# 🌾 农村合作社综合财务业务与资金风控审计管理系统
### Rural Cooperative Comprehensive Financial Management & Risk Audit System (CF-System)

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=flat-square&logo=springboot" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg?style=flat-square&logo=vuedotjs" alt="Vue 3" />
  <img src="https://img.shields.io/badge/Element%20Plus-2.x-409EFF.svg?style=flat-square&logo=elementplus" alt="Element Plus" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1.svg?style=flat-square&logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/Security-JWT%20RBAC-red.svg?style=flat-square" alt="Security" />
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License" />
</p>

---

## 📌 项目概述 (Executive Summary)

**Rural Cooperative Financial Management System (CF-System)** 是专为**农村信用合作社、农业产业联合体、乡村振兴集体经济组织**研发的**一站式综合财务业务中台与资金风控审计系统**。

针对农村金融与合作社业务中普遍存在的“存取款凭据易错漏、报销单据审批周期长、大额资金流动缺乏实时预警、股民社员缺乏透明对账渠道”等痛点，本项目构建了覆盖 **管理决策层、财务经办层、社员客户层** 的三权分立闭环业务体系，提供从**资金存取办理、智能账本核算、电子报销多级审批、大额交易风险预警到财务大屏经营分析与 AI 智能金融助手**的完整数字化解决方案。

---

## 🏛️ 系统架构设计 (System Architecture)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        前端展现层 (Presentation Layer)                 │
│         Vue 3 + Vite + Element Plus + Pinia + Axios + ECharts          │
├────────────────────────────────────────────────────────────────────────┤
│  [管理员决策总控台]        │  [财务专业经办工作台]    │  [社员自助服务门户]    │
│  - 经营数据大屏 / 趋势预测 │  - 存取款核算 / 余额对账 │  - 个人资产卡 / 收支明细│
│  - 大额风险预警 / 审计追踪 │  - 多级报销初复审 / 流水 │  - 电子报销申请 / AI问答│
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ RESTful API / JWT Token 拦截鉴权
┌────────────────────────────────────▼───────────────────────────────────┐
│                        后端业务驱动层 (Business Layer)                 │
│              Spring Boot 3 + Java 17 + Spring MVC + JWT                │
├────────────────────────────────────────────────────────────────────────┤
│  [安全与权限引擎]   基于 RBAC 三权分立的角色权限隔离 / AuthInterceptor 校验    │
│  [核心业务模块]     存取款原子事务引擎 / 报销多级工作流引擎 / 统一流水账本     │
│  [风控与审计中枢]   大额异常存取风控规则器 / 全量操作留痕审计 (Audit Log)     │
│  [智能化金融助理]   农业金融智能问答模型 (AskAi Service) / 报销单智能辅助填报 │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ JDBC / MyBatis Data Persistence
┌────────────────────────────────────▼───────────────────────────────────┐
│                        数据持久化层 (Storage Layer)                    │
│                      MySQL 8.0+ (InnoDB Storage Engine)                │
├────────────────────────────────────────────────────────────────────────┤
│  - 22+ 张严格范式化核心数据表 (账户账本、流水日志、报销主子表、风控预警等)   │
│  - 完整级联索引与事务隔离保证 (ACID 强一致性金融记账模型)              │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 核心业务创新与工程亮点 (Key Innovations)

### 1. 严格金融级双向记账与流水闭环 (ACID Consistency)
* 每一笔合作社存款、取款与利息变动均通过原子事务驱动，同步触发**账户余额更新（`account_balance`）**与**不可篡改的交易审计流水（`transaction_record`）**。
* 彻底杜绝账实不符、单边账异常，支持实时按凭证编号溯源穿透。

### 2. 合作社特色大额资金风险预警中枢 (Risk Warning)
* 针对农村金融洗钱防范与现金异常挤兑风险，系统内置多维度风险规则引擎：
  * **单笔大额阀值监控**：单笔资金存取突破安全限额自动高亮触发风控标旗；
  * **日累计额度频次分析**：对短时间内连续高频出入账的社员账户进行智能预警阻断；
  * **管理员总控处置流**：风控专员可在预警驾驶舱一键核查凭证、冻结或解除预警。

### 3. 多级电子报销审批工作流 (Reimbursement Workflow)
* 覆盖合作社涉农物资采购、农机维修、差旅差贴等日常经营报销全链路：
  * 具备单据暂存、发票附件上传、多级审批流转（提交 ➔ 财务初审 ➔ 主管复核 ➔ 出纳打款）；
  * 具备完整的打回修改、审批意见归档与财务凭证留痕机制。

### 4. 嵌入式 AI 乡村金融问答引擎 (Embedded AI Assistant)
* 前端无缝嵌入悬浮式 AI 问答终端，为广大农村社员提供合作社分红政策、涉农贷款利息测算、报销流程答疑等智能咨询服务。

---

## 👥 三端协同业务矩阵 (Role Feature Matrix)

| 功能板块 | 👑 管理员端 (Admin) | 💼 财务人员端 (Finance Staff) | 🧑 社员客户门户 (User Portal) |
| :--- | :--- | :--- | :--- |
| **资金业务经办** | 宏观监控全社存款/取款/利息走势 | 柜面存款办理、取款出纳、余额对账 | 个人账户资产查询、流水账单明细导出 |
| **报销全链路** | 全局报销支出审批总控、超标拦截 | 经办报销初审、单据核验、打款出账 | 在线填报报销申请、跟踪审核节点进度 |
| **风控与安全** | 大额异常告警、安全审计日志查看 | 柜面业务大额预警二次身份复核 | 个人密码修改、登录设备与会话安全 |
| **运营与信息** | 字典项编排、全社公告发布、留言监管 | 处理社员业务咨询留言、公告查阅 | 查阅合作社公告、提交涉农咨询与反馈 |
| **数据大盘** | 首页决策大屏、合作社资产负债结构表 | 财务收支分析报表、科目平衡图表 | 个人月度账单月报、收支统计可视化 |
| **AI 金融助理** | 监管 AI 问答日志与敏感词命中 | 辅助核对报销科目与财务法规标准 | 24 小时社员政策智能咨询助手 |

---

## 🛠️ 技术选型栈 (Tech Stack)

### 前端技术栈 (Frontend)
* **核心框架**：Vue 3.x (SFC 架构)
* **构建工具**：Vite
* **组件系统**：Element Plus 2.x
* **网络请求**：Axios (封装统一拦截器与 Token 刷新机制)
* **图表可视化**：ECharts (资产大屏、收支趋势折线图、结构饼图)

### 后端技术栈 (Backend)
* **服务基座**：Spring Boot 3.x (Java 17 LTS)
* **持久化**：Spring Data JDBC / MyBatis 规范设计
* **安全鉴权**：JJWT + 自定义 HandlerInterceptor 安全拦截器链
* **数据库**：MySQL 8.0+ (支持 UTF8MB4 字符集与高精度 DECIMAL 货币类型)
* **项目构建**：Maven 3.8+

---

## 📂 源码工程目录结构 (Project Layout)

```text
coop-finance-system/
├── backend/                               # Spring Boot 3 后端工程
│   ├── src/main/java/com/coop/
│   │   ├── config/                        # WebMVC、CORS 跨域、拦截器配置
│   │   ├── controller/                    # 业务接口控制层
│   │   │   ├── AdminResourceController.java   # 管理员总控、审计与大额风控接口
│   │   │   ├── AuthController.java            # 登录、鉴权与 Token 颁发接口
│   │   │   └── PortalController.java          # 柜面经办与社员门户接口
│   │   ├── dto/                           # 请求载荷模型 (存取款、报销、AI 问答)
│   │   ├── mapper/                        # 数据访问 Mapper 接口
│   │   ├── service/                       # 业务逻辑服务层 (Real / Mock 灵活切换)
│   │   ├── util/                          # JWT 凭证与通用工具库
│   │   └── CoopFinanceSystemApplication.java  # 后端启动入口
│   └── src/main/resources/
│       └── application.yml                # 数据库源与系统环境变量配置
├── frontend/                              # Vue 3 前端工程
│   ├── src/
│   │   ├── api/                           # Axios 统一 API 模块封装
│   │   ├── components/                    # AI 助手小窗、ECharts 图表公共组件
│   │   ├── router/                        # 三端独立隔离的路由定义与导航守卫
│   │   └── views/                         # 视图层 (三端独立工作区)
│   │       ├── admin/                     # 管理员驾驶舱与审计后台
│   │       ├── finance/                   # 财务柜面业务经办工作台
│   │       └── user/                      # 合作社社员客户自助端
│   ├── package.json
│   └── vite.config.js
├── db/
│   └── init.sql                           # 完整 22 张业务表建表与演示数据脚本
└── README.md                              # 工业级项目说明文档
```

---

## 🚀 快速启动与部署指南 (Quick Start)

### 1. 环境准备 (Prerequisites)
* **Java 运行环境**：JDK 17 LTS 或更高
* **Node.js 环境**：Node 18.x 或更高
* **数据库**：MySQL 8.0+

---

### 2. 数据库初始化 (Database Setup)
1. 登录本地或服务器 MySQL 实例：
   ```bash
   mysql -u root -p
   ```
2. 执行工程根目录下的 `db/init.sql` 脚本：
   ```sql
   source /path/to/coop-finance-system/db/init.sql;
   ```
   > 脚本将自动创建 `coop_finance_db` 数据库、22 张核心业务表以及初始角色与测试社员账户。

---

### 3. 后端服务启动 (Backend Setup)
1. 检查 `backend/src/main/resources/application.yml` 数据库密码（默认：`123456`）：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://127.0.0.1:3306/coop_finance_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: root
       password: 你的MySQL密码
   ```
2. 编译并运行 Spring Boot 工程：
   ```bash
   cd backend
   mvn clean spring-boot:run
   ```
3. 后端服务启动于：`http://127.0.0.1:8080`。

---

### 4. 前端服务启动 (Frontend Setup)
1. 安装依赖包：
   ```bash
   cd frontend
   npm install
   ```
2. 启动本地开发服务器：
   ```bash
   npm run dev
   ```
3. 访问前端控制台输出链接：`http://127.0.0.1:5173`。

---

## 🔑 系统预置演示账号体系 (Demo Accounts)

系统支持三套独立角色账户体系，可登录对应端进行全流程业务体验：

| 角色类型 | 登录账号 | 初始密码 | 对应工作台路径 | 体验功能重点 |
| :--- | :--- | :--- | :--- | :--- |
| **系统超级管理员** | `admin` | `123456` | `/admin/dashboard` | 合作社经营数据大屏、风险预警拦截处置、全量操作留痕审计 |
| **财务柜员/经办** | `caiwu` | `123456` | `/finance/dashboard` | 柜台资金存取记账、大额资金初核、报销单据审批与打款 |
| **合作社社员/客户** | `user` | `123456` | `/user/dashboard` | 个人账本资产看板、收支流水对账、在线报销提交、AI 金融咨询 |

---

## 📄 开源许可证 (License)

本项目遵循 [MIT License](LICENSE) 开源协议。
