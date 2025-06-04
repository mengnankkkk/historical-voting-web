<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container">
        <router-link class="navbar-brand" to="/">历史投票系统</router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/">首页</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/events">投票活动</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/groups">小组</router-link>
            </li>
          </ul>
          <div class="d-flex">
            <!-- 未登录显示 -->
            <template v-if="!isAuthenticated">
              <router-link to="/login" class="btn btn-outline-light me-2">登录</router-link>
              <router-link to="/register" class="btn btn-light">注册</router-link>
            </template>
            <!-- 已登录显示 -->
            <div v-else class="dropdown">
              <button class="btn btn-light dropdown-toggle" type="button" id="userDropdown" data-bs-toggle="dropdown">
                {{ userInfo?.nickname || userInfo?.username }}
              </button>
              <ul class="dropdown-menu">
                <li>
                  <router-link class="dropdown-item" to="/profile">
                    个人资料
                  </router-link>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                  <a class="dropdown-item" href="#" @click.prevent="handleLogout">
                    退出登录
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <div class="container mt-4">
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="clearError"></button>
      </div>
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';

export default {
  name: 'App',
  setup() {
    const store = useStore();
    const router = useRouter();

    // 计算属性
    const isAuthenticated = computed(() => store.getters['user/isAuthenticated']);
    const userInfo = computed(() => store.getters['user/userInfo']);
    const error = computed(() => store.getters['user/error']);

    // 方法
    const handleLogout = async () => {
      await store.dispatch('user/logout');
      router.push('/login');
    };

    const clearError = () => {
      store.commit('user/SET_ERROR', null);
    };

    // 检查登录状态
    store.dispatch('user/checkAuth');

    return {
      isAuthenticated,
      userInfo,
      error,
      handleLogout,
      clearError
    };
  }
};
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.navbar {
  margin-bottom: 2rem;
}

.dropdown-menu {
  min-width: 200px;
}
</style> 