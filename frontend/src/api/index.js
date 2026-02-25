import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
});

export const orderApi = {
  createOrder(data) {
    return api.post('/order/create', data);
  },
  getList(params) {
    return api.get('/order/list', { params });
  },
  getDetail(id) {
    return api.get(`/order/${id}`);
  },
  updateStatus(id, status) {
    return api.put(`/order/${id}/status`, { status });
  }
};

export const warehouseApi = {
  getList() {
    return api.get('/warehouse/list');
  }
};
