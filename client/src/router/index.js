import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Category from '../pages/Category.vue'
import NewsDetail from '../pages/NewsDetail.vue'
import Search from '../pages/Search.vue'
import Bookmarks from '../pages/Bookmarks.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
