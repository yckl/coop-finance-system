export const portalMenus = {
  admin: [
    { path: '/admin/dashboard', title: '首页驾驶舱', view: 'dashboard', icon: 'Odometer' },
    { path: '/admin/admin-accounts', title: '管理员账号与权限管理', view: 'module', moduleKey: 'admin-accounts', icon: 'UserFilled' },
    { path: '/admin/finance-staff', title: '财务人员管理', view: 'module', moduleKey: 'finance-staff', icon: 'Avatar' },
    { path: '/admin/users', title: '用户管理', view: 'module', moduleKey: 'users', icon: 'User' },
    { path: '/admin/dictionaries', title: '字典管理', view: 'module', moduleKey: 'dictionaries', icon: 'Collection' },
    { path: '/admin/announcements', title: '公告管理', view: 'module', moduleKey: 'announcements', icon: 'Notification' },
    { path: '/admin/messages', title: '留言与反馈管理', view: 'module', moduleKey: 'messages', icon: 'Message' },
    { path: '/admin/reimbursements', title: '报销总控', view: 'module', moduleKey: 'reimbursements', icon: 'Document' },
    { path: '/admin/analysis', title: '财务总览与经营分析', view: 'module', moduleKey: 'analysis', icon: 'DataLine' },
    { path: '/admin/security', title: '系统安全与审计', view: 'module', moduleKey: 'security', icon: 'Shield' }
  ],
  finance: [
    { path: '/finance/dashboard', title: '工作台首页', view: 'dashboard', icon: 'Odometer' },
    { path: '/finance/deposits', title: '存款业务管理', view: 'module', moduleKey: 'deposits', transactionKind: 'deposit', icon: 'Money' },
    { path: '/finance/withdrawals', title: '取款业务管理', view: 'module', moduleKey: 'withdrawals', transactionKind: 'withdraw', icon: 'BankCard' },
    { path: '/finance/balances', title: '余额查询', view: 'module', moduleKey: 'balances', icon: 'Wallet' },
    { path: '/finance/transactions', title: '统一交易流水', view: 'module', moduleKey: 'transactions', icon: 'List' },
    { path: '/finance/reimbursements', title: '报销业务处理', view: 'module', moduleKey: 'reimbursements', icon: 'Ticket' },
    { path: '/finance/announcements', title: '公告查看', view: 'module', moduleKey: 'announcements', icon: 'Notification' },
    { path: '/finance/messages', title: '留言处理', view: 'module', moduleKey: 'messages', icon: 'ChatDotSquare' },
    { path: '/finance/analysis', title: '报表分析 + 可视化分析', view: 'module', moduleKey: 'analysis', icon: 'PieChart' },
    { path: '/finance/ai', title: 'AI 助手', view: 'ai', icon: 'MagicStick' },
    { path: '/finance/profile', title: '业务员配置设置', view: 'module', moduleKey: 'profile', icon: 'Setting' }
  ],
  user: [
    { path: '/user/dashboard', title: '首页概览', view: 'dashboard', icon: 'HomeFilled' },
    { path: '/user/profile', title: '个人信息管理', view: 'module', moduleKey: 'profile', icon: 'UserFilled' },
    { path: '/user/balance', title: '余额查询', view: 'module', moduleKey: 'balance', icon: 'Wallet' },
    { path: '/user/deposits', title: '存款记录查询', view: 'module', moduleKey: 'deposits', icon: 'Money' },
    { path: '/user/withdrawals', title: '取款记录查询', view: 'module', moduleKey: 'withdrawals', icon: 'BankCard' },
    { path: '/user/reimbursements', title: '报销申请', view: 'module', moduleKey: 'reimbursements', icon: 'DocumentAdd' },
    { path: '/user/announcements', title: '公告查看', view: 'module', moduleKey: 'announcements', icon: 'Notification' },
    { path: '/user/messages', title: '在线留言/咨询', view: 'module', moduleKey: 'messages', icon: 'Message' },
    { path: '/user/notifications', title: '我的消息中心', view: 'module', moduleKey: 'notifications', icon: 'Bell' },
    { path: '/user/ai', title: 'AI 智能问答', view: 'ai', icon: 'Service' }
  ]
}
