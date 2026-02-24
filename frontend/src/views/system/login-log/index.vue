<template>
  <div class="login-log-container">
    <!-- ====== 筛选栏 ====== -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
            style="width: 360px"
          />
        </el-form-item>

        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="登录结果">
          <el-select
            v-model="queryParams.result"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button
            type="success"
            :icon="Download"
            :loading="exportLoading"
            @click="handleExport"
          >
            导出 CSV
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ====== 数据表格 ====== -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        highlight-current-row
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="login_time" label="登录时间" min-width="170" sortable />
        <el-table-column prop="ip" label="IP 地址" min-width="140" show-overflow-tooltip />
        <el-table-column label="登录方式" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="loginMethodTag(row.login_method).type" size="small">
              {{ loginMethodTag(row.login_method).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事件类型" min-width="100" align="center">
          <template #default="{ row }">
            {{ eventTypeLabel(row.event_type) }}
          </template>
        </el-table-column>
        <el-table-column label="登录结果" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">
              {{ row.result === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="归属地" min-width="120" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- ====== 详情抽屉 ====== -->
    <LoginLogDetail v-model="detailVisible" :log-id="currentLogId" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getLoginLogList, exportLoginLogCsv } from '@/api/loginLog'
import LoginLogDetail from './LoginLogDetail.vue'

/* ---------- 状态 ---------- */
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const dateRange = ref([])

const queryParams = reactive({
  username: '',
  result: undefined,
  pageNum: 1,
  pageSize: 20
})

const detailVisible = ref(false)
const currentLogId = ref(null)

/* ---------- 映射表 ---------- */
const LOGIN_METHOD_MAP = {
  1: { label: '密码', type: '' },
  2: { label: 'SSO', type: 'warning' },
  3: { label: 'Token', type: 'info' },
  4: { label: '短信', type: 'success' }
}

const EVENT_TYPE_MAP = {
  1: '登录',
  2: '登出',
  3: '强制下线'
}

function loginMethodTag(val) {
  return LOGIN_METHOD_MAP[val] || { label: '未知', type: 'info' }
}

function eventTypeLabel(val) {
  return EVENT_TYPE_MAP[val] || '未知'
}

/* ---------- 时间快捷选项 ---------- */
const dateShortcuts = [
  {
    text: '最近1小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000)
      return [start, end]
    }
  },
  {
    text: '今天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(0, 0, 0, 0)
      return [start, end]
    }
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

/* ---------- 查询 ---------- */
async function fetchList() {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 处理时间范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    // 清理空值
    Object.keys(params).forEach((key) => {
      if (params[key] === '' || params[key] === undefined || params[key] === null) {
        delete params[key]
      }
    })

    const res = await getLoginLogList(params)
    const resData = res.data || res
    tableData.value = resData.records || []
    total.value = resData.total || 0
  } catch (err) {
    console.error('获取登录日志失败', err)
    ElMessage.error('获取登录日志失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  fetchList()
}

function handleReset() {
  queryParams.username = ''
  queryParams.result = undefined
  queryParams.pageNum = 1
  queryParams.pageSize = 20
  dateRange.value = []
  fetchList()
}

/* ---------- 导出 ---------- */
async function handleExport() {
  exportLoading.value = true
  try {
    const params = {}
    if (queryParams.username) params.username = queryParams.username
    if (queryParams.result !== undefined && queryParams.result !== null) {
      params.result = queryParams.result
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const res = await exportLoginLogCsv(params)
    // 创建下载链接
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `login-logs-${Date.now()}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

/* ---------- 行点击 → 详情 ---------- */
function handleRowClick(row) {
  currentLogId.value = row.id
  detailVisible.value = true
}

/* ---------- 初始化 ---------- */
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.login-log-container {
  padding: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 4px;
}

.table-card :deep(.el-table) {
  cursor: pointer;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
