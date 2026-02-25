<template>
  <div class="order-detail">
    <h2>订单详情</h2>

    <div v-if="order" class="detail-card">
      <div class="detail-row">
        <label>订单ID:</label>
        <span>{{ order.id }}</span>
      </div>
      <div class="detail-row">
        <label>用户ID:</label>
        <span>{{ order.userId }}</span>
      </div>
      <div class="detail-row">
        <label>订单金额:</label>
        <span>¥{{ order.totalPrice }}</span>
      </div>
      <div class="detail-row">
        <label>仓库ID:</label>
        <span>{{ order.warehouseId }}</span>
      </div>
      <div class="detail-row">
        <label>订单状态:</label>
        <span class="status-tag" :class="'status-' + order.status">{{ statusMap[order.status] || order.status }}</span>
      </div>
      <div class="detail-row">
        <label>创建时间:</label>
        <span>{{ order.createdAt }}</span>
      </div>
      <div class="detail-row">
        <label>更新时间:</label>
        <span>{{ order.updatedAt || '-' }}</span>
      </div>

      <div class="actions">
        <h3>更新状态</h3>
        <div class="status-buttons">
          <button
            v-for="(label, key) in statusMap"
            :key="key"
            :disabled="order.status === key"
            :class="{ active: order.status === key }"
            @click="changeStatus(key)"
          >
            {{ label }}
          </button>
        </div>
      </div>

      <div v-if="message" class="message" :class="messageType">
        {{ message }}
      </div>
    </div>

    <div v-else-if="error" class="message error">{{ error }}</div>
    <div v-else class="loading">加载中...</div>

    <router-link to="/order/list" class="back-link">← 返回订单列表</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { orderApi } from '@/api';

const route = useRoute();
const order = ref(null);
const error = ref('');
const message = ref('');
const messageType = ref('');

const statusMap = {
  PENDING: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
};

const loadOrder = async () => {
  try {
    const res = await orderApi.getDetail(route.params.id);
    if (res.data.code === 200) {
      order.value = res.data.data;
    } else {
      error.value = res.data.message;
    }
  } catch (e) {
    error.value = '加载订单详情失败';
  }
};

const changeStatus = async (status) => {
  try {
    const res = await orderApi.updateStatus(route.params.id, status);
    if (res.data.code === 200) {
      message.value = '状态更新成功';
      messageType.value = 'success';
      order.value.status = status;
    } else {
      message.value = res.data.message;
      messageType.value = 'error';
    }
  } catch (e) {
    message.value = '状态更新失败';
    messageType.value = 'error';
  }
  setTimeout(() => { message.value = ''; }, 2000);
};

onMounted(loadOrder);
</script>

<style scoped>
.order-detail {
  max-width: 600px;
  margin: 30px auto;
  padding: 20px;
}

.detail-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}

.detail-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row label {
  width: 120px;
  font-weight: bold;
  color: #666;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
}

.status-PENDING { background: #fff3cd; color: #856404; }
.status-PAID { background: #d4edda; color: #155724; }
.status-SHIPPED { background: #cce5ff; color: #004085; }
.status-COMPLETED { background: #d1ecf1; color: #0c5460; }
.status-CANCELLED { background: #f8d7da; color: #721c24; }

.actions {
  margin-top: 20px;
}

.actions h3 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}

.status-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-buttons button {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.status-buttons button:hover:not(:disabled) {
  background: #f0f0f0;
}

.status-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.status-buttons button.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.message {
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
}

.message.success {
  background: #d4edda;
  color: #155724;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
}

.loading {
  text-align: center;
  color: #999;
  padding: 40px;
}

.back-link {
  display: inline-block;
  margin-top: 20px;
  color: #007bff;
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
}
</style>
