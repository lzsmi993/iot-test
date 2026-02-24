<template>
  <el-drawer
    v-model="visible"
    title="登录日志详情"
    size="520px"
    :before-close="handleClose"
  >
    <el-descriptions
      v-if="detail"
      :column="1"
      border
      label-class-name="detail-label"
    >
      <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
      <el-descriptions-item label="用户 ID">{{ detail.user_id }}</el-descriptions-item>
      <el-descriptions-item label="租户 ID">{{ detail.tenant_id }}</el-descriptions-item>
      <el-descriptions-item label="登录时间">{{ detail.login_time }}</el-descriptions-item>
      <el-descriptions-item label="IP 地址">{{ detail.ip }}</el-descriptions-item>
      <el-descriptions-item label="IP 归属地">{{ detail.location || '—' }}</el-descriptions-item>
      <el-descriptions-item label="登录方式">
        <el-tag :type="loginMethodTag(detail.login_method).type" size="small">
          {{ loginMethodTag(detail.login_method).label }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="事件类型">
        {{ eventTypeLabel(detail.event_type) }}
      </el-descriptions-item>
      <el-descriptions-item label="登录结果">
        <el-tag :type="detail.result === 1 ? 'success' : 'danger'" size="small">
          {{ detail.result === 1 ? '成功' : '失败' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item v-if="detail.result === 0" label="失败原因">
        <span style="color: var(--el-color-danger)">{{ detail.fail_reason || '—' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="设备类型">{{ detail.device_type || '—' }}</el-descriptions-item>
      <el-descriptions-item label="User-Agent">
        <div class="ua-text">{{ detail.user_agent || '—' }}</div>
      </el-descriptions-item>
    </el-descriptions>

    <el-skeleton v-else :rows="8" animated />
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getLoginLogDetail } from '@/api/loginLog'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  logId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const detail = ref(null)

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

/* ---------- 生命周期 ---------- */
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.logId) {
      fetchDetail(props.logId)
    }
  }
)

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
    detail.value = null
  }
})

async function fetchDetail(id) {
  detail.value = null
  try {
    const { data } = await getLoginLogDetail(id)
    detail.value = data.data
  } catch {
    detail.value = null
  }
}

function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.ua-text {
  word-break: break-all;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
  font-family: monospace;
  font-size: 12px;
}

:deep(.detail-label) {
  width: 100px;
  font-weight: 600;
}
</style>
