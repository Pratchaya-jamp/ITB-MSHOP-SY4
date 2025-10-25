<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router';
import { getItemByIdWithAuth } from '@/libs/fetchUtilsOur'; 
import Cookies from 'js-cookie' 
import { jwtDecode } from 'jwt-decode' 
import { theme } from '@/stores/themeStore.js'

const router = useRouter();
const route = useRoute(); 
const orderId = route.params.id; 

const currentOrder = ref(null);
const isLoading = ref(true);

// --- Computed Properties ---
const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'dark bg-gray-900 text-slate-200'
        : 'bg-slate-50 text-slate-800'
});

const orderTotal = computed(() => {
    if (!currentOrder.value || !currentOrder.value.orderItems) return 0;
    return currentOrder.value.orderItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
});

// ✨ FIX: เพิ่ม computed property สำหรับเช็คสถานะ Cancelled
const isOrderCancelled = computed(() => {
    if (!currentOrder.value?.orderStatus) return false;
    const status = currentOrder.value.orderStatus.toUpperCase();
    return status === 'CANCELLED' || status === 'CANCELED';
});

const progressWidth = computed(() => {
    if (!currentOrder.value) return '0%';
    const steps = ['PLACED', 'PAID', 'COMPLETED'];
    const currentStepIndex = steps.indexOf(currentOrder.value.orderStatus?.toUpperCase());

    if (currentStepIndex === 0) return '0%';
    if (currentStepIndex === 1) return '50%';
    if (currentStepIndex >= 2) return '100%';
    return '0%';
});

const formatPrice = (price) => {
    if (price === undefined || price === null) return '0'
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const getStepStatus = (stepName, orderStatus) => {
    const steps = ['PLACED', 'PAID', 'COMPLETED'];
    const currentStepIndex = steps.indexOf(orderStatus?.toUpperCase());
    const thisStepIndex = steps.indexOf(stepName);

    if (thisStepIndex < currentStepIndex) return 'completed';
    if (thisStepIndex === currentStepIndex) return 'active';
    return 'upcoming';
};

async function fetchItemOrderbyId() {
  isLoading.value = true;
  const token = Cookies.get('access_token');
  if (!token) {
    router.push('/signin');
    return;
  }

  try {
    const response = await getItemByIdWithAuth(
      `${import.meta.env.VITE_BACKEND}/v2/orders`, // Base URL
      orderId, // ID จาก route
      token
    );

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
          router.push('/signin');
      }
      throw new Error('Failed to fetch order details');
    }

    const data = await response.json();
    currentOrder.value = data; 
    
  } catch (error) {
    console.error('Error fetching order details:', error);
    currentOrder.value = null;
  } finally {
    isLoading.value = false;
  }
}

onMounted(() => {
  fetchItemOrderbyId();
});
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans transition-colors duration-500">
    <main class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      
      <div v-if="isLoading" class="text-center py-20 animate-fade-in-up">
        <svg class="animate-spin h-12 w-12 text-indigo-500 mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
        <p class="mt-4 text-lg font-medium" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Loading Order Details...
        </p>
      </div>
      
      <div v-else-if="currentOrder" class="animate-fade-in-up">
        <div class="mb-8 text-center">
            <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Order #{{ currentOrder.id }}</p>
            
            <h1 v-if="isOrderCancelled" class="text-4xl font-bold text-red-500">
                Order Cancelled
            </h1>
            <h1 v-else class="text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">
                Thank you for your order!
            </h1>
        </div>

        <div v-if="!isOrderCancelled" class="mb-12 max-w-xl mx-auto">
            <div class="flex items-start">
                <div class="text-center w-20">
                    <div class="w-10 h-10 mx-auto rounded-full flex items-center justify-center transition-all duration-500 border-4"
                        :class="{
                            'bg-blue-500 border-blue-500': getStepStatus('PLACED', currentOrder.orderStatus) === 'completed' || getStepStatus('PLACED', currentOrder.orderStatus) === 'active',
                            'bg-gray-800 border-gray-800': theme === 'dark' && getStepStatus('PLACED', currentOrder.orderStatus) === 'upcoming',
                            'bg-gray-200 border-gray-200': theme !== 'dark' && getStepStatus('PLACED', currentOrder.orderStatus) === 'upcoming'
                        }">
                        <svg v-if="getStepStatus('PLACED', currentOrder.orderStatus) === 'completed' || getStepStatus('PLACED', currentOrder.orderStatus) === 'active'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
                    </div>
                    <p class="mt-2 text-xs font-semibold">Placed</p>
                </div>
                <div class="flex-1 h-1 mt-5 rounded-full transition-colors duration-500" 
                     :class="getStepStatus('PLACED', currentOrder.orderStatus) === 'completed' ? 'bg-blue-500' : (theme === 'dark' ? 'bg-white/10' : 'bg-black/5')">
                </div>
                <div class="text-center w-20">
                    <div class="w-10 h-10 mx-auto rounded-full flex items-center justify-center transition-all duration-500 border-4"
                        :class="{
                            'bg-blue-500 border-blue-500': getStepStatus('PAID', currentOrder.orderStatus) === 'completed' || getStepStatus('PAID', currentOrder.orderStatus) === 'active',
                            'bg-gray-800 border-gray-800': theme === 'dark' && getStepStatus('PAID', currentOrder.orderStatus) === 'upcoming',
                            'bg-gray-200 border-gray-200': theme !== 'dark' && getStepStatus('PAID', currentOrder.orderStatus) === 'upcoming'
                        }">
                        <svg v-if="getStepStatus('PAID', currentOrder.orderStatus) === 'completed' || getStepStatus('PAID', currentOrder.orderStatus) === 'active'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
                    </div>
                    <p class="mt-2 text-xs font-semibold" :class="getStepStatus('PAID', currentOrder.orderStatus) === 'upcoming' ? 'text-gray-500' : ''">Paid</p>
                </div>
                <div class="flex-1 h-1 mt-5 rounded-full transition-colors duration-500" 
                     :class="getStepStatus('PAID', currentOrder.orderStatus) === 'completed' ? 'bg-blue-500' : (theme === 'dark' ? 'bg-white/10' : 'bg-black/5')">
                </div>
                <div class="text-center w-20">
                    <div class="w-10 h-10 mx-auto rounded-full flex items-center justify-center transition-all duration-500 border-4"
                        :class="{
                            'bg-blue-500 border-blue-500': getStepStatus('COMPLETED', currentOrder.orderStatus) === 'completed' || getStepStatus('COMPLETED', currentOrder.orderStatus) === 'active',
                            'bg-gray-800 border-gray-800': theme === 'dark' && getStepStatus('COMPLETED', currentOrder.orderStatus) === 'upcoming',
                            'bg-gray-200 border-gray-200': theme !== 'dark' && getStepStatus('COMPLETED', currentOrder.orderStatus) === 'upcoming'
                        }">
                        <svg v-if="getStepStatus('COMPLETED', currentOrder.orderStatus) === 'completed' || getStepStatus('COMPLETED', currentOrder.orderStatus) === 'active'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
                    </div>
                    <p class="mt-2 text-xs font-semibold" :class="getStepStatus('COMPLETED', currentOrder.orderStatus) === 'upcoming' ? 'text-gray-500' : ''">Completed</p>
                </div>
            </div>
        </div>
        
        <div v-else class="mb-12 max-w-xl mx-auto p-6 rounded-2xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-red-900/30 border border-red-500/30' : 'bg-red-50 border border-red-200'">
            <div class="flex-shrink-0 w-12 h-12 flex items-center justify-center rounded-full bg-red-500/10 text-red-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                </svg>
            </div>
            <div>
                <h3 class="text-lg font-bold" :class="theme === 'dark' ? 'text-red-400' : 'text-red-700'">Order Status: Cancelled</h3>
                <p class="text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">This order was cancelled and will not be processed.</p>
            </div>
        </div>

        <div class="p-8 rounded-3xl space-y-8" :class="theme === 'dark' ? 'bg-gray-950/70 backdrop-blur-xl border border-white/10' : 'bg-white/70 backdrop-blur-xl border border-black/10'">
            <div class="grid md:grid-cols-2 gap-8">
                <div class="space-y-2">
                    <h2 class="text-lg font-semibold flex items-center gap-2"><svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M9 17a2 2 0 11-4 0 2 2 0 014 0zM19 17a2 2 0 11-4 0 2 2 0 014 0z" /><path stroke-linecap="round" stroke-linejoin="round" d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10l2 2h8a1 1 0 001-1z" /><path stroke-linecap="round" stroke-linejoin="round" d="M16 16h2a2 2 0 002-2V6a2 2 0 00-2-2h-1" /></svg>Shipping Address</h2>
                    <p class="text-sm leading-relaxed" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">{{ currentOrder.shippingAddress }}</p>
                </div>
                 <div class="space-y-2">
                    <h2 class="text-lg font-semibold flex items-center gap-2"><svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>Payment Summary</h2>
                    <div class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                        <p><strong>Subtotal:</strong> {{ formatPrice(orderTotal) }} ฿</p>
                        <p><strong>Total Paid:</strong> <span class="font-bold text-base" :class="theme === 'dark' ? 'text-white' : 'text-black'">{{ formatPrice(orderTotal) }} ฿</span></p>
                    </div>
                </div>
            </div>

            <div class="border-t pt-8" :class="theme === 'dark' ? 'border-white/10' : 'border-black/10'">
                <h2 class="text-lg font-semibold mb-4">Items in this Order ({{ currentOrder.orderItems.length }})</h2>
                <div class="space-y-4">
                    <div v-for="item in currentOrder.orderItems" :key="item.no" class="flex items-center gap-4 p-4 rounded-2xl" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
                        <img src="/phone/iPhone.png" alt="Product" class="w-16 h-16 object-contain rounded-lg flex-shrink-0">
                        <div class="flex-grow">
                            <p class="font-semibold">{{ item.description }}</p>
                            <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">{{ item.description }}</p>
                        </div>
                        <div class="text-right flex-shrink-0">
                            <p class="font-bold">{{ formatPrice(item.price * item.quantity) }} ฿</p>
                            <p class="text-xs" :class="theme === 'dark' ? 'text-gray-500' : 'text-gray-400'">{{ item.quantity }} x {{ formatPrice(item.price) }} ฿</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </div>

      <div v-else class="text-center py-20 animate-fade-in-up">
        <h1 class="text-3xl font-bold mb-2">Order Not Found</h1>
        <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">We couldn't find an order with the specified ID.</p>
        <button @click="router.push('/orders')" class="mt-6 px-8 py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full shadow-lg transition-all transform hover:scale-105">
            Back to Order History
        </button>
      </div>

    </main>
  </div>
</template>

<style scoped>
@keyframes fade-in-up {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
    animation: fade-in-up 0.6s ease-out forwards;
}
@keyframes blob {
    0% { transform: scale(1) translate(0px, 0px); }
    33% { transform: scale(1.1) translate(30px, -50px); }
    66% { transform: scale(0.9) translate(-20px, 20px); }
    100% { transform: scale(1) translate(0px, 0px); }
}
.animate-blob {
    animation: blob 7s infinite ease-in-out;
}
.animation-delay-2000 {
    animation-delay: 2s;
}
</style>