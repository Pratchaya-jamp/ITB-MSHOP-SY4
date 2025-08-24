// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import LandingPage from '../components/LandingPage.vue'
import SaleItemsPage from '../components/SaleItemsPage.vue'
import SaleItemsDetailPage from '@/components/SaleItemsDetailPage.vue'
import PageNotFound from '@/components/Page404.vue'
import ListingPage from '../components/ListBrandPage.vue'
import SaleItemsAdd from '@/components/SaleItemsAdd.vue'
import BrandAdd from '@/components/BrandAdd.vue'
import Login from '@/components/Login.vue'
import Register from '@/components/Register.vue'

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
    path: '/sale-items/list',
    name: 'ListSaleItems',
    component: SaleItemsPage,
  },
  {
    path: '/sale-items/:id',
    name: 'SaleItemsdetail',
    component: SaleItemsDetailPage,
  },
  {
    path: '/brands',
    name: 'ListingPage',
    component: ListingPage
  },
  {
    path: '/sale-items/add',
    name: 'SaleItemsAdd',
    component: SaleItemsAdd,
  },
  {
  path: '/sale-items/:id/edit',
  name: 'SaleItemsEdit',
  component: SaleItemsAdd,
},
{
  path: '/brands/add',
  name: 'BrandAdd',
  component: BrandAdd,
},
{
  path: '/brands/:id/edit',
  name: 'BrandEdit',
  component: BrandAdd,
},
{
  path: '/login',
  name: 'login',
  component: Login,
},
{
  path: '/registers',
  name: 'register',
  component: Register,
},
]

const router = createRouter({
  history: createWebHistory('/sy4'),
  routes,
})

export default router
