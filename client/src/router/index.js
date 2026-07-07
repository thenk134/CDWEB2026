import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Category from '../pages/Category.vue'
import NewsDetail from '../pages/NewsDetail.vue'
import Search from '../pages/Search.vue'
import Bookmarks from '../pages/Bookmarks.vue'
import Login from '../pages/Login.vue'
import Register from '../pages/Register.vue'
import ForgotPassword from '../pages/ForgotPassword.vue'
import AdminDashboard from '../pages/AdminDashboard.vue'
import AdminEditNews from '../pages/AdminEditNews.vue'
import NotFound from '../pages/NotFound.vue'

import { toast } from '../utils/toast'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/category/:slug',
    name: 'Category',
    component: Category
  },
  {
    path: '/news-detail',
    name: 'NewsDetail',
    component: NewsDetail
  },
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/bookmarks',
    name: 'Bookmarks',
    component: Bookmarks
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/create',
    name: 'AdminCreateNews',
    component: AdminEditNews,
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/edit/:id',
    name: 'AdminEditNews',
    component: AdminEditNews,
    meta: { requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  },
  {
    path: '/admin/revenue',
    name: 'AdminRevenue',
    component: () => import('../pages/AdminRevenue.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/user-posts',
    name: 'UserPostManagement',
    // Sử dụng lazy-loading (import động) giúp ứng dụng tải nhanh hơn
    component: () => import('../pages/UserPostManagement.vue'),
    meta: { requiresAdmin: true } // Đánh dấu trang này bắt buộc phải là Admin mới vào được
  },
  {
    path: '/admin/user-posts/:id',
    name: 'UserPostDetail',
    component: () => import('../pages/UserPostDetail.vue'),
    // Bật props: true để biến tham số :id trên URL thành một biến props trong component Vue
    props: true,
    meta: { requiresAdmin: true }
  },
  {
    path: '/account-management',
    name: 'AccountManagement',
    component: () => import('../pages/AccountManagement.vue')
  },
  {
    path: '/my-posts',
    name: 'MyPosts',
    component: () => import('../pages/MyPosts.vue')
  },
  {
    path: '/my-post/create',
    name: 'CreateMyPost',
    component: () => import('../pages/CreateMyPost.vue')
  },
  {
    path: '/member/:username',
    name: 'MemberProfile',
    component: () => import('../pages/MemberProfile.vue'),
    props: true
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Route guard kiểm tra quyền ADMIN
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    const role = localStorage.getItem('user_role')
    if (role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập trang quản trị này!')
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
