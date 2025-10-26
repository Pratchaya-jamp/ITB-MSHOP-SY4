<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { getItemsWithAuth } from "@/libs/fetchUtilsOur";
import Cookies from "js-cookie";
import { jwtDecode } from "jwt-decode";
import { theme } from "@/stores/themeStore.js";

const router = useRouter();
const orders = ref([]);
const selectedTab = ref("Completed");

const goToOrderDetail = (orderId) => {
    router.push(`/order/${orderId}`);
}

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})


const sortedFilteredOrders = computed(() => {
    if (!Array.isArray(orders.value)) {
        return [];
    }

    const upperSelectedTab = selectedTab.value.toUpperCase();

    return orders.value
        .filter((order) => order.orderStatus.toUpperCase() === upperSelectedTab) 
        .sort((a, b) => { 
            const dateDifference = new Date(b.orderDate) - new Date(a.orderDate);

            if (dateDifference !== 0) {
                return dateDifference;
            }

            return b.id - a.id;
        });
});


const formatPrice = (price) => {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

const formatDate = (dateTimeString) => {
    if (!dateTimeString) return "-";
    const date = new Date(dateTimeString.replace(" ", "T"));
    const options = {
        year: "numeric",
        month: "short",
        day: "2-digit",
    };
    return new Intl.DateTimeFormat("en-GB", options).format(date);
};

const getStatusClass = (status) => {
    const upperStatus = status.toUpperCase();
    if (upperStatus === "COMPLETED") return "text-green-500 bg-green-500/10";
    if (upperStatus === "CANCELLED" || upperStatus === "CANCELED") return "text-red-500 bg-red-500/10";
    return "text-yellow-500 bg-yellow-500/10";
};

async function fetchItemOrder() {
    const token = Cookies.get("access_token");
    if (!token) {
        router.push("/signin");
        return;
    }

    let userId, userRole, sellerId;
    try {
        const decodedToken = jwtDecode(token);
        userId = decodedToken.id;
        userRole = decodedToken.role;
        sellerId = decodedToken.seller_id;
    } catch (err) {
        console.error("Failed to decode token:", err);
        router.push("/signin");
        return;
    }

    const endpoint =
        `${import.meta.env.VITE_BACKEND}/v2/users/${userId}/orders`
    try {
        const response = await getItemsWithAuth(endpoint, { token });
        orders.value = response?.content ?? [];
    } catch (error) {
        console.error("Error fetching user orders:", error);
        orders.value = [];
    }
}

onMounted(() => {
    fetchItemOrder();
});
</script>

<template>
    <div :class="themeClass" class="min-h-screen font-sans">
        <div class="container mx-auto px-6 py-12">
            <h1 class="text-4xl font-extrabold tracking-tight mb-10" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
                Your Orders
            </h1>

            <div class="flex border-b mb-8" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                <button @click="selectedTab = 'Completed'"
                    class="pb-3 px-4 font-semibold transition-all duration-200 border-b-2" :class="selectedTab === 'Completed'
                            ? 'border-indigo-500 text-indigo-500 dark:text-indigo-400'
                            : 'border-transparent text-slate-500 hover:border-slate-400 dark:text-slate-400 dark:hover:border-slate-500'
                        ">
                    Completed
                </button>
                <button @click="selectedTab = 'Canceled'"
                    class="pb-3 px-4 font-semibold transition-all duration-200 border-b-2" :class="selectedTab === 'Canceled'
                            ? 'border-indigo-500 text-indigo-500 dark:text-indigo-400'
                            : 'border-transparent text-slate-500 hover:border-slate-400 dark:text-slate-400 dark:hover:border-slate-500'
                        ">
                    Cancelled
                </button>
            </div>

            <div class="space-y-8">
                <div v-for="order in sortedFilteredOrders" :key="order.id" 
                     class="p-6 rounded-2xl animate-fade-in-up cursor-pointer transition-all duration-300"
                    :class="theme === 'dark' ? 'bg-gray-800/50 hover:bg-gray-800' : 'bg-white shadow-sm hover:shadow-md'"
                    @click="goToOrderDetail(order.id)">
                    <div class="flex flex-wrap items-center justify-between gap-4 pb-4 border-b"
                        :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                        <div class="flex items-center font-bold text-lg itbms-select-nickname">
                            <svg xmlns="http://www.w3.org/2000/svg"
                                class="h-5 w-5 mr-2 text-indigo-500 dark:text-indigo-400" viewBox="0 0 20 20"
                                fill="currentColor">
                                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                                    clip-rule="evenodd" />
                            </svg>
                            <span v-if="order.seller" class="itbms-nickname">{{
                                order.seller.nickname
                                }}</span>
                            <span v-if="order.buyer" class="itbms-nickname">{{
                                order.buyer.username
                                }}</span>
                        </div>
                        <div class="text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                            Order
                            <span class="mx-2">·</span>
                            <span>{{ formatDate(order.orderDate) }}</span>
                        </div>
                    </div>

                    <div class="divide-y" :class="theme === 'dark' ? 'divide-white/10' : 'divide-slate-200'">
                        <div v-for="item in order.orderItems" :key="item.id"
                            class="flex items-center gap-4 py-4 itbms-item-row">
                            <img src="/phone/iPhone.png" alt="Product Image"
                                class="w-16 h-16 object-contain rounded-lg flex-shrink-0"
                                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" />
                            <div class="flex-grow">
                                <p class="font-bold itbms-item-name">{{ item.model }}</p>
                                <p class="text-sm itbms-item-description"
                                    :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                                    {{ item.description }}
                                </p>
                            </div>
                            <div class="text-right flex-shrink-0">
                                <p
                                    class="font-semibold text-lg text-indigo-600 dark:text-indigo-400 itbms-item-total-price">
                                    {{ formatPrice(item.price * item.quantity) }} ฿
                                </p>
                                <p class="text-sm itbms-item-quantity"
                                    :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                                    Qty: {{ item.quantity }}
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="flex flex-wrap items-end justify-between gap-4 pt-4 border-t"
                        :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                        <div>
                            <p class="text-sm font-semibold"
                                :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
                                Ship To:
                            </p>
                            <p class="text-sm itbms-shipping-address"
                                :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                                {{ order.shippingAddress }}
                            </p>
                        </div>
                        <div class="flex items-center gap-4">
                            <div class="text-right">
                                <span class="font-semibold">Total:</span>
                                <span
                                    class="font-bold text-xl ml-2 text-indigo-600 dark:text-indigo-400 itbms-total-order-price">
                                    {{
                                        formatPrice(
                                            order.orderItems.reduce(
                                                (sum, item) => sum + item.price * item.quantity,
                                                0
                                    )
                                    )
                                    }}
                                    ฿
                                </span>
                            </div>
                            <div class="itbms-order-status text-sm font-bold px-3 py-1 rounded-full"
                                :class="getStatusClass(order.orderStatus)">
                                {{ order.orderStatus }}
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="sortedFilteredOrders.length === 0"
                    class="text-center py-20 text-slate-500 dark:text-slate-400">
                    <p class="text-xl font-semibold">
                        No {{ selectedTab.toLowerCase() }} orders found.
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
@keyframes fade-in-up {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.itbms-row {
    opacity: 0;
    animation: fade-in-up 0.5s ease-out forwards;
}

.form-checkbox {
    appearance: none;
    background-color: transparent;
    border-radius: 0.375rem;
    border-width: 2px;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
}

.dark .form-checkbox {
    border-color: #4b5563;
}

.light .form-checkbox {
    border-color: #cbd5e1;
}

.form-checkbox:checked {
    background-color: #6366f1;
    border-color: #6366f1;
    background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
}

.form-checkbox:focus {
    outline: 2px solid transparent;
    outline-offset: 2px;
    box-shadow: 0 0 0 2px #818cf8;
}
</style>