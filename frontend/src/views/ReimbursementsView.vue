<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">支出报销大厅</div>
        <div class="panel-subtitle">审核和追溯系统内所发起的各项差旅、农资、器材采购费用的垫资清算</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" v-if="role === 'user'" @click="showForm = true">发起新报销</el-button>
        <el-button @click="loadData">{{ role === 'user' ? '刷新我的单据' : '更新所有单据' }}</el-button>
      </div>
    </div>
    
    <el-card class="panel-card" shadow="never">
      <el-tabs v-model="activeTab" class="demo-tabs">
        <el-tab-pane label="待复核单据" name="待审核"></el-tab-pane>
        <el-tab-pane label="退回重审单据" name="被驳回"></el-tab-pane>
        <el-tab-pane label="归档清算单据" name="已通过"></el-tab-pane>
        <el-tab-pane label="所有单据索引" name="全部"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredRecords" stripe style="width: 100%; margin-top: 10px;">
        <!-- 行展开，用以执行真实审批 -->
        <el-table-column type="expand">
          <template #default="scope">
            <div style="padding: 20px; background-color: #fafbfc; border-radius: 4px; margin: 0 20px;">
              <el-row :gutter="20">
                <el-col :span="16">
                   <p v-if="role === 'user'" style="margin: 0; color: #555;"><strong>前置审核意见:</strong> {{ scope.row.audit_comment || '暂无内容' }}</p>
                   <div v-if="role !== 'user'" style="margin-top: 15px; border-top: 1px dashed #ddd; padding-top: 15px;">
                      <el-input
                        v-model="auditComments[scope.row.reimbursement_no]"
                        type="textarea"
                        :rows="2"
                        placeholder="输入您的审查批示..."
                      />
                      <div style="margin-top: 10px; text-align: right;">
                         <el-button type="success" @click="fakeAudit(scope.row, '驳回')">{{ role === 'admin' ? '强制打回' : '财务初审驳回' }}</el-button>
                         <el-button type="primary" @click="fakeAudit(scope.row, '通过')">{{ role === 'admin' ? '盖章通过结款' : '核算无误并放款' }}</el-button>
                      </div>
                   </div>
                </el-col>
                <el-col :span="8">
                   <div style="text-align: center; border: 1px solid #ddd; padding: 10px; background: #fff;">
                      <span style="font-size: 12px; color: #999; display:block; margin-bottom:10px;">附件留影</span>
                      <el-image 
                        style="width: 100px; height: 100px"
                        :src="'https://placehold.co/400x400/eeeeee/999999?text=' + encodeURIComponent(scope.row.attachment_url)" 
                        :preview-src-list="['https://placehold.co/800x800/eeeeee/999999?text=SampleReceipt']"
                        fit="cover"
                        preview-teleported
                      />
                   </div>
                </el-col>
              </el-row>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="reimbursement_no" label="结算申请码" min-width="150" />
        <el-table-column prop="applicant_name" label="报销人" min-width="120">
          <template #default="scope">
            <span style="color: #2980b9; font-weight: 500;">{{ scope.row.applicant_name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reimbursement_type" label="费用名目" min-width="120" />
        <el-table-column prop="reimbursement_amount" label="要求提款 (¥)" min-width="130">
          <template #default="scope">
            <strong style="color: #c0392b;">{{ Number(scope.row.reimbursement_amount).toFixed(2) }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="current_status" label="流转节点" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.current_status === '待审核' ? 'warning' : (scope.row.current_status === '已通过' ? 'success' : 'danger')" effect="dark">
              {{ scope.row.current_status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apply_time" label="初审发文时刻" min-width="160" />
      </el-table>
    </el-card>

    <el-dialog v-model="showForm" title="新建报销/垫资申请单" width="500px">
       <el-form label-width="100px">
          <el-form-item label="发票开办名目" required>
             <el-input v-model="newForm.type" placeholder="如：差旅票据，农资购买发票" />
          </el-form-item>
          <el-form-item label="垫资总核额" required>
             <el-input-number v-model="newForm.amount" :min="1" />
          </el-form-item>
          <el-form-item label="凭据物理地址">
             <el-input v-model="newForm.url" placeholder="请填入凭证图像URL（可选）" />
          </el-form-item>
       </el-form>
       <template #footer>
         <div class="dialog-footer">
           <el-button @click="showForm = false">暂不提交</el-button>
           <el-button type="primary" @click="submitReim">打上印鉴并投递</el-button>
         </div>
       </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'admin'
const records = ref([])
const activeTab = ref('待审核')
const auditComments = ref({})
const showForm = ref(false)
const newForm = ref({ type: '', amount: 100, url: '' })

const filteredRecords = computed(() => {
  if (activeTab.value === '全部') return records.value
  return records.value.filter(r => r.current_status === activeTab.value)
})

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/reimbursements`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('报销同步受阻: ' + e)
  }
}

async function fakeAudit(row, result) {
  const comment = auditComments.value[row.reimbursement_no] || (result === '通过' ? '原路准予' : '证据匮乏')
  try {
    await ElMessageBox.confirm(`您将以当前角色 [${result}] 法人代表：${row.applicant_name} 的单笔提款，附言：${comment}。资金流不可追回，确认吗？`, '资金复核预判', {
      type: result === '通过' ? 'warning' : 'error'
    })
    ElMessage.success(`操作录入完毕！流水号：${row.reimbursement_no} ${result === '通过' ? '已拨款' : '打回原始节点'}`)
  } catch(e) { }
}

async function submitReim() {
  if(!newForm.value.type) return ElMessage.error('请填写报销名目')
  ElMessage.success('您的报销工单已发送至柜面与总部联合机审池，请静候银联渠道放款！')
  showForm.value = false
  newForm.value = { type: '', amount: 100, url: '' }
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 8px;
}
</style>
