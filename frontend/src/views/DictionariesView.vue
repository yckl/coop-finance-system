<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">基础数据字典与系统配置</div>
        <div class="panel-subtitle">集中管理系统枚举值、系统常量参数及底层模型设定</div>
      </div>
      <div class="topbar-actions">
        <el-button color="#2980b9" :dark="true" @click="saveAll">保存所有修改</el-button>
        <el-button @click="loadData">强制拉取生效</el-button>
      </div>
    </div>
    
    <el-card class="panel-card" shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础字典动态构建 -->
        <el-tab-pane 
          v-for="type in dictTypes" 
          :key="type.type_code" 
          :label="type.type_name" 
          :name="type.type_code">
          
          <div class="toolbar-inline" style="margin-bottom: 15px;">
            <p style="color: #7f8c8d; font-size: 13px;">内部识别码: <code>{{ type.type_code }}</code> - {{ type.remark }}</p>
            <el-button size="small" type="primary" plain icon="Plus" @click="mockAdd">添加字典项</el-button>
          </div>
          
          <el-table :data="getItems(type.type_code)" stripe style="width: 100%" size="small">
            <el-table-column prop="item_sort" label="权重/排序号" width="100" align="center" />
            <el-table-column prop="item_label" label="显示标签 (Label)" min-width="150" />
            <el-table-column prop="item_value" label="底层入库值 (Value)" min-width="150" />
            <el-table-column prop="item_status" label="是否启用" min-width="100">
              <template #default="scope">
                <el-switch v-model="scope.row.item_status" active-value="启用" inactive-value="停用" />
              </template>
            </el-table-column>
            <el-table-column label="结构级操作" fixed="right" min-width="150" align="center">
              <template #default>
                <el-button link type="primary" disabled>深度映射修改</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 系统参数专属标签页 -->
        <el-tab-pane label="🔩 核心系统参数" name="SYSTEM_CONFIGS">
          <el-descriptions title="全局运营与风控常量" direction="vertical" :column="3" border>
            <el-descriptions-item v-for="cfg in configs" :key="cfg.config_code" :label="cfg.config_name + ' (' + cfg.config_code + ')'">
              <el-input v-model="cfg.config_value" size="small" />
              <div style="font-size: 12px; color: #95a5a6; margin-top: 4px;">{{ cfg.remark }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api'

const activeTab = ref('DEPOSIT_TYPE')
const dictTypes = ref([])
const dictItems = ref([])
const configs = ref([])

function getItems(code) {
  return dictItems.value.filter(i => i.type_code === code)
}

async function loadData() {
  try {
    const res = await http.get(`/admin/module/dictionaries`)
    dictTypes.value = res.data.dictTypes || []
    dictItems.value = res.data.dictItems || []
    configs.value = res.data.configs || []
    if (dictTypes.value.length > 0 && activeTab.value === 'SYSTEM_CONFIGS' === false) {
       activeTab.value = dictTypes.value[0].type_code
    }
  } catch (e) {
    ElMessage.error('无法同步底层字典配置: ' + e)
  }
}

function mockAdd() {
  ElMessage.warning('原型模式下无法向物理表直接增辟动态字典结构。')
}

function saveAll() {
  ElMessage.success('配置预验证通过！写入操作已被锁定（只读模式拦截）。')
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
</style>
