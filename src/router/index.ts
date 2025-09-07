import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';
import TodoList from '@/views/TodoList.vue';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/todos'
  },
  {
    path: '/todos',
    name: 'TodoList',
    component: TodoList
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router