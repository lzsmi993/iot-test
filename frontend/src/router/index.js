import { createRouter, createWebHistory } from 'vue-router';
import CreateOrder from '@/views/order/CreateOrder.vue';
import OrderList from '@/views/order/OrderList.vue';
import OrderDetail from '@/views/order/OrderDetail.vue';

const routes = [
  { path: '/', redirect: '/order/list' },
  { path: '/order/list', component: OrderList },
  { path: '/order/create', component: CreateOrder },
  { path: '/order/detail/:id', component: OrderDetail }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 货运平台` : '货运平台';
  next();
});

export default router;
