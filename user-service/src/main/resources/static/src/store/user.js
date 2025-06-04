import { userApi } from '../api/user';

export const user = {
  namespaced: true,
  
  state: {
    userInfo: null,
    isAuthenticated: false,
    loading: false,
    error: null
  },

  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo;
      state.isAuthenticated = !!userInfo;
    },
    SET_LOADING(state, loading) {
      state.loading = loading;
    },
    SET_ERROR(state, error) {
      state.error = error;
    },
    CLEAR_USER(state) {
      state.userInfo = null;
      state.isAuthenticated = false;
    }
  },

  actions: {
    // 登录
    async login({ commit }, credentials) {
      try {
        commit('SET_LOADING', true);
        commit('SET_ERROR', null);
        const response = await userApi.login(credentials);
        const { accessToken, refreshToken } = response.data;
        
        // 保存token
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        
        // 获取用户信息
        await dispatch('getUserInfo');
        
        return true;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '登录失败');
        return false;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 注册
    async register({ commit }, userData) {
      try {
        commit('SET_LOADING', true);
        commit('SET_ERROR', null);
        await userApi.register(userData);
        return true;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '注册失败');
        return false;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 发送验证码
    async sendVerificationCode({ commit }, email) {
      try {
        commit('SET_LOADING', true);
        commit('SET_ERROR', null);
        await userApi.sendVerificationCode(email);
        return true;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '发送验证码失败');
        return false;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 获取用户信息
    async getUserInfo({ commit }) {
      try {
        commit('SET_LOADING', true);
        commit('SET_ERROR', null);
        const response = await userApi.getProfile();
        commit('SET_USER_INFO', response.data);
        return true;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '获取用户信息失败');
        return false;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 更新用户信息
    async updateProfile({ commit }, profileData) {
      try {
        commit('SET_LOADING', true);
        commit('SET_ERROR', null);
        await userApi.updateProfile(profileData);
        await dispatch('getUserInfo');
        return true;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '更新用户信息失败');
        return false;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 登出
    async logout({ commit }) {
      try {
        commit('SET_LOADING', true);
        commit('SET_ERROR', null);
        await userApi.logout();
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        commit('CLEAR_USER');
        return true;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '登出失败');
        return false;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 检查登录状态
    checkAuth({ commit, dispatch }) {
      const token = localStorage.getItem('accessToken');
      if (token) {
        return dispatch('getUserInfo');
      } else {
        commit('CLEAR_USER');
        return false;
      }
    }
  },

  getters: {
    isAuthenticated: state => state.isAuthenticated,
    userInfo: state => state.userInfo,
    loading: state => state.loading,
    error: state => state.error,
    userRank: state => state.userInfo?.userRank,
    userLevel: state => state.userInfo?.level,
    userExperience: state => state.userInfo?.experience
  }
}; 