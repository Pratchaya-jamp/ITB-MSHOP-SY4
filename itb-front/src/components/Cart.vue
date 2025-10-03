<script setup>
import { ref, onMounted, computed } from 'vue'

// --- Mock Data: จำลองข้อมูลในตะกร้าสินค้า (อ้างอิงจากภาพตัวอย่าง) ---
const cartItems = ref([
    {
        id: 1,
        selected: true,
        image: '/sy4/phone/iPhone.png', // ใช้รูปจำลองเดียวกับ Landing Page
        name: 'Apple iPhone 14 Pro Max',
        description: ' (512GB, Space Black)',
        quantity: 1,
        pricePerUnit: 42900,
    },
    {
        id: 2,
        selected: true,
        image: '/sy4/phone/iPhone.png', // ใช้รูปจำลองเดียวกับ Landing Page
        name: 'Apple iPhone 13 mini',
        description: ' (128GB, Green)',
        quantity: 1,
        pricePerUnit: 19800,
    },
])

const shippingAddress = ref('123/45 Moo 6, T. Bangna, A. Bangna, Bangkok 10260')
const orderNote = ref('')

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

// --- Computed Properties ---

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

// คลาสสำหรับ Input/Textarea
const inputClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-700 border border-gray-600 text-white placeholder-gray-400' // เพิ่ม placeholder-gray-400 สำหรับ Dark Theme
        : 'bg-gray-50 border border-gray-300 text-gray-900 placeholder-gray-500' // เพิ่ม placeholder-gray-500 สำหรับ Light Theme
})

// คลาสสำหรับปุ่ม Quantity
const qtyButtonClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-700 hover:bg-gray-600 border-gray-600 text-white'
        : 'bg-gray-200 hover:bg-gray-300 border-gray-300 text-gray-800'
})


// สินค้าที่ถูกเลือกทั้งหมด
const selectedItems = computed(() => cartItems.value.filter(item => item.selected))

// ราคารวมของสินค้าที่ถูกเลือก
const totalSelectedPrice = computed(() => {
    return selectedItems.value.reduce((total, item) => total + (item.pricePerUnit * item.quantity), 0)
})

// จำนวนสินค้ารวมที่ถูกเลือก
const totalSelectedItems = computed(() => {
    return selectedItems.value.reduce((total, item) => total + item.quantity, 0)
})

// สถานะการเลือกสินค้าทั้งหมด
const allSelected = computed(() => {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected)
})

// Function สำหรับจัดรูปแบบราคา
const formatPrice = (price) => {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// --- Methods ---

// Toggle เลือก/ไม่เลือกสินค้าทั้งหมด
const toggleSelectAll = () => {
    const shouldSelectAll = !allSelected.value
    cartItems.value.forEach(item => item.selected = shouldSelectAll)
}

// เพิ่ม/ลดจำนวนสินค้า
const updateQuantity = (id, delta) => {
    const item = cartItems.value.find(i => i.id === id)
    if (item) {
        const newQuantity = item.quantity + delta
        if (newQuantity >= 1) {
            item.quantity = newQuantity
        }
    }
}

onMounted(() => {
    applyTheme(theme.value);
});
</script>

<template>
    <div :class="themeClass" class="p-4 w-full min-h-screen font-sans transition-colors duration-500">
        <div 
            :class="[
                // คลาสคงที่ (Static classes)
                'px-6 md:px-20 py-4 flex justify-between items-center transition-all duration-300 border-b',
                // คลาสแบบไดนามิก: สีพื้นหลังและเส้นขอบ
                theme === 'dark' ? 'bg-gray-950/80 border-gray-700' : 'bg-white/80 border-gray-200'
            ]"
        >
            <div class="text-2xl font-extrabold" :class="theme === 'dark' ? 'text-white' : 'text-gray-950'">
            </div>

            <div class="flex items-center space-x-4">
                <div class="relative cursor-pointer">
                    <svg version="1.1" xmlns="http://www.w3.org/2000/svg"
                        class="h-8 w-8"
                        :class="theme === 'dark' ? 'text-white' : 'text-black'"
                        viewBox="0 0 128 128"
                        fill="currentColor">
                        <g>
                            <path
                                d="M125.1 43.6h-20.4V17.5H84.4v-2.9H46.5v2.9H26.2v26.2H2.9C1.3 43.7 0 45 0 46.6v8.7c0 1.6 1.3 2.9 2.9 2.9h122.2c1.6 0 2.9-1.3 2.9-2.9v-8.7c0-1.7-1.3-3-2.9-3zm-26.2 0H32V23.3h14.5v2.9h37.8v-2.9h14.5v20.3zm-78.5 64c0 3.2 2.6 5.8 5.8 5.8h72.7c3.2 0 5.8-2.6 5.8-5.8l14.5-46.5H8.7l11.7 46.5zm61.1-36.3c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3z" />
                        </g>
                    </svg>
                    <span class="absolute -top-3 -right-3 px-2 py-0.5 text-xs font-bold leading-none text-white transform translate-x-1/2 translate-y-1/2 bg-red-500 rounded-full itbms-cart-quantity">
                        {{ cartItems.length }}
                    </span>
                </div>
            </div>
        </div>
        
        <div :class="contentBgClass" class="px-6 md:px-20 py-12">
            <h1 class="text-4xl font-extrabold mb-10">Shopping Cart</h1>
            
            <div class="flex flex-col lg:flex-row gap-10">
                
                <div class="lg:w-2/3 space-y-6">
                    <div :class="cardClass" class="p-6 rounded-3xl">
                        <div class="flex items-center space-x-3 pb-4 border-b" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" class="form-checkbox h-5 w-5 rounded itbms-select-all" 
                                :class="{'text-orange-500 border-gray-400 focus:ring-orange-500': theme === 'light', 'text-orange-400 bg-gray-700 border-gray-600 focus:ring-orange-400': theme === 'dark'}" />
                            <span class="font-semibold text-lg">Select All</span>
                            <span class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">( {{ cartItems.length }} Items )</span>
                        </div>

                        <div class="divide-y" :class="theme === 'dark' ? 'divide-gray-700' : 'divide-gray-200'">
                            <div v-for="item in cartItems" :key="item.id" class="flex items-start py-6 itbms-item-row">
                                <div class="flex items-center space-x-4">
                                    <input type="checkbox" v-model="item.selected" class="form-checkbox h-5 w-5 rounded itbms-select-item" 
                                        :class="{'text-orange-500 border-gray-400 focus:ring-orange-500': theme === 'light', 'text-orange-400 bg-gray-700 border-gray-600 focus:ring-orange-400': theme === 'dark'}" />
                                    <img :src="item.image" :alt="item.name" class="w-20 h-20 object-contain rounded-lg itbms-item-image" />
                                </div>

                                <div class="flex-grow ml-4">
                                    <p class="text-lg font-bold itbms-item-name">{{ item.name }}</p>
                                    <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400 itbms-item-description' : 'text-gray-600 itbms-item-description'">{{ item.description }}</p>
                                    <div class="flex items-center text-sm mt-1 itbms-select-nickname">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-orange-500" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" /></svg>
                                        <span class="font-semibold itbms-nickname">Samsun</span>
                                    </div>
                                </div>
                                
                                <div class="flex items-center justify-center space-x-1 mr-6 mt-2 lg:mt-0 itbms-item-quantity">
                                    <button @click="updateQuantity(item.id, -1)" class="p-2 border rounded-l-full itbms-dec-qty-button" :class="qtyButtonClass">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M20 12H4" /></svg>
                                    </button>
                                    <input type="text" v-model.number="item.quantity" class="w-12 text-center p-2 border-t border-b itbms-item-quantity-input" readonly
                                        :class="{'bg-transparent': theme === 'dark', 'bg-white': theme === 'light', 'border-gray-600': theme === 'dark', 'border-gray-300': theme === 'light'}" />
                                    <button @click="updateQuantity(item.id, 1)" class="p-2 border rounded-r-full itbms-inc-qty-button" :class="qtyButtonClass">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" /></svg>
                                    </button>
                                </div>

                                <div class="text-right flex-shrink-0 mt-2 lg:mt-0">
                                    <p class="text-xl font-extrabold text-orange-500 itbms-item-total-price">
                                        Price: {{ formatPrice(item.pricePerUnit * item.quantity) }} ฿
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="lg:w-1/3 space-y-6">
                    <div :class="cardClass" class="p-6 rounded-3xl">
                        <h2 class="text-2xl font-bold mb-4">Cart Summary</h2>

                        <div class="mb-6 pb-4 border-b" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            <h3 class="font-semibold mb-2">Ship To</h3>
                            <textarea rows="3" :value="shippingAddress" @input="shippingAddress = $event.target.value" 
                                class="w-full p-3 rounded-xl text-sm resize-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all itbms-shipping-address"
                                :class="inputClass" placeholder="Address No, Street, Subdistrict, District, Province, Postal Code"></textarea>
                        </div>
                        
                        <div class="mb-6 pb-4 border-b" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            <h3 class="font-semibold mb-2">Note</h3>
                            <textarea rows="3" v-model="orderNote" 
                                class="w-full p-3 rounded-xl text-sm resize-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all itbms-order-note"
                                :class="inputClass" placeholder="Additional Instructions or requests"></textarea>
                        </div>

                        <div class="space-y-2 mb-6">
                            <div class="flex justify-between font-medium">
                                <span>Total items :</span>
                                <span class="itbms-total-order-items">{{ totalSelectedItems }}</span>
                            </div>
                            <div class="flex justify-between font-extrabold text-xl">
                                <span>Total price :</span>
                                <span class="text-orange-500 itbms-total-order-price">
                                    Baht {{ formatPrice(totalSelectedPrice) }}
                                </span>
                            </div>
                        </div>

                        <button 
                            :disabled="totalSelectedItems === 0"
                            class="w-full px-8 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-0.5 hover:scale-[1.01] disabled:from-gray-400 disabled:to-gray-500 disabled:shadow-none disabled:transform-none itbms-place-order-button">
                            Place Order
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
        </button>
        
        </div>
</template>

<style scoped>
/*
 * Style สำหรับ checkbox และ input quantity เพื่อให้ดูเข้ากับ theme
 */
.form-checkbox {
    /* ใช้สีของแบรนด์เมื่อถูกเลือก */
    --tw-ring-color: var(--tw-color-orange-500);
    appearance: none;
    border-width: 1px;
    height: 1.25rem; /* 20px */
    width: 1.25rem; /* 20px */
    cursor: pointer;
}

/* Dark Theme Specific Styles */
.dark-theme .form-checkbox {
    --tw-ring-color: var(--tw-color-orange-400);
    background-color: #374151; /* bg-gray-700 */
    border-color: #4b5563; /* border-gray-600 */
    color: #f97316; /* text-orange-500 for checkmark */
}
.dark-theme .form-checkbox:checked {
    background-color: #f97316; /* orange-500 */
    border-color: #f97316;
}

/* Light Theme Specific Styles */
.form-checkbox:checked {
    background-color: #f97316; /* orange-500 */
    border-color: #f97316;
}

.itbms-item-quantity-input:focus {
    outline: none;
}
</style>