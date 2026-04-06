export const portalMenus = {
  admin: [
    { path: '/admin/dashboard', title: '首页驾驶舱', view: 'dashboard' },
    { path: '/admin/admin-accounts', title: '管理员账号与权限管理', view: 'module', moduleKey: 'admin-accounts' },
    { path: '/admin/finance-staff', title: '财务人员管理', view: 'module', moduleKey: 'finance-staff' },
    { path: '/admin/users', title: '用户管理', view: 'module', moduleKey: 'users' },
    { path: '/admin/dictionaries', title: '字典管理', view: 'module', moduleKey: 'dictionaries' },
    { path: '/admin/announcements', title: '公告管理', view: 'module', moduleKey: 'announcements' },
    { path: '/admin/messages', title: '留言与反馈管理', view: 'module', moduleKey: 'messages' },
    { path: '/admin/reimbursements', title: '报销总控', view: 'module', moduleKey: 'reimbursements' },
    { path: '/admin/analysis', title: '财务总览与经营分析', view: 'module', moduleKey: 'analysis' },
    { path: '/admin/security', title: '系统安全与审计', view: 'module', moduleKey: 'security' }
  ],
  finance: [
    { path: '/finance/dashboard', title: '工作台首页', view: 'dashboard' },
    { path: '/finance/deposits', title: '存款业务管理', view: 'module', moduleKey: 'deposits', transactionKind: 'deposit' },
    { path: '/finance/withdrawals', title: '取款业务管理', view: 'module', moduleKey: 'withdrawals', transactionKind: 'withdraw' },
    { path: '/finance/balances', title: '余额查询', view: 'module', moduleKey: 'balances' },
    { path: '/finance/transactions', title: '统一交易流水', view: 'module', moduleKey: 'transactions' },
    { path: '/finance/reimbursements', title: '报销业务处理', view: 'module', moduleKey: 'reimbursements' },
    { path: '/finance/announcements', title: '公告查看', view: 'module', moduleKey: 'announcements' },
    { path: '/finance/messages', title: '留言处理', view: 'module', moduleKey: 'messages' },
    { path: '/finance/analysis', title: '报表分析 + 可视化分析', view: 'module', moduleKey: 'analysis' },
    { path: '/finance/ai', title: 'AI 助手', view: 'ai' },
    { path: '/finance/profile', title: '柜员配置与个人信息组', view: 'module', moduleKey: 'profile' }
  ],
  user: [
    { path: '/user/dashboard', title: '首页概览', view: 'dashboard' },
    { path: '/user/profile', title: '个人信息管理', view: 'module', moduleKey: 'profile' },
    { path: '/user/balance', title: '余额查询', view: 'module', moduleKey: 'balance' },
    { path: '/user/deposits', title: '存款记录查询', view: 'module', moduleKey: 'deposits' },
    { path: '/user/withdrawals', title: '取款记录查询', view: 'module', moduleKey: 'withdrawals' },
    { path: '/user/reimbursements', title: '报销申请', view: 'module', moduleKey: 'reimbursements' },
    { path: '/user/announcements', title: '公告查看', view: 'module', moduleKey: 'announcements' },
    { path: '/user/messages', title: '在线留言/咨询', view: 'module', moduleKey: 'messages' },
    { path: '/user/notifications', title: '我的消息中心', view: 'module', moduleKey: 'notifications' },
    { path: '/user/ai', title: 'AI 智能问答', view: 'ai' }
  ]
}
