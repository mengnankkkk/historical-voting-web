import { createApp } from 'vue';
import { createStore } from 'vuex';
import { createRouter, createWebHistory } from 'vue-router';
import App from './App.vue';
import { user } from './store/user';

// 创建 Vuex store
const store = createStore({
  modules: {
    user
  }
});

// 创建路由
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('./views/Home.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('./views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('./views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('./views/Profile.vue'),
      meta: { requiresAuth: true }
    }
  ]
});

// 路由守卫
router.beforeEach(async (to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const isAuthenticated = await store.dispatch('user/checkAuth');
    if (!isAuthenticated) {
      next({ name: 'Login', query: { redirect: to.fullPath } });
    } else {
      next();
    }
  } else {
    next();
  }
});

// 创建Vue应用
const app = createApp(App);

// 注册全局属性
app.config.globalProperties.$formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleDateString('zh-CN');
};

// 使用插件
app.use(store);
app.use(router);

// 挂载应用
app.mount('#app'); 