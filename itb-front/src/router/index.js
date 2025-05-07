// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import LandingPage from '../components/LandingPage.vue'
import SaleItemsPage from '../components/SaleItemsPage.vue'
import SaleItemsDetailPage from '@/components/SaleItemsDetailPage.vue'
import PageNotFound from '@/components/Page404.vue'
import ListingPage from '../components/ListBrandPage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: LandingPage,
  },
  {
    path: '/:notMatch(.*)',
    name: 'PageNotFound',
    component: PageNotFound
  },
  {
    path: '/sale-items',
    name: 'SaleItems',
    component: SaleItemsPage,
  },
  {
    path: '/sale-items/:id',
    name: 'SaleItemsdetail',
    component: SaleItemsDetailPage,
  },
  {
    path: '/brand',
    name: 'ListingPage',
    component: ListingPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router