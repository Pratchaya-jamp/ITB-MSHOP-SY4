<script setup>
import { ref, onMounted, computed } from 'vue'
import {getItemsWithAuth,} from '@/libs/fetchUtilsOur';
import Cookies from 'js-cookie' 
import { jwtDecode } from 'jwt-decode' 

// *** 💡 การจำลอง: สมมติว่านี่คือค่า orderid ที่ถูกดึงมาจาก /order/:orderid ***
// สามารถเปลี่ยนค่าเพื่อดูรายละเอียดของออเดอร์ต่างๆ ได้ (เช่น '12345' หรือ '12346')
const orderIdInRoute = ref('12345') 
// ถ้าเป็น null หรือ '12347' จะแสดงหน้า "Order Not Found"

// --- Mock Data: จำลองข้อมูลประวัติการสั่งซื้อ (อ้างอิงจากภาพตัวอย่าง) ---
const orders = ref([])

// --- Theme & Utility Logic (คงเดิม) ---
const theme = ref(localStorage.getItem('theme') || 'dark')

const applyTheme = (newTheme) => {
    document.body.className = newTheme === 'dark' ? 'dark-theme' : ''
    localStorage.setItem('theme', newTheme)
    theme.value = newTheme
}

const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(newTheme)
}

// Function สำหรับกลับไปหน้า Order List (จำลองการกลับไปที่ /orders)
const backToOrderList = () => {
    // ในแอปจริง: router.push('/orders')
    // ในการจำลอง: เราสามารถรีเซ็ต ID เพื่อจำลองการ "กลับหน้า"
    orderIdInRoute.value = null; 
    alert("Simulated: Navigated back to /orders list!");
}

// --- Computed Properties ---

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

const iconComponent = computed(() => {
    return theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
})

const contentBgClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-900' // มืดกว่านิดหน่อย
        : 'bg-gray-100' // สว่างกว่านิดหน่อย
})

const cardClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-800 shadow-xl border border-gray-700'
        : 'bg-white shadow-xl border border-gray-300'
})

// *** Computed Property หลักสำหรับหน้านี้: หา Order จาก ID ที่อยู่ใน Route ***
const currentOrder = computed(() => {
    return orders.value[0] || null;
});

const formatPrice = (price) => {
    if (price === undefined || price === null) return '0'
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const getStatusClass = (status) => {
    if (!status) return ''
    status = status.toUpperCase()
    if (status === 'COMPLETED') return 'text-green-500 bg-green-500/10'
    if (status === 'CANCELLED') return 'text-red-500 bg-red-500/10'
    return 'text-yellow-500 bg-yellow-500/10'
}


async function fetchItemOrderbyId() {
  const token = Cookies.get('access_token');
  let decodedToken = null;
  let userId = null;

  if (token) {
    try {
      decodedToken = jwtDecode(token);
      userId = decodedToken.id;
    } catch (err) {
      console.error("Failed to decode token:", err);
      return;
    }
  } else {
    console.error("No access token found");
    router.push('/login');
    return;
  }

  try {
    const response = await getItemsWithAuth(
      `https://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/orders/2`,
      {
        token: token,
      }
    );

    // ตรวจสอบ response ก่อนใช้
    if (!response || (response.status && (response.status === 401 || response.status === 403))) {
      console.error('Authentication error: Unauthorized or Forbidden');
      router.push('/login');
      return;
    }

    const data = response.data ?? response; // รองรับทั้ง axios หรือ json ตรงๆ
    data.userType = decodedToken?.role === 'SELLER' ? 'Seller' : 'Buyer';
    orders.value = [data];
  } catch (error) {
    console.error('Error fetching user orders:', error);
  }
}

onMounted(() => {
    applyTheme(theme.value);
fetchItemOrderbyId()
});
</script>

<template>
    <div :class="themeClass" class="p-4 w-full min-h-screen font-sans transition-colors duration-500">
        
        <div :class="contentBgClass" class="px-6 md:px-20 py-12">
            
            <div class="mb-8 flex items-center justify-between">
                <button @click="backToOrderList" class="flex items-center space-x-2 text-lg font-semibold transition-colors duration-200" :class="theme === 'dark' ? 'text-orange-400 hover:text-orange-300' : 'text-orange-600 hover:text-orange-500'">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    <span>Back to Order History</span>
                </button>
                <div v-if="currentOrder" class="itbms-order-status text-lg font-bold px-4 py-1 rounded-full" :class="getStatusClass(currentOrder.orderStatus)">
                    {{ currentOrder.orderStatus }}
                </div>
            </div>

            <h1 class="text-4xl font-extrabold mb-8" v-if="currentOrder">Order Details #{{ currentOrder.id }}</h1>
            
            <div v-if="currentOrder" class="space-y-8">
                
                <div :class="cardClass" class="p-6 rounded-3xl">
                    <h2 class="text-2xl font-bold mb-4 border-b pb-3" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">Order & Shipping Info</h2>
                    
                    <div class="grid md:grid-cols-2 gap-6 text-base">
                        <div>
                            <p class="font-semibold mb-2 text-lg">Order Summary</p>
                            <div class="space-y-1" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
                                <p><strong class="font-medium">Order No:</strong> {{ currentOrder.id }}</p>
                                <p><strong class="font-medium">Order Date:</strong> {{ currentOrder.orderDate }}</p>
                                <p><strong class="font-medium">Payment Date:</strong> {{ currentOrder.paymentDate }}</p>
                                <p><strong class="font-medium">Customer:</strong> {{ currentOrder.seller.nickname }}</p>
                            </div>
                        </div>

                        <div>
                            <p class="font-semibold mb-2 text-lg">Shipping Details</p>
                            <div class="space-y-1" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
                                <p><strong class="font-medium">Address:</strong> {{ currentOrder.shippingAddress }}</p>
                                <p><strong class="font-medium">Note:</strong> <span class="italic">{{ currentOrder.orderNote || 'No additional instructions.' }}</span></p>
                                <p><strong class="font-medium">Payment Method:</strong> <span class="font-medium">Credit Card (Mock)</span></p>
                            </div>
                        </div>
                    </div>
                </div>

                <div :class="cardClass" class="p-6 rounded-3xl">
                    <h2 class="text-2xl font-bold mb-4 border-b pb-3" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">Items Ordered ({{ currentOrder.orderItems.length }})</h2>
                    
                    <div class="divide-y" :class="theme === 'dark' ? 'divide-gray-700' : 'divide-gray-200'">
                        <div v-for="item in currentOrder.orderItems" :key="item.no" class="flex justify-between items-center py-4 itbms-item-row">
                            <div class="flex items-center space-x-4">
                                <img src="/phone/iPhone.png" alt="Product Image" class="w-16 h-16 object-contain rounded-lg border" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'"/>
                                <div class="flex-grow">
                                    <p class="font-medium itbms-item-name text-lg">{{ item.description }}</p>
                                    <p class="text-sm itbms-item-description" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">{{ item.description }}</p>
                                </div>
                            </div>

                            <div class="flex flex-col space-y-1 text-right items-end">
                                <span class="font-bold text-xl text-orange-500 itbms-item-total-price">
                                    {{ formatPrice(item.price * item.quantity) }} ฿
                                </span>
                                <span class="itbms-item-quantity text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                                    {{ formatPrice(item.price) }} ฿ x {{ item.quantity }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div :class="cardClass" class="p-6 rounded-3xl md:w-1/2 md:ml-auto">
                    <h2 class="text-2xl font-bold mb-4 border-b pb-3" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">Payment Summary</h2>
                    
                    <div class="space-y-3">
                        <div class="flex justify-between text-lg" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
                            <span class="font-medium">Subtotal:</span>
                            <span>{{ formatPrice(currentOrder.subTotal) }} ฿</span>
                        </div>
                        <!-- <div class="flex justify-between text-lg" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
                            <span class="font-medium">Shipping Fee:</span>
                            <span>{{ formatPrice(currentOrder.shippingFee) }} ฿</span>
                        </div> -->
                        <div class="flex justify-between pt-4 border-t" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            <span class="text-2xl font-extrabold">Total:</span>
                            <span class="text-2xl font-extrabold text-orange-500">
                                {{ formatPrice(currentOrder.totalPrice) }} ฿
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div v-else class="text-center py-20" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                <p class="text-3xl font-bold mb-4">😔 Order Not Found</p>
                <p class="text-xl">The order ID '{{ orderIdInRoute }}' could not be found.</p>
                <p class="mt-4">Please check the order number or go back to your order history.</p>
            </div>

        </div>
        
        <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
        </button>
        
    </div>
</template>

<style scoped>
/* No additional CSS needed beyond Tailwind utilities */
</style>
