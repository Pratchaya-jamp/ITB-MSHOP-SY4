<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'                                                                     
import {getItemsWithAuth, addItemWithAuth } from '@/libs/fetchUtilsOur';               
import Cookies from 'js-cookie'   


const cartItems = ref([])
const router = useRouter()
const totalCartCountKey = 'total_cart_count'

function saveCartToLocalStorage() {
    localStorage.setItem("CartData", JSON.stringify({ items: cartItems.value }));
    
    const newCartCount = cartItems.value.reduce((sum, item) => sum + item.quantity, 0);
    localStorage.setItem(totalCartCountKey, newCartCount.toString());
    console.log("🛒 Cart and Total Count updated.");
}

function loadCartFromLocalStorage() {
  const savedCart = localStorage.getItem("CartData")
  if (savedCart) {
    try {
      const parsed = JSON.parse(savedCart)
      cartItems.value = (parsed.items || []).map(item => ({
        ...item,
        sellernickname: item.sellernickname || `Seller ID ${item.sellerId}`, 
        selected: item.selected ?? false
      }))
      console.log("Loaded cart from localStorage:", cartItems.value)
      const newCartCount = cartItems.value.reduce((sum, item) => sum + item.quantity, 0);
      localStorage.setItem(totalCartCountKey, newCartCount.toString());
    } catch (err) {
      console.error("Invalid JSON in localStorage:", err)
    }
  } else {
    console.log("No cart data found in localStorage.")
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
    const itemsToDeleteCount = selectedItems.value.length;

    if (itemsToDeleteCount === 0) {
        alert('Please select at least one item to delete.');
        return;
    }

    cartItems.value = cartItems.value.filter(item => !item.selected);
    
    saveCartToLocalStorage();
    console.log(`${itemsToDeleteCount} item(s) deleted from cart.`);
}

const deleteItemFromCart = (saleItemId) => {
    cartItems.value = cartItems.value.filter(item => item.saleItemId !== saleItemId);
    
    saveCartToLocalStorage();
    console.log(`Item ${saleItemId} deleted from cart without confirmation.`);
}

const shippingAddress = ref('')
const orderNote = ref('')

const showConfirmationOrder = ref(false)
const isLoading = ref(false)

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
        ? 'bg-gray-900'
        : 'bg-gray-100'
})

const cardClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-800 shadow-xl border border-gray-700'
        : 'bg-white shadow-xl border border-gray-300'
})

const inputClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-700 border border-gray-600 text-white placeholder-gray-400'
        : 'bg-gray-50 border border-gray-300 text-gray-900 placeholder-gray-500'
})

const qtyButtonClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-700 hover:bg-gray-600 border-gray-600 text-white'
        : 'bg-gray-200 hover:bg-gray-300 border-gray-300 text-gray-800'
})


const selectedItems = computed(() => cartItems.value.filter(item => item.selected))

const totalSelectedPrice = computed(() => {
    return selectedItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

const totalSelectedItems = computed(() => {
    return selectedItems.value.reduce((total, item) => total + item.quantity, 0)
})

const allSelected = computed(() => {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected)
})

const formatPrice = (price) => {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const toggleSelectAll = () => {
    const shouldSelectAll = !allSelected.value
    cartItems.value.forEach(item => item.selected = shouldSelectAll)
}

const toggleSelectSeller = (sellerId) => {
    const itemsOfSeller = cartItems.value.filter(item => item.sellerId === sellerId);
    const shouldSelect = !itemsOfSeller.every(item => item.selected);
    itemsOfSeller.forEach(item => {
        item.selected = shouldSelect;
    });
}

// เพิ่ม/ลดจำนวนสินค้า
const updateQuantity = (saleItemId, delta) => {
  const item = cartItems.value.find(i => i.saleItemId === saleItemId)
  if (item) {
    const newQuantity = item.quantity + delta

   if (newQuantity < 1) {
        item.quantity = 1
    } 
    else if (item.maxquantity && newQuantity > item.maxquantity) {
        item.quantity = item.maxquantity
    } else {
        item.quantity = newQuantity
    }
    saveCartToLocalStorage();
  }
}

const canFillAddress = computed(() => {
    return totalSelectedItems.value > 0
})

const canPlaceOrder = computed(() => {
    const isAddressEmpty = shippingAddress.value.trim() === ''
    return totalSelectedItems.value > 0 && !isAddressEmpty
})

const openConfirmOrder = () => {
    if (!canPlaceOrder.value) {
        return
    }
    
    showConfirmationOrder.value = true
}

const cancelPlaceOrder = () => {
    showConfirmationOrder.value = false
}

const confirmPlaceOrder = async () => {
    showConfirmationOrder.value = false
    await addorder()
}

// async function fetchitemcart() {
//   const token = Cookies.get('access_token');
//   let decodedToken = null;
//   let userId = null;

//   if (token) {
//     try {
//       decodedToken = jwtDecode(token);
//       userId = decodedToken.id;
//     } catch (err) {
//       console.error("Failed to decode token:", err);
//     }
//   }

//   try {
// const response = await getItemsWithAuth(
//   `https://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/carts/${userId}/user`,
//   {
//     token: token,
//     params: null, // ถ้าไม่มี query params
//   }
// );
//     // ตรวจสอบ response ก่อนใช้
//     if (!response || (response.status && (response.status === 401 || response.status === 403))) {
//       console.error('Authentication error: Unauthorized or Forbidden');
//       router.push('/login');
//       return;
//     }

//     const data = response.data ?? response; // รองรับทั้ง axios หรือ json ตรงๆ
//     data.userType = decodedToken?.role === 'SELLER' ? 'Seller' : 'Buyer';

//     cartItems.value = data.carts ?? []; // ถ้ามี cartItems
//   } catch (error) {
//      console.error('Error fetching user profile:', error);
//   }
// 



onMounted(() => {
    applyTheme(theme.value);
    loadCartFromLocalStorage()
});

const addorder = async () => { 
    isLoading.value = true

  const token = Cookies.get('access_token');
  if (!token) {
    alert('Please login before placing an order.');
    isLoading.value = false
    return;
  }

  let decodedToken = null;
  let buyerId = null;

  try {
    decodedToken = jwtDecode(token);
    buyerId = decodedToken.id;
  } catch (err) {
    console.error('Failed to decode token:', err);
    alert('Invalid token. Please login again.');
    isLoading.value = false
    return;
  }

  const selected = cartItems.value.filter(item => item.selected);
  if (selected.length === 0) {
    alert('Please select at least one item to order.');
    isLoading.value = false
    return;
  }

  if (shippingAddress.value.trim() === '') {
        alert('Shipping address cannot be empty.');
        isLoading.value = false
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
      'https://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/orders',
      orderPayload, 
      false,
      token
    );

    if (response.status === 201 || response.status === 200) {
      cartItems.value = cartItems.value.filter(item => !item.selected);
      saveCartToLocalStorage();
      setTimeout(() => {
            isLoading.value = false;
            router.push({ path: '/sale-items', query: { orderSuccess: 'true' } });
        }, 1000);
    } else {
      console.error('❌ Failed to place order:', response.status, response.data);
    }
  } catch (error) {
    isLoading.value = false;
    console.error('Error placing order:', error);
  }
};

</script>

<template>
    <div :class="themeClass" class="p-4 w-full min-h-screen font-sans transition-colors duration-500">
        <div 
            :class="[
                'px-6 md:px-20 py-4 flex justify-between items-center transition-all duration-300 border-b',
                theme === 'dark' ? 'bg-gray-950/80 border-gray-700' : 'bg-white/80 border-gray-200'
            ]"
        >
            <div class="text-2xl font-extrabold" :class="theme === 'dark' ? 'text-white' : 'text-gray-950'">
            </div>
        </div>
        
        <div :class="contentBgClass" class="px-6 md:px-20 py-12">
            <h1 class="text-4xl font-extrabold mb-10">Shopping Cart</h1>
            
            <div class="flex flex-col lg:flex-row gap-10">
                
                <div class="lg:w-2/3 space-y-6">
                    
                    <div v-if="cartItems.length > 0" :class="cardClass" class="p-6 rounded-3xl space-y-6">
                        
                        <div class="flex items-center justify-between pb-4 border-b" 
                             :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            
                            <div class="flex items-center space-x-3">
                                <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" class="form-checkbox h-5 w-5 rounded itbms-select-all" 
                                    :class="{'text-orange-500 border-gray-400 focus:ring-orange-500': theme === 'light', 'text-orange-400 bg-gray-700 border-gray-600 focus:ring-orange-400': theme === 'dark'}" />
                                <span class="font-semibold text-lg">Select All</span>
                                <span class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">( {{ cartItems.length }} Total Items )</span>
                            </div>

                            <button @click="deleteSelectedItems" :disabled="totalSelectedItems === 0"
                                class="px-4 py-1 flex items-center rounded-full text-sm font-semibold transition-colors duration-200 itbms-delete-selected-button"
                                :class="totalSelectedItems === 0 
                                    ? 'bg-gray-400 text-gray-700 cursor-not-allowed'
                                    : 'bg-red-500 hover:bg-red-600 text-white'">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
                                Delete Selected
                            </button>
                        </div>

                        <div v-for="group in groupedCartItems" :key="group.sellerId" class="pt-4 first:pt-0">
                            
                            <div class="flex items-center space-x-3 mb-4">
                                
                                <input type="checkbox" 
                                    :checked="group.sellerAllSelected" 
                                    @change="toggleSelectSeller(group.sellerId)" 
                                    class="form-checkbox h-5 w-5 rounded itbms-select-seller-all" 
                                    :class="{'text-orange-500 border-gray-400 focus:ring-orange-500': theme === 'light', 'text-orange-400 bg-gray-700 border-gray-600 focus:ring-orange-400': theme === 'dark'}" />
                                
                                <div class="flex items-center text-lg itbms-select-nickname">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-orange-500" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" /></svg>
                                    <span class="font-bold itbms-nickname">{{ group.sellernickname }}</span>
                                    <span class="text-sm ml-3" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">( {{ group.items.length }} Items )</span>
                                </div>
                            </div>

                            <div class="divide-y" :class="theme === 'dark' ? 'divide-gray-700' : 'divide-gray-200'">
                                <div v-for="item in group.items" :key="item.saleItemId" class="flex items-start py-6 itbms-item-row">
                                    <div class="flex items-center space-x-4">
                                        <input type="checkbox" v-model="item.selected" class="form-checkbox h-5 w-5 rounded itbms-select-item" 
                                            :class="{'text-orange-500 border-gray-400 focus:ring-orange-500': theme === 'light', 'text-orange-400 bg-gray-700 border-gray-600 focus:ring-orange-400': theme === 'dark'}" />
                                        <img src="/phone/iPhone.png" :alt="item.description" class="w-20 h-20 object-contain rounded-lg itbms-item-image" />
                                    </div>
                                    <div class="flex-grow ml-4">
                                        <p class="text-lg font-bold itbms-item-name">{{ item.description }}</p>
                                        <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400 itbms-item-description' : 'text-gray-600 itbms-item-description'">{{ item.description }}</p>
                                    </div>
                                    
                                    <div class="flex items-center justify-center space-x-1 mr-6 mt-2 lg:mt-0 flex-shrink-0 itbms-item-quantity">
                                        <button @click="updateQuantity(item.saleItemId, -1)" 
                                                :disabled="item.quantity <= 1"
                                                class="p-2 border rounded-l-full itbms-dec-qty-button" 
                                                :class="[qtyButtonClass, {'opacity-50 cursor-not-allowed': item.quantity <= 1}]">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M20 12H4" /></svg>
                                        </button>
                                        
                                        <input type="text" v-model.number="item.quantity" class="w-12 text-center p-2 border-t border-b itbms-item-quantity-input" readonly
                                            :class="{'bg-transparent': theme === 'dark', 'bg-white': theme === 'light', 'border-gray-600': theme === 'dark', 'border-gray-300': theme === 'light'}" />
                                        
                                        <button @click="updateQuantity(item.saleItemId, 1)" 
                                                :disabled="item.maxquantity && item.quantity >= item.maxquantity"
                                                class="p-2 border rounded-r-full itbms-inc-qty-button" 
                                                :class="[qtyButtonClass, {'opacity-50 cursor-not-allowed': item.maxquantity && item.quantity >= item.maxquantity}]">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" /></svg>
                                        </button>
                                    </div>

                                    <div class="text-right flex-shrink-0 mt-2 lg:mt-0">
                                        <p class="text-xl font-extrabold text-orange-500 itbms-item-total-price">
                                            Price: {{ formatPrice(item.price * item.quantity) }} ฿
                                        </p>
                                    </div>

                                    <button @click="deleteItemFromCart(item.saleItemId)"
                                        class="ml-4 p-2 rounded-full flex-shrink-0 transition-colors duration-200 itbms-delete-item-button"
                                        :class="theme === 'dark' ? 'text-gray-400 hover:bg-gray-700 hover:text-red-500' : 'text-gray-600 hover:bg-gray-100 hover:text-red-600'">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div v-if="cartItems.length === 0" class="text-center py-10" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                        <p class="text-xl">Your cart is empty.</p>
                    </div>

                </div>

                <div class="lg:w-1/3 space-y-6">
                    <div :class="cardClass" class="p-6 rounded-3xl">
                        <h2 class="text-2xl font-bold mb-4">Cart Summary</h2>

                        <div class="mb-6 pb-4 border-b" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            <h3 class="font-semibold mb-2">Ship To</h3>
                            <textarea rows="3" :value="shippingAddress" @input="shippingAddress = $event.target.value" 
                                :disabled="!canFillAddress"
                                class="w-full p-3 rounded-xl text-sm resize-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all itbms-shipping-address"
                                :class="[inputClass, {'opacity-60 cursor-not-allowed': !canFillAddress}]" placeholder="Address No, Street, Subdistrict, District, Province, Postal Code"></textarea>
                        </div>
                        
                        <div class="mb-6 pb-4 border-b" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-200'">
                            <h3 class="font-semibold mb-2">Note</h3>
                            <textarea rows="3" v-model="orderNote" 
                                :disabled="!canFillAddress"
                                class="w-full p-3 rounded-xl text-sm resize-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all itbms-order-note"
                                :class="[inputClass, {'opacity-60 cursor-not-allowed': !canFillAddress}]" placeholder="Additional Instructions or requests"></textarea>
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
                            @click="openConfirmOrder"
                            :disabled="!canPlaceOrder"
                            class="w-full px-8 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-0.5 hover:scale-[1.01] disabled:from-gray-400 disabled:to-gray-500 disabled:shadow-none disabled:transform-none disabled:cursor-not-allowed itbms-place-order-button">
                            Place Order
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <transition name="bounce-popup">
            <div v-if="showConfirmationOrder"
                class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm Order Placement</h2>
                    <p class="itbms-message mb-6 text-lg">
                        Are you sure you want to place an order for {{ totalSelectedItems }} item(s)<br>
                    </p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmPlaceOrder"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Confirm Order</button>
                        <button @click="cancelPlaceOrder"
                            class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">Cancel</button>
                    </div>
                </div>
            </div>
        </transition>

        <transition name="fade-background">
            <div v-if="isLoading"
                class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
                <div class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                        fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
                    </svg>
                    <p class="itbms-message text-lg font-medium">Placing your order...</p>
                </div>
            </div>
        </transition>
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
    transition: background-color 0.3s ease;
}

.fade-background-enter-from {
    background-color: rgba(0, 0, 0, 0);
}

.fade-background-leave-to {
    background-color: rgba(0, 0, 0, 0);
}

.fade-in-out-enter-active,
.fade-in-out-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-in-out-enter-from {
    opacity: 0;
    transform: scale(0.95);
}

.fade-in-out-leave-to {
    opacity: 0;
    transform: scale(1.05);
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: transform 0.3s ease, opacity 0.3s ease;
}

.slide-up-enter-from {
    transform: translateY(20px);
    opacity: 0;
}

.slide-up-leave-to {
    transform: translateY(-20px);
    opacity: 0;
}

.fixed.bg-black {
    background-color: rgba(0, 0, 0, 0.5);
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.animate-spin {
    animation: spin 1s linear infinite;
}

span {
    white-space: normal;
    word-break: break-word;
}

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
/* Animations */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up { animation: fade-in-up 1s ease-out forwards; }
@keyframes blob {
  0% { transform: scale(1) translate(0px, 0px); }
  33% { transform: scale(1.1) translate(30px, -50px); }
  66% { transform: scale(0.9) translate(-20px, 20px); }
  100% { transform: scale(1) translate(0px, 0px); }
}
.animate-blob { animation: blob 7s infinite ease-in-out; }
.animation-delay-2000 { animation-delay: 2s; }
</style>
