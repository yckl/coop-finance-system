const fs = require('fs');
const path = require('path');

const DIRECTORIES = [
  path.join(__dirname, 'frontend/src'),
  path.join(__dirname, 'backend/src')
];

const REPLACEMENTS = {
  "财务审核总控": "财务审核",
  "行政总控": "管理员",
  "财务审核": "财务人员",
  "财务人员（.*）": "财务人员（$1）", // if context Needs it, but let's stick to simple strings
  "平民储户": "普通用户",
  "黑天白夜": "主题颜色",
  "扫描锚点身份": "核验用户身份",
  "读取取款人物理凭证": "验证取款人身份凭证",
  "可通过扫描芯片或物理介质键入检索系统映射体": "可通过扫描证件或手动输入查询用户信息",
  "出款物理指令参数": "出款指令参数",
  "账务抽水操作穿透！发票就绪！": "账务处理完成！发票已生成！",
  "提取出款物理票据快照": "提取出款电子票据",
  "终端柜台签批": "柜台处理人",
  "关闭沙盒": "关闭窗口",
  "穿透刷新": "刷新数据",
  "防爆校验主体": "审核验签主体",
  "物理大厅开卡、反洗钱身份侦测、与终端设备解绑中心": "包含用户开户、身份验证与设备解绑等功能",
  "涉及到储户极点数据，已开始全域沙盒监控": "涉及敏感数据，已开启严格安全监控",
  "离线沙盒安全扫描通过。所有人民银行对齐数据已静默合并入列": "系统安全扫描通过。所有数据已同步完成",
  "物理卡片读取或人工索检": "卡片读取或人工检索",
  "终端机器附言": "终端机备注",
  "资金穿透上链完成": "资金处理完成",
  "提取物理票据快照": "提取电子票据",
  "宏观透视所有资金往来游丝，每笔交易均可向下钻取其物理加密指纹": "宏观查看所有资金往来明细，每笔交易均可查看其安全校验指纹",
  "内部统一交易物理备份印册": "内部统一交易安全备份印册",
  "抛出为 XLSX 快照文件至本地，物理沙盒保存成功": "已成功导出 XLSX 文件至本地",
  "天网系统安全与防爆审计枢纽": "系统安全与审计中心",
  "全网物理截断": "全网紧急断网",
  "激活红色熔断预案": "激活紧急熔断预案",
  "沙盒误报": "系统误报",
  "物理时间戳": "时间戳",
  "强制令当前全网所有终端在线人员下线，并在下次登录时强制要求使用生物识别及重置高强度口令": "强制全网所有在线人员下线，并在下次登录时要求验证身份及重置密码",
  "骨干网路熔断": "骨干网络阻断",
  "高危网段熔断": "高危网段阻断",
  "物理加密验证完成，【(.*?)】指令已送达底层 CPU 并执行": "安全验证完成，【$1】指令已执行",
  "使用终端摄像头扫入发票纸基": "使用设备摄像头扫描纸质发票",
  "图床公网地址": "图片网络地址",
  "电子穿透追溯图谱": "电子审批追溯图谱",
  "落库不可逆": "提交后不可逆",
  "终结该提款公文！附言将被长久锚定": "完成该提款工单审批！审批意见将被永久记录",
  "多级防爆验证": "多级安全验证",
  "物理指令落库": "数据已入库",
  "柜面财务系统实体": "柜面财务人员",
  "终端自助储户实体": "普通用户",
  "总行管理员实体": "总行管理员",
  "物理接入极光": "当前登录位置",
  "沙盒演示终端": "系统演示设备",
  "物理验证端口 \\(短波信道\\)": "安全验证网络 (加密信道)",
  "密码加密沙盒": "密码安全中心",
  "执行物理覆写": "执行密码重置",
  "沙盒正在销毁旧有密钥内存": "系统正在清理旧的密钥缓存",
  "活动终端防御阵": "活动设备防御阵",
  "物理座标": "地理坐标",
  "外网遥测终端": "外部网络终端",
  "阻断网关握手": "阻断设备连接",
  "配置包已挂载云存储网关，等待区块确认": "配置已保存至云端，等待系统确认",
  "终端特征树已全量同步到主控制域": "设备信息已全量同步到系统主控端",
  "跨终端的非对称性保密通讯频道，查阅所有下沉分发的系统密电与业务结案回执": "安全保姆通讯频道，可查阅所有系统重要通知与业务回执",
  "时间锚点": "时间锚点", 
  "针对单一锚点的微观折图透视": "针对单一账号的微观折线图透视",
  "管控锚点": "账号状态",
  "网关截断通信故障": "网络连接通信故障",
  "财务终端核心拦截监控与资金追踪": "财务核心拦截监控与资金追踪",
  "报销与审核总控": "报销与审核",
  "报销人员": "普通用户", // Fix specific bad translations if they occur
};

function processDirectory(dir) {
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      processDirectory(fullPath);
    } else if (fullPath.endsWith('.vue') || fullPath.endsWith('.java') || fullPath.endsWith('.js')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let modified = false;
      
      for (const [key, value] of Object.entries(REPLACEMENTS)) {
        const regex = new RegExp(key, 'g');
        if (regex.test(content)) {
          content = content.replace(regex, value);
          modified = true;
        }
      }
      
      // Additional simple string replaces for broad words if needed
      // but only safe ones
      const simpleReplacements = [
        ["时间锚点", "时间"],
        ["财务人员（财务人员）", "财务人员"], // Cleanup in case "财务审核" got replaced poorly
      ];
      for (const [k, v] of simpleReplacements) {
        if (content.includes(k)) {
          content = content.split(k).join(v);
          modified = true;
        }
      }

      if (modified) {
        fs.writeFileSync(fullPath, content, 'utf8');
        console.log(`Updated: ${fullPath}`);
      }
    }
  }
}

DIRECTORIES.forEach(processDirectory);
console.log('Replacement complete.');
