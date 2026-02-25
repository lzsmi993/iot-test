<template>
  <div class="order-list">
    <h2>订单列表</h2>

    <div class="toolbar">
      <select v-model="statusFilter" @change="loadOrders">
        <option value="">全部状态</option>
        <option value="PENDING">待支付</option>
        <option value="PAID">已支付</option>
        <option value="SHIPPED">已发货</option>
        <option value="COMPLETED">已完成</option>
        <option value="CANCELLED">已取消</option>
      </select>
      <router-link to="/order/create" class="btn-create">创建订单</router-link>
    </div>

    <table>
      <thead>
        <tr>
          <th>订单ID</th>
          <th>用户ID</th>
          <th>金额</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="order in orders" :key="order.id">
          <td>{{ order.id }}</td>
          <td>{{ order.userId }}</td>
          <td>¥{{ order.totalPrice }}</td>
          <td><span class="status-tag" :class="'status-' + order.status">{{ statusMap[order.status] || order.status }}</span></td>
          <td>{{ order.createdAt }}</td>
          <td>
            <router-link :to="`/order/detail/${order.id}`" class="btn-link">详情</router-link>
          </td>
        </tr>
        <tr v-if="orders.length === 0">
          <td colspan="6" class="empty">暂无订单数据</td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button :disabled="page <= 1" @click="page--; loadOrders()">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页（{{ total }} 条）</span>
      <button :disabled="page >= totalPages" @click="page++; loadOrders()">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { orderApi } from '@/api';

const orders = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const statusFilter = ref('');

const statusMap = {
  PENDING: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
};

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));

const loadOrders = async () => {
  try {
    const params = { page: page.value, size: size.value };
    if (statusFilter.value) {
      params.status = statusFilter.value;
    }
    const res = await orderApi.getList(params);
    if (res.data.code === 200) {
      orders.value = res.data.data.list;
      total.value = res.data.data.total;
    }
  } catch (error) {
    console.error('加载订单列表失败', error);
  }
};

onMounted(loadOrders);
</script>

<style scoped>
.order-list {
  max-width: 900px;
  margin: 30px auto;
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-create {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.btn-create:hover {
  background: #0056b3;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 10px 12px;
  border: 1px solid #ddd;
  text-align: left;
}

th {
  background: #f5f5f5;
}

.empty {
  text-align: center;
  color: #999;
  padding: 30px;
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

.btn-link {
  color: #007bff;
  text-decoration: none;
}

.btn-link:hover {
  text-decoration: underline;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.pagination button {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:not(:disabled):hover {
  background: #f0f0f0;
}
</style>
