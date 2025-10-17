<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import { getItemsWithAuth, addItemWithAuth } from '@/libs/fetchUtilsOur';
import Cookies from 'js-cookie'
// 1. Import 'theme' จาก Store ส่วนกลาง
import { theme } from '@/stores/themeStore.js'; // <-- ตรวจสอบว่า Path ไปยังไฟล์ store ถูกต้อง

// --- โค้ดส่วน Logic ทั้งหมดเหมือนเดิมทุกประการ ไม่มีการแก้ไข ---
const cartItems = ref([])
const router = useRouter()
const totalCartCountKey = 'total_cart_count'

const showNotification = ref(false);
const notificationMessage = ref("");
let notificationTimeout = null;

const triggerNotification = (message) => {
    if (notificationTimeout) {
        clearTimeout(notificationTimeout);
    }
    notificationMessage.value = message;
    showNotification.value = true;
    notificationTimeout = setTimeout(() => {
        showNotification.value = false;
    }, 1500); // Popup จะหายไปใน 3 วินาที
};

function saveCartToLocalStorage() {
    const token = Cookies.get("access_token");
    if (!token) { return; }
    let userId = null;
    try {
        const decoded = jwtDecode(token);
        userId = decoded.id;
    } catch (err) { return; }

    localStorage.setItem(`CartData_${userId}`, JSON.stringify({ items: cartItems.value }));
    const newCartCount = cartItems.value.reduce((sum, item) => sum + item.quantity, 0);
    localStorage.setItem(totalCartCountKey, newCartCount.toString());
}

function loadCartFromLocalStorage() {
    const token = Cookies.get("access_token");
    if (!token) { cartItems.value = []; return; }
    let userId = null;
    try {
        const decoded = jwtDecode(token);
        userId = decoded.id;
    } catch (err) { return; }
    const cartKey = `CartData_${userId}`;
    const savedCart = localStorage.getItem(cartKey);

    if (savedCart) {
        try {
            const parsed = JSON.parse(savedCart);
            cartItems.value = (parsed.items || []).map(item => ({
                ...item,
                sellernickname: item.sellernickname || `Seller ID ${item.sellerId}`,
                selected: item.selected ?? false
            }));
            const newCartCount = cartItems.value.reduce((sum, item) => sum + item.quantity, 0);
            localStorage.setItem(totalCartCountKey, newCartCount.toString());
        } catch (err) { console.error("Invalid JSON in localStorage:", err); }
    } else {
        cartItems.value = [];
    }
}

const groupedCartItems = computed(() => {
    const groups = cartItems.value.reduce((groups, item) => {
        const name = item.sellernickname;
        if (!groups[item.sellerId]) {
            groups[item.sellerId] = {
                sellerId: item.sellerId,
                sellernickname: name,
                items: [],
                sellerAllSelected: false
            };
        }
        groups[item.sellerId].items.push(item);
        return groups;
    }, {});
    Object.values(groups).forEach(group => {
        group.sellerAllSelected = group.items.length > 0 && group.items.every(item => item.selected);
    });
    return Object.values(groups);
});

const deleteSelectedItems = () => {
    const itemsToDelete = selectedItems.value;
    if (itemsToDelete.length === 0) { return; }

    const itemCount = itemsToDelete.length;
    const quantityCount = itemsToDelete.reduce((sum, item) => sum + item.quantity, 0);

    cartItems.value = cartItems.value.filter(item => !item.selected);
    saveCartToLocalStorage();

    triggerNotification(`Removed ${itemCount} item(s) (${quantityCount} total units).`);
}

const deleteItemFromCart = (saleItemId) => {
    const itemToDelete = cartItems.value.find(item => item.saleItemId === saleItemId);
    if (!itemToDelete) return;

    const quantityCount = itemToDelete.quantity;

    cartItems.value = cartItems.value.filter(item => item.saleItemId !== saleItemId);
    saveCartToLocalStorage();

    triggerNotification(`Removed 1 item (${quantityCount} total units).`);
}

const shippingAddress = ref('')
const orderNote = ref('')
const showConfirmationOrder = ref(false)
const isLoading = ref(false)

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

const selectedItems = computed(() => cartItems.value.filter(item => item.selected))
const totalSelectedPrice = computed(() => selectedItems.value.reduce((total, item) => total + (item.price * item.quantity), 0))
const totalSelectedItems = computed(() => selectedItems.value.reduce((total, item) => total + item.quantity, 0))
const allSelected = computed(() => cartItems.value.length > 0 && cartItems.value.every(item => item.selected))

const formatPrice = (price) => price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')

const toggleSelectAll = () => {
    const shouldSelectAll = !allSelected.value
    cartItems.value.forEach(item => item.selected = shouldSelectAll)
}

const toggleSelectSeller = (sellerId) => {
    const itemsOfSeller = cartItems.value.filter(item => item.sellerId === sellerId);
    const shouldSelect = !itemsOfSeller.every(item => item.selected);
    itemsOfSeller.forEach(item => { item.selected = shouldSelect; });
}

const updateQuantity = (saleItemId, delta) => {
    const item = cartItems.value.find(i => i.saleItemId === saleItemId)
    if (item) {
        const newQuantity = item.quantity + delta
        if (newQuantity < 1) {
            item.quantity = 1
        } else if (item.maxquantity && newQuantity > item.maxquantity) {
            item.quantity = item.maxquantity
        } else {
            item.quantity = newQuantity
        }
        saveCartToLocalStorage();
    }
}

const canFillAddress = computed(() => totalSelectedItems.value > 0)
const canPlaceOrder = computed(() => totalSelectedItems.value > 0 && shippingAddress.value.trim() !== '')
const openConfirmOrder = () => { if (canPlaceOrder.value) { showConfirmationOrder.value = true } }
const cancelPlaceOrder = () => { showConfirmationOrder.value = false }
const confirmPlaceOrder = async () => {
    showConfirmationOrder.value = false
    await addorder()
}

onMounted(() => {
    loadCartFromLocalStorage()
});

const addorder = async () => {
    isLoading.value = true
    const token = Cookies.get('access_token');
    if (!token) { isLoading.value = false; return; }

    let buyerId = null;
    try {
        const decodedToken = jwtDecode(token);
        buyerId = decodedToken.id;
    } catch (err) { isLoading.value = false; return; }

    const selected = cartItems.value.filter(item => item.selected);
    if (selected.length === 0 || shippingAddress.value.trim() === '') {
        isLoading.value = false;
        return;
    }

    const groupedBySeller = selected.reduce((groups, item) => {
        if (!groups[item.sellerId]) groups[item.sellerId] = [];
        groups[item.sellerId].push(item);
        return groups;
    }, {});

    const orderPayload = Object.entries(groupedBySeller).map(([sellerId, items]) => ({
        buyerId,
        sellerId: Number(sellerId),
        orderDate: new Date().toISOString(),
        orderNote: orderNote.value,
        shippingAddress: shippingAddress.value,
        orderStatus: 'COMPLETED',
        orderItems: items.map((item, index) => ({
            no: index + 1,
            saleItemId: item.saleItemId,
            price: item.price,
            quantity: item.quantity,
            description: item.description
        }))
    }));

    try {
        const response = await addItemWithAuth(
            `${import.meta.env.VITE_BACKEND}/v2/orders`,
            orderPayload, false, token
        );
        if (response.status === 201 || response.status === 200) {
            cartItems.value = cartItems.value.filter(item => !item.selected);
            saveCartToLocalStorage();
            setTimeout(() => {
                isLoading.value = false;
                router.push({ path: '/sale-items', query: { orderSuccess: 'true' } });
            }, 1000);
        } else {
            console.error('Failed to place order:', response.status, response.data);
            isLoading.value = false;
        }
    } catch (error) {
        isLoading.value = false;
        console.error('Error placing order:', error);
    }
};

</script>

<template>
    <div :class="themeClass" class="min-h-screen font-sans transition-colors duration-300">

        <transition name="slide-down">
            <div v-if="showNotification"
                class="fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 p-4 rounded-lg shadow-lg bg-red-500 text-white">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round"
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                <span class="font-semibold">{{ notificationMessage }}</span>
            </div>
        </transition>

        <div class="container mx-auto px-6 py-12">
            <h1 class="text-4xl font-extrabold tracking-tight mb-10"
                :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Shopping Cart</h1>

            <div v-if="cartItems.length > 0" class="grid lg:grid-cols-3 lg:gap-12">
                <div class="lg:col-span-2 space-y-6">
                    <div class="flex items-center justify-between p-4 rounded-xl"
                        :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-slate-100'">
                        <div class="flex items-center space-x-4">
                            <input type="checkbox" :checked="allSelected" @change="toggleSelectAll"
                                class="form-checkbox h-5 w-5 rounded itbms-select-all" />
                            <label @click="toggleSelectAll" class="font-semibold cursor-pointer">Select All ({{
                                cartItems.length }} items)</label>
                        </div>
                        <button @click="deleteSelectedItems" :disabled="selectedItems.length === 0"
                            class="flex items-center space-x-2 px-4 py-2 text-sm font-semibold rounded-full transition-colors itbms-delete-selected-button"
                            :class="selectedItems.length > 0 ? 'text-red-500 bg-red-500/10 hover:bg-red-500/20' : 'text-slate-500 dark:text-slate-400 cursor-not-allowed'">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24"
                                stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                            <span>Delete</span>
                        </button>
                    </div>

                    <div v-for="group in groupedCartItems" :key="group.sellerId" class="p-6 rounded-2xl space-y-4"
                        :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-white shadow-sm'">
                        <div class="flex items-center space-x-4 pb-4 border-b"
                            :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                            <input type="checkbox" :checked="group.sellerAllSelected"
                                @change="toggleSelectSeller(group.sellerId)"
                                class="form-checkbox h-5 w-5 rounded itbms-select-seller-all" />
                            <div class="flex items-center font-bold itbms-select-nickname">
                                <svg xmlns="http://www.w3.org/2000/svg"
                                    class="h-5 w-5 mr-2 text-indigo-500 dark:text-indigo-400" viewBox="0 0 20 20"
                                    fill="currentColor">
                                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                                        clip-rule="evenodd" />
                                </svg>
                                <span class="itbms-nickname">{{ group.sellernickname }}</span>
                            </div>
                        </div>

                        <div v-for="item in group.items" :key="item.saleItemId"
                            class="flex items-center space-x-4 pt-4 itbms-item-row">
                            <input type="checkbox" v-model="item.selected"
                                class="form-checkbox h-5 w-5 rounded flex-shrink-0 itbms-select-item" />
                            <img src="/phone/iPhone.png" :alt="item.description"
                                class="w-20 h-20 object-contain rounded-lg flex-shrink-0 itbms-item-image"
                                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" />

                            <div class="flex-grow">
                                <p class="font-bold itbms-item-name">{{ item.model }}</p>
                                <p class="text-sm itbms-item-description"
                                    :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">{{ item.description
                                    }}</p>
                                <p class="font-semibold mt-1 text-indigo-600 dark:text-indigo-400">{{
                                    formatPrice(item.price) }} ฿</p>
                            </div>

                            <div class="flex items-center space-x-2 flex-shrink-0 itbms-item-quantity">
                                <button @click="updateQuantity(item.saleItemId, -1)" :disabled="item.quantity <= 1"
                                    class="p-2 rounded-full itbms-dec-qty-button disabled:opacity-50 disabled:cursor-not-allowed"
                                    :class="theme === 'dark' ? 'bg-gray-700 hover:bg-gray-600' : 'bg-slate-200 hover:bg-slate-300'"><svg
                                        xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none"
                                        viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M20 12H4" />
                                    </svg></button>
                                <span class="w-8 text-center font-semibold itbms-item-quantity-input">{{ item.quantity
                                    }}</span>
                                <button @click="updateQuantity(item.saleItemId, 1)"
                                    :disabled="item.maxquantity && item.quantity >= item.maxquantity"
                                    class="p-2 rounded-full itbms-inc-qty-button disabled:opacity-50 disabled:cursor-not-allowed"
                                    :class="theme === 'dark' ? 'bg-gray-700 hover:bg-gray-600' : 'bg-slate-200 hover:bg-slate-300'"><svg
                                        xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none"
                                        viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                                    </svg></button>
                            </div>

                            <button @click="deleteItemFromCart(item.saleItemId)"
                                class="p-2 rounded-full flex-shrink-0 itbms-delete-item-button"
                                :class="theme === 'dark' ? 'text-slate-400 hover:bg-gray-700 hover:text-red-500' : 'text-slate-500 hover:bg-slate-200 hover:text-red-500'"><svg
                                    xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
                                    stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                                </svg></button>
                        </div>
                    </div>
                </div>

                <div class="lg:col-span-1">
                    <div class="sticky top-28 space-y-6">
                        <div class="p-6 rounded-2xl"
                            :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-white shadow-sm'">
                            <h2 class="text-2xl font-bold mb-6"
                                :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Order Summary</h2>
                            <div class="space-y-4">
                                <div>
                                    <label class="block mb-2 text-sm font-semibold">Ship To</label>
                                    <textarea rows="3" v-model="shippingAddress" :disabled="!canFillAddress"
                                        class="w-full p-3 rounded-lg text-sm resize-none focus:ring-2 focus:ring-indigo-500 border-0 outline-none transition itbms-shipping-address disabled:opacity-50 disabled:cursor-not-allowed"
                                        :class="theme === 'dark'
                                            ? 'bg-gray-700/50 placeholder-slate-400'
                                            : 'bg-slate-100 placeholder-slate-500'"
                                        placeholder="Address, Street, City, Postal Code..."></textarea>
                                </div>
                                <div>
                                    <label class="block mb-2 text-sm font-semibold">Order Note</label>
                                    <textarea rows="2" v-model="orderNote" :disabled="!canFillAddress"
                                        class="w-full p-3 rounded-lg text-sm resize-none focus:ring-2 focus:ring-indigo-500 border-0 outline-none transition itbms-order-note disabled:opacity-50 disabled:cursor-not-allowed"
                                        :class="theme === 'dark'
                                            ? 'bg-gray-700/50 placeholder-slate-400'
                                            : 'bg-slate-100 placeholder-slate-500'"
                                        placeholder="Optional: e.g. special delivery instructions"></textarea>
                                </div>
                            </div>
                        </div>
                        <div class="p-6 rounded-2xl"
                            :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-white shadow-sm'">
                            <div class="space-y-3 mb-6">
                                <div class="flex justify-between font-medium">
                                    <span>Selected Items</span>
                                    <span class="itbms-total-order-items">{{ totalSelectedItems }}</span>
                                </div>
                                <div class="flex justify-between font-bold text-xl">
                                    <span>Total Price</span>
                                    <span class="text-indigo-600 dark:text-indigo-400 itbms-total-order-price">{{
                                        formatPrice(totalSelectedPrice) }} ฿</span>
                                </div>
                            </div>
                            <button @click="openConfirmOrder" :disabled="!canPlaceOrder"
                                class="w-full py-4 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1 disabled:bg-slate-400 disabled:shadow-none disabled:transform-none disabled:cursor-not-allowed dark:disabled:bg-gray-600 itbms-place-order-button">
                                Place Order
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div v-else class="text-center py-20">
                <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto h-24 w-24"
                    :class="theme === 'dark' ? 'text-gray-700' : 'text-slate-300'" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor" stroke-width="1">
                    <path stroke-linecap="round" stroke-linejoin="round"
                        d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <h2 class="mt-6 text-2xl font-bold" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Your cart is empty</h2>
                <p class="mt-2 text-slate-500 dark:text-slate-400">Looks like you haven't added anything to your cart
                    yet.</p>
                <button @click="() => router.push('/sale-items')"
                    class="mt-8 px-8 py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 hover:bg-indigo-700 transition-all duration-300 transform hover:-translate-y-1">
                    Continue Shopping
                </button>
            </div>
        </div>

        <transition name="bounce-popup">
            <div v-if="showConfirmationOrder" class="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm Order</h2>
                    <p class="itbms-message mb-6 text-lg">Place an order for {{ totalSelectedItems }} item(s)?</p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmPlaceOrder"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95">Confirm</button>
                        <button @click="cancelPlaceOrder"
                            class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95">Cancel</button>
                    </div>
                </div>
            </div>
        </transition>

        <transition name="fade-background">
            <div v-if="isLoading" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
                <div class="p-8 rounded-2xl shadow-xl text-center"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <svg class="animate-spin h-8 w-8 text-indigo-500 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg"
                        fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
                    </svg>
                    <p class="itbms-message text-lg font-medium">Placing your order...</p>
                </div>
            </div>
        </transition>
    </div>
</template>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}

.slide-down-enter-from,
.slide-down-leave-to {
    transform: translateY(-150%);
    opacity: 0;
}

/* Custom styles for checkbox to match the new theme */
.form-checkbox {
    appearance: none;
    background-color: transparent;
    border-radius: 0.375rem;
    /* rounded */
    border-width: 2px;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
}

.dark .form-checkbox {
    border-color: #4b5563;
    /* slate-600 */
}

.light .form-checkbox {
    border-color: #cbd5e1;
    /* slate-300 */
}

.form-checkbox:checked {
    background-color: #6366f1;
    /* indigo-500 */
    border-color: #6366f1;
    background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
}

.form-checkbox:focus {
    outline: 2px solid transparent;
    outline-offset: 2px;
    --tw-ring-color: #818cf8;
    /* indigo-400 */
    --tw-ring-offset-shadow: 0 0 #0000;
    --tw-ring-shadow: 0 0 #0000;
    box-shadow: var(--tw-ring-offset-shadow), var(--tw-ring-shadow), var(--tw-shadow, 0 0 #0000);
    box-shadow: 0 0 0 2px var(--tw-ring-color);
}

/* Animations */
.bounce-popup-enter-active,
.bounce-popup-leave-active {
    transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.bounce-popup-enter-from {
    transform: scale(0.8);
    opacity: 0;
}

.bounce-popup-leave-to {
    transform: scale(1.2);
    opacity: 0;
}

.fade-background-enter-active,
.fade-background-leave-active {
    transition: opacity 0.3s ease;
}

.fade-background-enter-from,
.fade-background-leave-to {
    opacity: 0;
}
</style>