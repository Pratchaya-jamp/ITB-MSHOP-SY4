<script setup>
import { ref, onMounted, watch, computed, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getItemById, deleteItemById } from "@/libs/fetchUtilsOur";
import { jwtDecode } from "jwt-decode";
import Cookies from "js-cookie";
import { theme } from "@/stores/themeStore.js";

const route = useRoute();
const router = useRouter();

const product = ref(null);
const id = route.params.id;

const imageLoading = ref(true);
const imageError = ref(false);
const productImages = ref([]);
const mainImage = ref("");

const showNotFoundPopup = ref(false);
const isDeleting = ref(false);
const showDeleteConfirmationPopup = ref(false);
const deleteResponseMessage = ref("");
const countdown = ref(3);
const showEditSuccessPopup = ref(false);
const showEditFallPopup = ref(false);
const userRole = ref(null);
const isAuthenticated = ref(false);
const cartCount = ref(0);
const itemQuantityToAddToCart = ref(1);

const showNotification = ref(false);
const notificationMessage = ref("");
const notificationSuccess = ref(false);
let notificationTimeout = null;

// --- ✨ 1. สร้าง State สำหรับติดตามจำนวนสินค้าชิ้นนี้ในตะกร้าโดยเฉพาะ ---
const currentItemQtyInCart = ref(0);

const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

const triggerNotification = (message, isSuccess) => {
  if (notificationTimeout) {
    clearTimeout(notificationTimeout);
  }
  notificationMessage.value = message;
  notificationSuccess.value = isSuccess;
  showNotification.value = true;
  notificationTimeout = setTimeout(() => {
    showNotification.value = false;
  }, 1500);
};

const startCountdown = () => {
  if (countdown.value > 0) {
    setTimeout(() => {
      countdown.value--;
      startCountdown();
    }, 1000);
  }
};

const goToCart = () => {
  isAuthenticated.value ? router.push("/cart") : router.push("/signin");
};
const isSeller = computed(() => userRole.value === "SELLER");
const closeSuccessPopup = () => {
  showEditSuccessPopup.value = false;
  showEditFallPopup.value = false;
};

const decodeTokenAndSetRole = () => {
  try {
    const token = Cookies.get("access_token");
    if (token) {
      const decodedToken = jwtDecode(token);
      userRole.value = decodedToken.role;
      isAuthenticated.value = true;
    } else {
      isAuthenticated.value = false;
    }
  } catch (error) {
    console.error("Failed to decode JWT token:", error);
    userRole.value = null;
  }
};

const handleMainImageError = () => {
  mainImage.value = "/sy4/phone/iPhone.png"; // Fallback image
};

const getUserIdFromToken = () => {
  const token = Cookies.get("access_token");
  if (!token) return null;
  try {
    return jwtDecode(token).id;
  } catch (e) {
    return null;
  }
};

// --- ✨ 2. สร้างฟังก์ชันสำหรับอัปเดต State ที่เราสร้างขึ้น ---
const updateCurrentItemQtyInCart = () => {
  const userId = getUserIdFromToken();
  if (!userId) {
    currentItemQtyInCart.value = 0;
    return;
  }
  const currentId = parseInt(id);
  const cartKey = `CartData_${userId}`;
  const existingCart = JSON.parse(localStorage.getItem(cartKey)) || { items: [] };
  const existingItem = existingCart.items.find((i) => i.saleItemId === currentId);
  currentItemQtyInCart.value = existingItem ? existingItem.quantity : 0;
};

const loadCartCount = () => {
  const userId = getUserIdFromToken();
  if (!userId) {
    cartCount.value = 0;
    return;
  }
  const cartKey = `CartData_${userId}`;
  const cart = JSON.parse(localStorage.getItem(cartKey)) || { items: [] };
  cartCount.value = cart.items.reduce((sum, item) => sum + item.quantity, 0);
};

// --- ✨ 3. Computed Property ทั้งหมดจะอ่านค่าจาก State ที่เป็น Reactive ---
const isMaxQuantityReached = computed(() => {
  if (!product.value) return true;
  return currentItemQtyInCart.value >= product.value.quantity;
});

const handleStorageChange = (event) => {
  if (event.key.startsWith("CartData_")) {
    loadCartCount();
    updateCurrentItemQtyInCart(); // อัปเดตจำนวนของชิ้นนี้ด้วย
  }
};

const increaseQuantity = () => {
  if (!product.value) return;
  const maxCanAdd = product.value.quantity - currentItemQtyInCart.value;
  if (itemQuantityToAddToCart.value < maxCanAdd) {
    itemQuantityToAddToCart.value++;
  }
};
const decreaseQuantity = () => {
  if (itemQuantityToAddToCart.value > 1) {
    itemQuantityToAddToCart.value--;
  }
};
const isDecreaseDisabled = computed(
  () => itemQuantityToAddToCart.value <= 1 || isMaxQuantityReached.value
);
const isIncreaseDisabled = computed(
  () =>
    !product.value ||
    product.value.quantity <= 0 ||
    itemQuantityToAddToCart.value + currentItemQtyInCart.value >=
      product.value.quantity
);

const addToCart = (item) => {
  if (!isAuthenticated.value) {
    router.push("/signin");
    return;
  }
  if (isMaxQuantityReached.value) {
    triggerNotification("Item is out of stock or max quantity in cart.", false);
    return;
  }
  if (!item || item.quantity <= 0) {
    triggerNotification("This item cannot be added.", false);
    return;
  }

  const qtyToAdd = itemQuantityToAddToCart.value;
  const userId = getUserIdFromToken();
  if (!userId) {
    router.push("/signin");
    return;
  }

  const cartKey = `CartData_${userId}`;
  const existingCart = JSON.parse(localStorage.getItem(cartKey)) || { items: [] };
  const existingItem = existingCart.items.find((i) => i.saleItemId === item.id);

  if (existingItem) {
    const newTotalQty = existingItem.quantity + qtyToAdd;
    existingItem.quantity = Math.min(newTotalQty, item.quantity);
  } else {
    const safeQty = Math.min(qtyToAdd, item.quantity);
    existingCart.items.push({
      saleItemId: item.id,
      quantity: safeQty,
      description: item.description,
      model: item.model,
      price: item.price,
      maxquantity: item.quantity,
      sellerId: item.sellerId,
      sellernickname: item.sellerName,
      selected: false,
    });
  }

  localStorage.setItem(cartKey, JSON.stringify(existingCart));
  triggerNotification("Item added to cart!", true)

  // --- ✨ 4. สั่งให้อัปเดต State ทั้งหมดหลังจากเพิ่มของลงตะกร้า ---
  loadCartCount();
  updateCurrentItemQtyInCart(); // สำคัญมาก: อัปเดตจำนวนของชิ้นนี้เพื่อให้ปุ่มเปลี่ยนสถานะ
  itemQuantityToAddToCart.value = 1;
};

onMounted(async () => {
  decodeTokenAndSetRole();
  loadCartCount();

  try {
    const data = await getItemById(`${import.meta.env.VITE_BACKEND}/v2/sale-items`, id);
    if (!data || data?.status === 404) {
      showNotFoundPopup.value = true;
      startCountdown();
      setTimeout(() => { router.push("/sale-items"); }, 3000);
      return;
    }
    product.value = data;

    // --- ✨ 5. โหลดจำนวนสินค้าชิ้นนี้ในตะกร้าครั้งแรก ---
    updateCurrentItemQtyInCart(); 

    if (data.saleItemImages && data.saleItemImages.length > 0) {
      const sortedImages = data.saleItemImages
        .sort((a, b) => a.imageViewOrder - b.imageViewOrder)
        .map((img) => `${import.meta.env.VITE_BACKEND}/v2/sale-items/images/${img.fileName}`);
      productImages.value = sortedImages;
      mainImage.value = sortedImages[0];
    } else {
      mainImage.value = "/sy4/phone/iPhone.png";
    }
  } catch (error) {
    showNotFoundPopup.value = true;
    startCountdown();
    setTimeout(() => { router.push("/sale-items"); }, 3000);
  }
  window.addEventListener("storage", handleStorageChange);
});

onUnmounted(() => {
  window.removeEventListener("storage", handleStorageChange);
});

watch(
  () => route.query.editSuccess,
  (val) => {
    if (val === "true") {
      setTimeout(() => { showEditSuccessPopup.value = true; }, 200);
      router.replace({ query: {} });
    }
  }, { immediate: true }
);
watch(
  () => route.query.editFail,
  (val) => {
    if (val === "true") {
      setTimeout(() => { showEditFallPopup.value = true; }, 200);
      router.replace({ query: {} });
    }
  }, { immediate: true }
);

const deleteproduct = () => { showDeleteConfirmationPopup.value = true; };
const confirmDelete = async () => {
  showDeleteConfirmationPopup.value = false;
  isDeleting.value = true;
  try {
    const statusCode = await deleteItemById(`${import.meta.env.VITE_BACKEND}/v2/sale-items`, id);
    if (statusCode === 204) {
      setTimeout(() => {
        isDeleting.value = false;
        router.push({ path: "/sale-items", query: { deleteSuccess: "true" } });
      }, 1000);
    } else {
      isDeleting.value = false;
    }
  } catch (error) {
    isDeleting.value = false;
  }
};
const cancelDeleteItem = () => { showDeleteConfirmationPopup.value = false; };
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">

    <transition name="slide-down">
        <div v-if="showNotification" 
             class="fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 p-4 rounded-lg shadow-lg"
             :class="notificationSuccess ? 'bg-green-500 text-white' : 'bg-red-500 text-white'">
            <svg v-if="notificationSuccess" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span class="font-semibold">{{ notificationMessage }}</span>
        </div>
    </transition>
    
    <div class="container mx-auto px-6 py-8">
      <div class="flex justify-between items-center mb-8">
        <nav class="text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
            <router-link to="/sale-items" class="itbms-home-button hover:text-indigo-500 dark:hover:text-indigo-400 transition">
              All Phones
            </router-link>
            <span v-if="product" class="mx-2">/</span>
            <span v-if="product" class="itbms-row font-medium" :class="theme === 'dark' ? 'text-slate-200' : 'text-slate-800'">
              {{ product?.brandName || 'Brand' }}
            </span>
            <span v-if="product" class="mx-2">/</span>
            <span v-if="product" class="itbms-row font-medium" :class="theme === 'dark' ? 'text-slate-200' : 'text-slate-800'">
              {{ product?.model || 'Model' }}
            </span>
        </nav>
        <div class="relative itbms-cart-icon cursor-pointer p-2 rounded-full" :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'" @click="goToCart">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
            <span v-if="cartCount > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-bold">
                {{ cartCount > 99 ? "99+" : cartCount }}
            </span>
        </div>
      </div>

      <div v-if="product" class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
        <div>
            <div class="relative w-full aspect-square rounded-2xl overflow-hidden shadow-lg mb-4" :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-slate-100'">
                <img :src="mainImage" @error="handleMainImageError" :alt="product.model" class="w-full h-full object-contain transition-all duration-300" />
            </div>
            <div v-if="productImages.length > 1" class="flex space-x-3">
                <img v-for="(img, index) in productImages" :key="index" :src="img" :alt="`Thumbnail ${index + 1}`" @click="mainImage = img"
                    class="w-20 h-20 object-contain rounded-lg cursor-pointer ring-2 transition-all duration-200"
                    :class="mainImage === img ? 'ring-indigo-500' : (theme === 'dark' ? 'ring-transparent hover:ring-indigo-500/50' : 'ring-transparent hover:ring-indigo-500/50 bg-slate-100')" />
            </div>
        </div>

        <div class="itbms-row flex flex-col">
            <div class="flex-grow">
                <p class="font-bold text-indigo-500 dark:text-indigo-400 mb-2">{{ product.brandName }}</p>
                <h1 class="text-3xl lg:text-4xl font-extrabold tracking-tight mb-4 text-slate-900 dark:text-white">{{ product.model }}</h1>
                <div class="text-4xl font-extrabold mb-6" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">{{ product.price.toLocaleString() }} <span class="text-2xl font-semibold">฿</span></div>
                <p class="text-base mb-8" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-600'">{{ product.description }}</p>
                <div class="grid grid-cols-2 gap-6 py-6 border-y" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                    <div class="flex items-center gap-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 12h14M12 5l7 7-7 7" /></svg>
                        <div><strong class="font-semibold block text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">Storage</strong><span class="font-semibold">{{ product.storageGb || "-" }} GB</span></div>
                    </div>
                    <div class="flex items-center gap-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                        <div><strong class="font-semibold block text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">RAM</strong><span class="font-semibold">{{ product.ramGb || "-" }} GB</span></div>
                    </div>
                    <div class="flex items-center gap-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
                        <div><strong class="font-semibold block text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">Screen</strong><span class="font-semibold">{{ product.screenSizeInch || "-" }} "</span></div>
                    </div>
                    <div class="flex items-center gap-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" :class="product.quantity > 0 ? 'text-green-500' : 'text-red-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" /></svg>
                        <div><strong class="font-semibold block text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">Stock</strong><span class="font-semibold" :class="{ 'text-green-500': product.quantity > 0, 'text-red-500': product.quantity <= 0 }">{{ product.quantity > 0 ? `${product.quantity} Available` : "Out of Stock" }}</span></div>
                    </div>
                </div>
            </div>
            <div class="mt-auto pt-8">
                <div v-if="isSeller" class="flex gap-4">
                    <button @click="router.push(`/sale-items/${product.id}/edit`)" class="itbms-edit-button w-full flex items-center justify-center gap-2 font-semibold bg-yellow-500/10 text-yellow-600 dark:text-yellow-400 rounded-full px-6 py-4 transition-colors duration-300 hover:bg-yellow-500/20">Edit</button>
                    <button @click="deleteproduct" class="itbms-delete-button w-full flex items-center justify-center gap-2 font-semibold bg-red-500/10 text-red-600 dark:text-red-400 rounded-full px-6 py-4 transition-colors duration-300 hover:bg-red-500/20">Delete</button>
                </div>
                <div v-else class="flex flex-col sm:flex-row gap-4 items-center">
                    <div v-if="!isMaxQuantityReached && product.quantity > 0" class="flex items-center justify-center space-x-4">
                        <button @click="decreaseQuantity" :disabled="isDecreaseDisabled" class="p-2 border rounded-full w-12 h-12 flex items-center justify-center transition-colors disabled:opacity-50 disabled:cursor-not-allowed" :class="theme === 'dark' ? 'border-gray-700 hover:bg-gray-700' : 'border-gray-300 hover:bg-gray-100'"><span class="text-2xl leading-none">-</span></button>
                        <span class="text-xl font-semibold w-10 text-center">{{ itemQuantityToAddToCart }}</span>
                        <button @click="increaseQuantity" :disabled="isIncreaseDisabled" class="p-2 border rounded-full w-12 h-12 flex items-center justify-center transition-colors disabled:opacity-50 disabled:cursor-not-allowed" :class="theme === 'dark' ? 'border-gray-700 hover:bg-gray-700' : 'border-gray-300 hover:bg-gray-100'"><span class="text-xl leading-none">+</span></button>
                    </div>
                    <button @click="addToCart(product)" :disabled="isMaxQuantityReached || product.quantity <= 0 || itemQuantityToAddToCart <= 0" class="flex-grow w-full py-4 px-6 rounded-full font-bold text-lg transition-all duration-300 transform hover:-translate-y-1 disabled:bg-slate-300 disabled:text-slate-500 disabled:cursor-not-allowed disabled:transform-none dark:disabled:bg-gray-600 dark:disabled:text-gray-400" :class="'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20 hover:bg-indigo-700'">
                        <span v-if="product.quantity <= 0">Out of Stock</span>
                        <span v-else-if="isMaxQuantityReached">Max in Cart</span>
                        <span v-else>Add to Cart</span>
                    </button>
                </div>
            </div>
        </div>
      </div>
      
      <div v-else-if="!showNotFoundPopup" class="text-center py-20 text-slate-500"><p>Loading product details...</p></div>
    </div>
    
    <transition name="bounce-popup">
            <div v-if="showDeleteConfirmationPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm Deletion</h2>
                    <p class="itbms-message mb-6 text-lg">Are you sure you want to delete this sale item?</p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmDelete"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
                        <button @click="cancelDeleteItem"
                            class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">No</button>
                    </div>
                </div>
            </div>
        </transition>

        <transition name="bounce-popup">
            <div v-if="showNotFoundPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center max-w-sm w-full transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">⚠️ Item not found.</h2>
                    <p class="itbms-message mb-4 text-lg">The requested sale item does not exist.</p>
                    <p class="text-sm transition-colors duration-500"
                        :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                        Redirecting in {{ countdown }} second<span v-if="countdown > 1">s</span>...
                    </p>
                </div>
            </div>
        </transition>

        <transition name="fade-background">
            <div v-if="isDeleting"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
                <div class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                        fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
                    </svg>
                    <p class="itbms-message text-lg font-medium">Deleting product...</p>
                </div>
            </div>
        </transition>

        <transition name="bounce-popup">
            <div v-if="showEditSuccessPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4 text-green-500">Success!</h2>
                    <p class="itbms-message mb-6 text-lg">The sale item has been successfully updated!</p>
                    <button @click="closeSuccessPopup"
                        class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
                </div>
            </div>
        </transition>

        <transition name="bounce-popup">
            <div v-if="showEditFallPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4 text-red-500">The sale item has been fail to Edit!</h2>
                    <p class="itbms-message mb-6 text-lg">Please try again later.</p>
                    <button @click="closeSuccessPopup"
                        class="bg-blue-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-blue-600 active:scale-95 hover:cursor-pointer">Done</button>
                </div>
            </div>
        </transition>
  </div>
</template>

<style scoped>
/* ✨ CSS สำหรับ Slide Down Notification ✨ */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}
.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-150%);
  opacity: 0;
}

/* Scoped styles can be added here if needed */
.itbms-bg {
    background-color: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(4px);
}
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
@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}
.animate-spin {
    animation: spin 1s linear infinite;
}
</style>