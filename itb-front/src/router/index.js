// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import Cookies from 'js-cookie'
import LandingPage from '../components/LandingPage.vue'
import SaleItemsPage from '../components/SaleItemsPage.vue'
import SaleItemsDetailPage from '@/components/SaleItemsDetailPage.vue'
import PageNotFound from '@/components/Page404.vue'
import ListingPage from '../components/ListBrandPage.vue'
import SaleItemsAdd from '@/components/SaleItemsAdd.vue'
import BrandAdd from '@/components/BrandAdd.vue'
import Login from '@/components/Login.vue'
import Register from '@/components/Register.vue'
import VerifyEmail from '@/components/VerifyEmail.vue'
import UserProfile from '@/components/UserProfile.vue'
import UserEdit from '@/components/UserEdit.vue'
import Cart from '@/components/Cart.vue'
import PlaceOrder from '@/components/PlaceOrder.vue'
import OrderDetail from '@/components/OrderDetail.vue'
import ForgotPassword from '@/components/ForgotPassword.vue'
import ResetPassword from '@/components/ResetPassword.vue'
import ChangePassword from '@/components/ChangePassword.vue'

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
    meta: { isGallery: true }
  },
  {
    path: '/sale-items/list',
    name: 'ListSaleItems',
    component: SaleItemsPage,
    meta: { requiresSeller: true }
  },
  {
    path: '/sale-items/:id',
    name: 'SaleItemsdetail',
    component: SaleItemsDetailPage,
  },
  {
    path: '/brands',
    name: 'ListingPage',
    component: ListingPage,
    meta: { requiresSeller: true }
  },
  {
    path: '/sale-items/add',
    name: 'SaleItemsAdd',
    component: SaleItemsAdd,
    meta: { requiresSeller: true }
  },
  {
  path: '/sale-items/:id/edit',
  name: 'SaleItemsEdit',
  component: SaleItemsAdd,
  meta: { requiresSeller: true }
},
{
  path: '/brands/add',
  name: 'BrandAdd',
  component: BrandAdd,
  meta: { requiresSeller: true }
},
{
  path: '/brands/:id/edit',
  name: 'BrandEdit',
  component: BrandAdd,
  meta: { requiresSeller: true }
},
{
  path: '/signin',
  name: 'signin',
  component: Login,
},
{
  path: '/registers',
  name: 'register',
  component: Register,
},
{
  path: '/verify-email',
  name: 'verify-email',
  component: VerifyEmail,
},
{
  path: '/profile',
  name: 'userProfile',
  component: UserProfile,
},
{
  path: '/profile/edit',
  name: 'userEdit',
  component: UserEdit,
},
{
  path: '/cart',
  name: 'cart',
  component: Cart,
  meta: { requiresAuth: true }
},
{
  path: '/order',
  name: 'placeOrder',
  component: PlaceOrder,
  meta: { requiresAuth: true }
},
{
  path: '/order/:orderid',
  name: 'orderDetail',
  component: OrderDetail,
  meta: { requiresAuth: true }
},
{
  path: '/forgot-password',
  name: 'forgotPassword',
  component: ForgotPassword
},
{
  path: '/verify-password',
  name: 'verify-password',
  component: ResetPassword,
},
{
  path: '/change-password',
  name: 'changePassword',
  component: ChangePassword,
  meta: { requiresAuth: true }
},
]

const router = createRouter({
  history: createWebHistory('/sy4'),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = Cookies.get('access_token');

  if ((to.name === 'signin' || to.name === 'register') && token) {
    console.log('User is already logged in. Redirecting to Home...');
    next({ name: 'Home' });
    return;
  }

  if (to.meta.requiresAuth && !token) {
    console.log('Access to ' + to.path + ' requires login. Redirecting...');
    next('/signin')
    return
  }

  if (to.meta.requiresSeller) {
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        const userRole = decodedToken.role;
        
        if (userRole === 'SELLER') {
          next();
        } else if (userRole === 'BUYER') {
          console.log('Buyer trying to access a seller-only page. Redirecting...');
          next('/sale-items');
        } else {
          // บทบาทไม่ถูกต้อง
          next('/signin');
        }
      } catch (error) {
        console.error("JWT decoding failed:", error);
        next('/signin');
      }
    } else {
      next('/signin');
    }
  } else {
    next();
  }
});

export default router
