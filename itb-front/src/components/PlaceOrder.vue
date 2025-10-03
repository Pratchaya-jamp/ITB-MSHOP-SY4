<script setup>
import { ref, onMounted, computed } from 'vue'

// --- Mock Data: จำลองข้อมูลประวัติการสั่งซื้อ (อ้างอิงจากภาพตัวอย่าง) ---
const orders = ref([
    {
        id: '12345',
        nickname: 'Somsun',
        orderDate: 'October 1, 2025',
        paymentDate: 'October 1, 2025',
        status: 'Completed',
        shippingAddress: 'Somsun Jaidee, 123/45 Moo 6, T. Bangna, A. Bangna, Bangkok 10260',
        orderNote: 'กรุณาโทรแจ้งก่อนส่งมอบสินค้า',
        totalPrice: 62700,
        items: [
            {
                name: 'Apple iPhone 14 Pro Max',
                description: ' (512GB, Space Black)',
                quantity: 1,
                price: 42900,
            },
            {
                name: 'Apple iPhone 13 mini',
                description: ' (128GB, Green)',
                quantity: 1,
                price: 19800,
            },
        ],
    },
    {
        id: '12346',
        nickname: 'Somsun',
        orderDate: 'September 20, 2025',
        paymentDate: 'September 20, 2025',
        status: 'Cancelled',
        shippingAddress: 'Somsun Jaidee, 123/45 Moo 6, T. Bangna, A. Bangna, Bangkok 10260',
        orderNote: 'ต้องการเปลี่ยนสี',
        totalPrice: 20000,
        items: [
            {
                name: 'Xiaomi 13T',
                description: ' (256GB, Black)',
                quantity: 1,
                price: 20000,
            },
        ],
    },
])

// สถานะที่เลือกในหน้า (Completed/Cancelled)
const selectedTab = ref('Completed')

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

// --- Computed Properties (ใช้ร่วมกับ Theme) ---

// คลาสสำหรับ Theme หลัก
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

// คลาสสำหรับพื้นหลังส่วนเนื้อหาหลัก
const contentBgClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-900' // มืดกว่านิดหน่อย
        : 'bg-gray-100' // สว่างกว่านิดหน่อย
})

// คลาสสำหรับ Card/Box ต่างๆ
const cardClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-800 shadow-xl border border-gray-700'
        : 'bg-white shadow-xl border border-gray-300'
})

// คลาสสำหรับ Tab Active
const tabActiveClass = computed(() => {
    return theme.value === 'dark'
        ? 'border-orange-500 text-orange-500 font-bold'
        : 'border-orange-500 text-orange-500 font-bold'
})

// คลาสสำหรับ Tab Inactive
const tabInactiveClass = computed(() => {
    return theme.value === 'dark'
        ? 'text-gray-400 border-transparent hover:text-white'
        : 'text-gray-500 border-transparent hover:text-gray-800'
})

// Orders ที่ถูกกรองตาม Tab ที่เลือก
const filteredOrders = computed(() => {
    return orders.value.filter(order => order.status === selectedTab.value)
})

// Function สำหรับจัดรูปแบบราคา
const formatPrice = (price) => {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// Map สถานะไปยังสี
const getStatusClass = (status) => {
    if (status === 'Completed') return 'text-green-500 bg-green-500/10'
    if (status === 'Cancelled') return 'text-red-500 bg-red-500/10'
    return 'text-yellow-500 bg-yellow-500/10'
}

onMounted(() => {
    applyTheme(theme.value);
});
</script>

<template>
    <div :class="themeClass" class="p-4 w-full min-h-screen font-sans transition-colors duration-500">
        
        
        <div :class="contentBgClass" class="px-6 md:px-20 py-12">
            <h1 class="text-4xl font-extrabold mb-8">Your Orders</h1>
            
            <div class="flex border-b mb-6" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'">
                <button 
                    @click="selectedTab = 'Completed'" 
                    class="pb-2 px-4 transition-all duration-200"
                    :class="selectedTab === 'Completed' ? tabActiveClass : tabInactiveClass"
                >
                    Completed
                </button>
                <button 
                    @click="selectedTab = 'Cancelled'" 
                    class="pb-2 px-4 transition-all duration-200"
                    :class="selectedTab === 'Cancelled' ? tabActiveClass : tabInactiveClass"
                >
                    Cancelled
                </button>
            </div>

            <div class="space-y-8">
                <div v-for="order in filteredOrders" :key="order.id" :class="cardClass" class="p-6 rounded-3xl itbms-row">
                    <div class="flex flex-wrap items-center justify-between pb-4 border-b" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                        <div class="flex items-center space-x-2 itbms-nickname mb-2 sm:mb-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-orange-500" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" /></svg>
                            <span class="font-semibold">{{ order.nickname }}</span>
                        </div>
                        <div class="flex flex-wrap text-sm gap-x-4 gap-y-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                            <span class="itbms-order-id">Order No: <span :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">{{ order.id }}</span></span>
                            <span class="itbms-order-date">Order Date: <span :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">{{ order.orderDate }}</span></span>
                            <span class="itbms-payment-date">Payment Date: <span :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">{{ order.paymentDate }}</span></span>
                        </div>
                    </div>

                    <div class="mt-4 mb-4 text-sm space-y-2" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                        <p class="itbms-shipping-address">
                            <span class="font-semibold" :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">Shipped To:</span> {{ order.shippingAddress }}
                        </p>
                        <p class="itbms-order-note">
                            <span class="font-semibold" :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">Note:</span> {{ order.orderNote || 'No additional instructions.' }}
                        </p>
                    </div>

                    <div class="flex justify-between items-center pt-4 border-t" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                        <div class="font-extrabold text-xl">
                            <span :class="theme === 'dark' ? 'text-white' : 'text-gray-950'">Total:</span> 
                            <span class="text-orange-500 itbms-total-order-price">
                                Baht {{ formatPrice(order.totalPrice) }}
                            </span>
                        </div>
                        <div class="itbms-order-status text-sm font-semibold px-3 py-1 rounded-full" :class="getStatusClass(order.status)">
                            {{ order.status }}
                        </div>
                    </div>

                    <div class="divide-y mt-6" :class="theme === 'dark' ? 'divide-gray-700' : 'divide-gray-200'">
                        <div v-for="item in order.items" :key="item.name" class="flex justify-between items-center py-4 itbms-item-row">
                            <div class="flex items-center space-x-4">
                                <img src="/phone/iPhone.png" alt="Product Image" class="w-12 h-12 object-contain rounded-lg"/>
                                <div class="flex-grow">
                                    <p class="font-medium itbms-item-name">{{ item.name }}</p>
                                    <p class="text-xs itbms-item-description" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">{{ item.description }}</p>
                                </div>
                            </div>

                            <div class="flex space-x-4 text-right items-center text-sm">
                                <span class="itbms-item-quantity" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Qty {{ item.quantity }}</span>
                                <span class="font-bold text-lg text-orange-500 itbms-item-total-price">
                                    {{ formatPrice(item.price) }} ฿
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="filteredOrders.length === 0" class="text-center py-10" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                    <p class="text-xl">No {{ selectedTab.toLowerCase() }} orders found.</p>
                </div>
            </div>

        </div>
        <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
        </button>
        
    </div>
</template>

<style scoped>
/* เนื่องจากเราใช้ Utility Class จาก TailwindCSS เกือบทั้งหมด 
   จึงมีเพียง CSS ที่จำเป็นสำหรับการแสดงผลเท่านั้น */
</style>