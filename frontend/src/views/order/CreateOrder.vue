<template>
  <div class="order-create">
    <h2>创建订单</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>用户ID:</label>
        <input v-model.number="form.userId" type="number" required />
      </div>

      <div class="form-group">
        <label>订单金额:</label>
        <input v-model.number="form.totalPrice" type="number" step="0.01" required />
      </div>

      <div class="form-group">
        <label>选择仓库:</label>
        <select v-model.number="form.warehouseId" required>
          <option value="">请选择仓库</option>
          <option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :value="warehouse.id"
          >
            {{ warehouse.name }} - {{ warehouse.location }}
          </option>
        </select>
      </div>

      <button type="submit">创建订单</button>
    </form>

    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { orderApi, warehouseApi } from '@/api';

const form = ref({
  userId: null,
  totalPrice: null,
  warehouseId: ''
});

const warehouseList = ref([]);
const message = ref('');
const messageType = ref('');

onMounted(async () => {
  try {
    const res = await warehouseApi.getList();
    if (res.data.code === 200) {
      warehouseList.value = res.data.data;
    }
  } catch (error) {
    message.value = '加载仓库列表失败';
    messageType.value = 'error';
  }
});

const handleSubmit = async () => {
  try {
    const res = await orderApi.createOrder(form.value);
    if (res.data.code === 200) {
      message.value = '订单创建成功';
      messageType.value = 'success';
      form.value = { userId: null, totalPrice: null, warehouseId: '' };
    } else {
      message.value = res.data.message;
      messageType.value = 'error';
    }
  } catch (error) {
    message.value = '订单创建失败';
    messageType.value = 'error';
  }
};
</script>

<style scoped>
.order-create {
  max-width: 500px;
  margin: 50px auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #0056b3;
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
</style>
