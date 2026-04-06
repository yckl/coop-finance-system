CREATE DATABASE IF NOT EXISTS coop_finance_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE coop_finance_db;

DROP TABLE IF EXISTS ai_session;
DROP TABLE IF EXISTS report_snapshot;
DROP TABLE IF EXISTS system_config;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS risk_warning;
DROP TABLE IF EXISTS message_center;
DROP TABLE IF EXISTS reimbursement_audit_record;
DROP TABLE IF EXISTS reimbursement_order;
DROP TABLE IF EXISTS transaction_record;
DROP TABLE IF EXISTS account_balance;
DROP TABLE IF EXISTS finance_staff_profile;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS sys_notice;
DROP TABLE IF EXISTS dictionary_item;
DROP TABLE IF EXISTS dictionary_type;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='系统角色表';

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    id_card VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
    gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    account_status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '账号状态',
    risk_level VARCHAR(20) NOT NULL DEFAULT '低' COMMENT '风险等级',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sys_user_username (username)
) COMMENT='系统用户表';

CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    permission_code VARCHAR(80) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(80) NOT NULL COMMENT '权限名称',
    module_name VARCHAR(80) NOT NULL COMMENT '所属模块',
    action_name VARCHAR(50) NOT NULL COMMENT '操作动作',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='系统权限表';

CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    permission_code VARCHAR(80) NOT NULL COMMENT '权限编码',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='角色权限关联表';

CREATE TABLE dictionary_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    type_name VARCHAR(80) NOT NULL COMMENT '字典类型名称',
    type_status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='字典类型表';

CREATE TABLE dictionary_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    item_label VARCHAR(80) NOT NULL COMMENT '字典标签',
    item_value VARCHAR(80) NOT NULL COMMENT '字典值',
    item_sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    item_status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='字典数据项表';

CREATE TABLE sys_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    notice_title VARCHAR(200) NOT NULL COMMENT '公告标题',
    notice_type VARCHAR(50) NOT NULL COMMENT '公告类型',
    notice_content TEXT NOT NULL COMMENT '公告内容',
    visible_scope VARCHAR(100) NOT NULL COMMENT '可见范围',
    top_flag VARCHAR(10) NOT NULL DEFAULT '否' COMMENT '是否置顶',
    publish_status VARCHAR(20) NOT NULL DEFAULT '草稿' COMMENT '发布状态',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    expire_time DATETIME DEFAULT NULL COMMENT '失效时间',
    read_count INT NOT NULL DEFAULT 0 COMMENT '阅读人数',
    publisher_name VARCHAR(50) NOT NULL COMMENT '发布人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='系统公告表';

CREATE TABLE user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_no VARCHAR(50) NOT NULL COMMENT '用户编号',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    id_card VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
    address VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
    account_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '账户状态',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    key_customer_flag VARCHAR(10) NOT NULL DEFAULT '否' COMMENT '是否重点客户',
    blacklist_flag VARCHAR(10) NOT NULL DEFAULT '否' COMMENT '是否黑名单',
    risk_level VARCHAR(20) NOT NULL DEFAULT '低' COMMENT '风险等级'
) COMMENT='普通用户档案表';

CREATE TABLE finance_staff_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    staff_no VARCHAR(50) NOT NULL COMMENT '财务人员工号',
    staff_name VARCHAR(50) NOT NULL COMMENT '财务人员姓名',
    gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    id_card VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
    position_name VARCHAR(80) NOT NULL COMMENT '岗位名称',
    branch_name VARCHAR(100) NOT NULL COMMENT '所属网点',
    hire_date DATE DEFAULT NULL COMMENT '入职日期',
    employment_status VARCHAR(20) NOT NULL DEFAULT '在职' COMMENT '在职状态',
    login_username VARCHAR(50) DEFAULT NULL COMMENT '绑定账号',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注'
) COMMENT='财务人员档案表';

CREATE TABLE account_balance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_no VARCHAR(50) NOT NULL COMMENT '用户编号',
    account_no VARCHAR(50) NOT NULL COMMENT '账户编号',
    total_balance DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '账户总余额',
    frozen_balance DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '冻结余额',
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '可用余额',
    account_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '账户状态',
    risk_level VARCHAR(20) NOT NULL DEFAULT '低' COMMENT '风险等级',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_account_balance_account_no (account_no)
) COMMENT='账户余额表';

CREATE TABLE transaction_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    serial_no VARCHAR(50) NOT NULL COMMENT '交易流水号',
    user_no VARCHAR(50) NOT NULL COMMENT '用户编号',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    amount DECIMAL(18,2) NOT NULL COMMENT '交易金额',
    balance_after DECIMAL(18,2) NOT NULL COMMENT '交易后余额',
    operator_name VARCHAR(50) NOT NULL COMMENT '经办人',
    transaction_status VARCHAR(20) NOT NULL DEFAULT '成功' COMMENT '交易状态',
    warning_level VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '预警等级',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_transaction_record_serial_no (serial_no)
) COMMENT='统一交易流水表';

CREATE TABLE reimbursement_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    reimbursement_no VARCHAR(50) NOT NULL COMMENT '报销单号',
    applicant_no VARCHAR(50) NOT NULL COMMENT '申请人编号',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    reimbursement_type VARCHAR(50) NOT NULL COMMENT '报销类型',
    reimbursement_amount DECIMAL(18,2) NOT NULL COMMENT '报销金额',
    attachment_url VARCHAR(255) DEFAULT NULL COMMENT '附件地址',
    current_status VARCHAR(20) NOT NULL DEFAULT '待审核' COMMENT '当前状态',
    current_auditor VARCHAR(50) DEFAULT NULL COMMENT '当前审核人',
    audit_comment VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_reimbursement_order_no (reimbursement_no)
) COMMENT='报销申请表';

CREATE TABLE reimbursement_audit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    reimbursement_no VARCHAR(50) NOT NULL COMMENT '报销单号',
    audit_node VARCHAR(50) NOT NULL COMMENT '审核节点',
    auditor_name VARCHAR(50) NOT NULL COMMENT '审核人',
    audit_result VARCHAR(20) NOT NULL COMMENT '审核结果',
    audit_comment VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
    audit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间'
) COMMENT='报销审核记录表';

CREATE TABLE message_center (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sender_no VARCHAR(50) NOT NULL COMMENT '发送人编号',
    sender_name VARCHAR(50) NOT NULL COMMENT '发送人姓名',
    receiver_role VARCHAR(50) NOT NULL COMMENT '接收角色',
    message_type VARCHAR(50) NOT NULL COMMENT '消息类型',
    message_title VARCHAR(150) NOT NULL COMMENT '消息标题',
    message_content TEXT NOT NULL COMMENT '消息内容',
    process_status VARCHAR(20) NOT NULL DEFAULT '未处理' COMMENT '处理状态',
    read_status VARCHAR(20) NOT NULL DEFAULT '未读' COMMENT '阅读状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='消息中心与留言反馈表';

CREATE TABLE risk_warning (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warning_code VARCHAR(50) NOT NULL COMMENT '预警编号',
    warning_type VARCHAR(80) NOT NULL COMMENT '预警类型',
    warning_level VARCHAR(20) NOT NULL COMMENT '预警等级',
    warning_object VARCHAR(100) NOT NULL COMMENT '预警对象',
    warning_content VARCHAR(255) NOT NULL COMMENT '预警内容',
    process_status VARCHAR(20) NOT NULL DEFAULT '未处理' COMMENT '处理状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='风险预警表';

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人',
    module_name VARCHAR(100) NOT NULL COMMENT '功能模块',
    action_name VARCHAR(100) NOT NULL COMMENT '操作动作',
    action_content VARCHAR(255) NOT NULL COMMENT '操作内容',
    risk_level VARCHAR(20) NOT NULL DEFAULT '低' COMMENT '风险等级',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='系统审计日志表';

CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    config_code VARCHAR(80) NOT NULL COMMENT '配置编码',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    config_group VARCHAR(80) NOT NULL COMMENT '配置分组',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='系统参数配置表';

CREATE TABLE report_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    snapshot_name VARCHAR(100) NOT NULL COMMENT '报表名称',
    report_type VARCHAR(50) NOT NULL COMMENT '报表类型',
    report_period VARCHAR(100) NOT NULL COMMENT '统计区间',
    report_summary VARCHAR(255) NOT NULL COMMENT '报表摘要',
    export_status VARCHAR(20) NOT NULL DEFAULT '未导出' COMMENT '导出状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='报表快照表';

CREATE TABLE ai_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    session_no VARCHAR(50) NOT NULL COMMENT '会话编号',
    user_role VARCHAR(50) NOT NULL COMMENT '用户角色',
    user_name VARCHAR(50) NOT NULL COMMENT '用户名称',
    question_text TEXT NOT NULL COMMENT '提问内容',
    answer_text TEXT NOT NULL COMMENT '回答内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='AI 助手会话表';

INSERT INTO sys_role (role_code, role_name, role_desc) VALUES
('ADMIN', '管理员', '系统控制台角色'),
('FINANCE', '财务人员', '负责存款取款报销审核'),
('USER', '普通用户', '负责自助查询与申请');

INSERT INTO sys_user (username, password, real_name, role_code, phone, id_card, gender, account_status, risk_level, last_login_time) VALUES
('admin', '123456', '系统管理员', 'ADMIN', '13800000001', '410100199001010001', '男', '启用', '低', NOW()),
('caiwu', '123456', '张会计', 'FINANCE', '13800000002', '410100199002020002', '女', '启用', '低', NOW()),
('user', '123456', '李玉兰', 'USER', '13800000003', '410100199003030003', '女', '启用', '低', NOW());

INSERT INTO sys_permission (permission_code, permission_name, module_name, action_name) VALUES
('ADMIN_VIEW', '管理员查看', '管理员账号与权限管理', '查看'),
('ADMIN_ADD', '管理员新增', '管理员账号与权限管理', '新增'),
('ADMIN_EDIT', '管理员编辑', '管理员账号与权限管理', '编辑'),
('FINANCE_AUDIT', '财务审核', '报销与审核总控', '审核'),
('REPORT_EXPORT', '报表导出', '报表分析', '导出');

INSERT INTO sys_role_permission (role_code, permission_code) VALUES
('ADMIN', 'ADMIN_VIEW'),
('ADMIN', 'ADMIN_ADD'),
('ADMIN', 'ADMIN_EDIT'),
('FINANCE', 'FINANCE_AUDIT'),
('ADMIN', 'REPORT_EXPORT'),
('FINANCE', 'REPORT_EXPORT');

INSERT INTO dictionary_type (type_code, type_name, type_status, remark) VALUES
('DEPOSIT_TYPE', '存款类型', '启用', '系统预置存款类型'),
('WITHDRAW_TYPE', '取款类型', '启用', '系统预置取款类型'),
('REIMBURSE_TYPE', '报销类型', '启用', '系统预置报销类型'),
('NOTICE_TYPE', '公告类型', '启用', '系统预置公告类型');

INSERT INTO dictionary_item (type_code, item_label, item_value, item_sort, item_status) VALUES
('DEPOSIT_TYPE', '定期存款', '定期存款', 1, '启用'),
('DEPOSIT_TYPE', '活期存款', '活期存款', 2, '启用'),
('WITHDRAW_TYPE', '现金取款', '现金取款', 1, '启用'),
('WITHDRAW_TYPE', '转账取款', '转账取款', 2, '启用'),
('REIMBURSE_TYPE', '农资采购', '农资采购', 1, '启用'),
('REIMBURSE_TYPE', '差旅报销', '差旅报销', 2, '启用'),
('NOTICE_TYPE', '业务公告', '业务公告', 1, '启用'),
('NOTICE_TYPE', '制度公告', '制度公告', 2, '启用');

INSERT INTO user_profile (user_no, user_name, gender, phone, id_card, address, account_status, register_time, key_customer_flag, blacklist_flag, risk_level) VALUES
('U1001', '李玉兰', '女', '13900001111', '410100199101010011', '河南省郑州市高新区', '正常', NOW(), '是', '否', '低'),
('U1002', '王建国', '男', '13900002222', '410100199102020022', '河南省开封市兰考县', '正常', NOW(), '否', '否', '中'),
('U1003', '赵秋梅', '女', '13900003333', '410100199103030033', '河南省南阳市邓州市', '冻结', NOW(), '否', '否', '高');

INSERT INTO finance_staff_profile (staff_no, staff_name, gender, phone, id_card, position_name, branch_name, hire_date, employment_status, login_username, remark) VALUES
('CW001', '张会计', '女', '13800001111', '410100199104040044', '柜面财务', '中心网点', '2022-03-01', '在职', 'caiwu', '负责存款取款业务'),
('CW002', '刘出纳', '男', '13800002222', '410100199105050055', '报销审核', '北郊网点', '2021-08-15', '在职', NULL, '负责报销审核');

INSERT INTO account_balance (user_no, account_no, total_balance, frozen_balance, available_balance, account_status, risk_level) VALUES
('U1001', 'ACC1001', 85600.00, 0.00, 85600.00, '正常', '低'),
('U1002', 'ACC1002', 125000.00, 0.00, 125000.00, '正常', '中'),
('U1003', 'ACC1003', 46800.00, 1200.00, 45600.00, '冻结', '高');

INSERT INTO transaction_record (serial_no, user_no, user_name, transaction_type, business_type, amount, balance_after, operator_name, transaction_status, warning_level, remark) VALUES
('TX202604070001', 'U1001', '李玉兰', '存款', '春耕补贴入账', 6000.00, 85600.00, '张会计', '成功', '正常', '春耕专项资金'),
('TX202604070002', 'U1002', '王建国', '取款', '农机采购支出', 52000.00, 125000.00, '刘出纳', '成功', '高', '大额取款预警');

INSERT INTO reimbursement_order (reimbursement_no, applicant_no, applicant_name, reimbursement_type, reimbursement_amount, attachment_url, current_status, current_auditor, audit_comment) VALUES
('BX202604070001', 'U1001', '李玉兰', '农资采购', 1860.00, '/upload/demo-1.jpg', '待审核', '张会计', '等待财务审核'),
('BX202604070002', 'U1002', '王建国', '差旅报销', 980.00, '/upload/demo-2.jpg', '已通过', '系统管理员', '管理员终审通过');

INSERT INTO reimbursement_audit_record (reimbursement_no, audit_node, auditor_name, audit_result, audit_comment) VALUES
('BX202604070001', '财务初审', '张会计', '待处理', '待审核'),
('BX202604070002', '管理员终审', '系统管理员', '通过', '票据齐全，准予报销');

INSERT INTO message_center (sender_no, sender_name, receiver_role, message_type, message_title, message_content, process_status, read_status) VALUES
('U1001', '李玉兰', 'FINANCE', '咨询', '报销附件上传失败', '请问附件上传失败后如何补传？', '未处理', '未读'),
('U1002', '王建国', 'ADMIN', '投诉', '取款等待时间过长', '希望增加高峰时段柜面窗口。', '处理中', '已读');

INSERT INTO risk_warning (warning_code, warning_type, warning_level, warning_object, warning_content, process_status) VALUES
('WARN20260407001', '大额取款预警', '高', '王建国', '当日单笔取款超过 50000 元', '未处理'),
('WARN20260407002', '异常报销预警', '中', '李玉兰', '同类型报销金额连续上涨', '处理中');

INSERT INTO audit_log (log_type, operator_name, module_name, action_name, action_content, risk_level, ip_address) VALUES
('登录日志', 'admin', '系统登录', '登录', '管理员登录成功', '低', '127.0.0.1'),
('业务日志', '张会计', '存款办理', '新增', '新增春耕补贴入账流水 TX202604070001', '低', '127.0.0.1'),
('风险日志', '系统', '风险预警中心', '预警', '检测到大额取款预警', '高', '127.0.0.1');

INSERT INTO system_config (config_code, config_name, config_value, config_group, remark) VALUES
('PASSWORD_POLICY', '密码策略', '长度不少于6位，包含数字', '安全配置', '可在系统配置中调整'),
('SESSION_TIMEOUT', '会话超时时间', '30', '安全配置', '单位为分钟'),
('AI_MODEL_NAME', 'AI模型名称', '中文业务助手演示模型', 'AI配置', '用于前端 AI 助手演示'),
('EXPORT_DEFAULT_FORMAT', '默认导出格式', 'Excel', '报表配置', '报表导出默认格式');

INSERT INTO report_snapshot (snapshot_name, report_type, report_period, report_summary, export_status) VALUES
('月度收支汇总报表', '月报', '2026-04-01 至 2026-04-30', '存款增长明显，净流入上升', '可导出'),
('报销审核统计报表', '专项报表', '2026-04-01 至 2026-04-30', '待审核 3 单，异常 1 单', '已生成');

INSERT INTO ai_session (session_no, user_role, user_name, question_text, answer_text) VALUES
('AI202604070001', 'ADMIN', '系统管理员', '请生成本月经营摘要', '本月净流入持续增长，建议重点复核大额交易与异常报销。'),
('AI202604070002', 'USER', '李玉兰', '报销申请如何提交？', '请在用户端报销申请页面填写金额、类型并上传票据附件。');
