// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import LandingPage from '../components/LandingPage.vue'
import SaleItems from '../components/SaleItems.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: LandingPage,
  },
  {
    path: '/sale-items',
    name: 'SaleItems',
    component: SaleItems,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router