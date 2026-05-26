<template>
  <section class="admin-page">
    <article class="toolbar-card">
      <div>
        <h2>举报处理</h2>
        <p>筛选待处理举报，填写处理结论后状态会回写数据库。</p>
      </div>
      <el-select v-model="statusFilter" @change="reload">
        <el-option label="全部状态" value="all" />
        <el-option v-for="status in statuses" :key="status" :label="reportStatusLabels[status]" :value="status" />
      </el-select>
    </article>

    <article class="table-card">
      <el-table v-loading="loading" :data="reports" stripe>
        <el-table-column prop="reportId" label="ID" width="80" />
        <el-table-column label="对象" min-width="140">
          <template #default="{ row }">
            {{ reportTargetLabel(row) }} #{{ row.targetId }}
          </template>
        </el-table-column>
        <el-table-column label="举报人" min-width="120">
          <template #default="{ row }">{{ row.reporterName }} (#{{ row.reporterId }})</template>
        </el-table-column>
        <el-table-column label="被举报用户" min-width="140">
          <template #default="{ row }">{{ row.targetUserName }} (#{{ row.targetUserId }})</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ reportStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 'PENDING'"
              @click="openHandleDialog(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="total"
          @current-change="loadReports"
        />
      </div>
    </article>

    <el-dialog v-model="dialogVisible" title="处理举报" width="520px">
      <el-form label-position="top">
        <el-form-item label="处理状态">
          <el-radio-group v-model="handleForm.status">
            <el-radio-button label="HANDLED">已处理</el-radio-button>
            <el-radio-button label="REJECTED">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结论">
          <el-input v-model="handleForm.result" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import { getAdminReports, handleReport } from '@/api/admin'
import { reportStatusLabels, reportTargetTypeLabels } from '@/types'
import type { ReportDTO, ReportStatus } from '@/types'

const statuses: ReportStatus[] = ['PENDING', 'HANDLED', 'REJECTED']

const loading = ref(false)
const submitting = ref(false)
const reports = ref<ReportDTO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const statusFilter = ref<'all' | ReportStatus>('PENDING')
const dialogVisible = ref(false)
const selectedReport = ref<ReportDTO | null>(null)
const handleForm = reactive<{ status: 'HANDLED' | 'REJECTED'; result: string }>({
  status: 'HANDLED',
  result: '',
})

const statusTag = (status: ReportStatus) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'HANDLED') return 'success'
  return 'info'
}
const reportStatusLabel = (status: ReportStatus) => reportStatusLabels[status]
const reportTargetLabel = (report: ReportDTO) => reportTargetTypeLabels[report.targetType]

const loadReports = async () => {
  loading.value = true
  try {
    const result = await getAdminReports({
      status: statusFilter.value === 'all' ? undefined : statusFilter.value,
      page: page.value,
      size: pageSize,
    })
    reports.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  loadReports()
}

const openHandleDialog = (report: ReportDTO) => {
  selectedReport.value = report
  handleForm.status = 'HANDLED'
  handleForm.result = ''
  dialogVisible.value = true
}

const submitHandle = async () => {
  if (!selectedReport.value) return
  if (!handleForm.result.trim()) {
    ElMessage.warning('请填写处理结论')
    return
  }

  submitting.value = true
  try {
    await handleReport(selectedReport.value.reportId, {
      status: handleForm.status,
      result: handleForm.result.trim(),
    })
    ElMessage.success('处理结果已保存')
    dialogVisible.value = false
    await loadReports()
  } finally {
    submitting.value = false
  }
}

onMounted(loadReports)
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.toolbar-card,
.table-card {
  padding: 20px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.06);
}

.toolbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.toolbar-card h2,
.toolbar-card p {
  margin: 0;
}

.toolbar-card h2 {
  color: #111827;
  font-size: 20px;
}

.toolbar-card p {
  margin-top: 6px;
  color: #64748b;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 760px) {
  .toolbar-card,
  .pager {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
