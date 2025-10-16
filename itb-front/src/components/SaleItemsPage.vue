<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getItems,
  deleteItemById,
  getItemsWithAuth,
} from "@/libs/fetchUtilsOur";
import { DotLottieVue } from "@lottiefiles/dotlottie-vue";
import { jwtDecode } from "jwt-decode";
import Cookies from "js-cookie";
import { theme } from "@/stores/themeStore.js";

const router = useRouter();
const route = useRoute();

// --- State สำหรับ Slide Popup Notification ---
const showNotification = ref(false);
const notificationMessage = ref("");
const notificationSuccess = ref(false);
let notificationTimeout = null;

// --- State สำหรับ Sidebar ---
const isSidebarOpen = ref(false);
const openFilterSection = ref("brand");

const showAddSuccessPopup = ref(false);
const showDeleteSuccessPopup = ref(false);
const showfailPopup = ref(false);
const showRegisSuccess = ref(false);
const showOrderSuccess = ref(false);
const isGridView = computed(() => route.path !== "/sale-items/list");

const items = ref([]);
const totalPages = ref(0);
const brandList = ref([]);
const pageSize = ref(parseInt(sessionStorage.getItem("pageSize") || "10"));
const currentPage = ref(parseInt(sessionStorage.getItem("currentPage") || "0"));
const showLoginSuccess = ref(false);
const userRole = ref("");
const cartCount = ref(0);
const isAuthenticated = ref(false);

// --- ฟังก์ชันสำหรับแสดง Slide Popup ---
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

const displayedPrice = computed(() => {
  if (priceLower.value != null && priceUpper.value != null)
    return `${priceLower.value.toLocaleString()} – ${priceUpper.value.toLocaleString()} Baht`;
  if (priceLower.value != null)
    return `= ${priceLower.value.toLocaleString()} Baht`;
  if (priceUpper.value != null)
    return `<= ${priceUpper.value.toLocaleString()} Baht`;
  return "";
});
const priceLower = ref(
  JSON.parse(sessionStorage.getItem("priceLower") || "null")
);
const priceUpper = ref(
  JSON.parse(sessionStorage.getItem("priceUpper") || "null")
);
const priceRanges = [
  { min: 0, max: 5000, label: "0 - 5,000 Baht" },
  { min: 5001, max: 10000, label: "5,001 - 10,000 Baht" },
  { min: 10001, max: 20000, label: "10,001 - 20,000 Baht" },
  { min: 20001, max: 30000, label: "20,001 - 30,000 Baht" },
  { min: 30001, max: 40000, label: "30,001 - 40,000 Baht" },
  { min: 40001, max: 50000, label: "40,001 - 50,000 Baht" },
];

const storageRanges = [
  "32 GB", "64 GB", "128 GB", "256 GB",
  "512 GB", "1 TB", "Not specified",
];
const selectedStorages = ref(
  JSON.parse(sessionStorage.getItem("selectedStorages") || "[]")
);
const searchQuery = ref(sessionStorage.getItem("searchQuery") || "");
const selectedBrands = ref(
  JSON.parse(sessionStorage.getItem("selectedBrands") || "[]")
);

const debounce = (func, delay) => {
  let timeoutId;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      func.apply(this, args);
    }, delay);
  };
};

const debouncedFetchItems = debounce(() => {
  currentPage.value = 0;
  fetchItems();
}, 500);

const getCartQuantityForItem = (itemId) => {
  const token = Cookies.get("access_token");
  if (!token) return 0;
  let userId = null;
  try {
    userId = jwtDecode(token).id;
  } catch (e) {
    return 0;
  }
  const targetId = parseInt(itemId);
  const cartKey = `CartData_${userId}`;
  const existingCart = JSON.parse(localStorage.getItem(cartKey)) || {
    items: [],
  };
  const existingItem = existingCart.items.find(
    (i) => i.saleItemId === targetId
  );
  return existingItem ? existingItem.quantity : 0;
};

const isItemSoldOut = (item) => {
  if (!item || item.quantity <= 0) return true;
  const qtyInCart = getCartQuantityForItem(item.id);
  return qtyInCart >= item.quantity;
};

const addToCart = (item, qtyToAdd = 1) => {
  if (!isAuthenticated.value) {
    router.push("/signin");
    return;
  }
  if (isItemSoldOut(item)) {
    triggerNotification("Item is out of stock or max quantity in cart.", false);
    return;
  }
  if (!item || item.quantity <= 0) {
    triggerNotification("This item cannot be added.", false);
    return;
  }
  const token = Cookies.get("access_token");
  if (!token) {
    router.push("/signin");
    return;
  }
  let userId = null;
  try {
    userId = jwtDecode(token).id;
  } catch (err) {
    console.error("Failed to decode token:", err);
    return;
  }
  if (!userId) return;
  const cartKey = `CartData_${userId}`;
  const existingCart = JSON.parse(localStorage.getItem(cartKey)) || {
    items: [],
  };
  const itemId = parseInt(item.id);
  const existingItem = existingCart.items.find((i) => i.saleItemId === itemId);
  if (existingItem) {
    const newTotalQty = existingItem.quantity + qtyToAdd;
    existingItem.quantity =
      newTotalQty > item.quantity ? item.quantity : newTotalQty;
  } else {
    const safeQty = qtyToAdd > item.quantity ? item.quantity : qtyToAdd;
    existingCart.items.push({
      saleItemId: itemId,
      quantity: safeQty,
      description: `${item.brandName} ${item.model} (${item.storageGb ? item.storageGb + "GB" : "-"
        }, ${item.color || "-"})`,
      model: item.model,
      price: item.price,
      maxquantity: item.quantity,
      sellerId: item.sellerId,
      sellernickname: item.sellerName,
      selected: false,
    });
  }
  localStorage.setItem(cartKey, JSON.stringify(existingCart));
  triggerNotification("Item added to cart!", true);
  loadCartCount();
  fetchItems();
};

const loadCartCount = () => {
  const token = Cookies.get("access_token");
  if (!token) {
    cartCount.value = 0;
    return;
  }
  try {
    const userId = jwtDecode(token).id;
    const cartKey = `CartData_${userId}`;
    const cart = JSON.parse(localStorage.getItem(cartKey)) || { items: [] };
    cartCount.value = cart.items.reduce((sum, item) => sum + item.quantity, 0);
  } catch (e) {
    cartCount.value = 0;
  }
};

const handleStorageChange = (event) => {
  if (event.key.startsWith("CartData_")) {
    loadCartCount();
    fetchItems();
  }
};

const savedSort = sessionStorage.getItem("sortOrder");
const currentSortOrder = ref(savedSort || "createdOn");
const availableBrands = computed(() =>
  brandList.value.map((b) => b.brandName).filter((name) => !!name)
);

const themeClass = computed(() => {
  return theme.value === "dark"
    ? "bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

async function fetchItems() {
  const token = Cookies.get("access_token");
  let userId = null;
  let userRole = null;
  if (token) {
    try {
      const decodedToken = jwtDecode(token);
      userId = decodedToken.seller_id;
      userRole = decodedToken.role;
    } catch (err) {
      console.error("Failed to decode token:", err);
    }
  }
  try {
    let lower = priceLower.value != null ? priceLower.value : undefined;
    let upper = priceUpper.value != null ? priceUpper.value : undefined;
    let storagesToSend = selectedStorages.value.map((s) => {
      if (s === "Not specified") return null;
      if (s === "1 TB") return 1024;
      return parseInt(s);
    });
    const params = {
      filterBrands: selectedBrands.value.length
        ? selectedBrands.value
        : undefined,
      filterStorages: storagesToSend.length ? storagesToSend : undefined,
      filterPriceLower: lower,
      filterPriceUpper: upper,
      page: currentPage.value,
      size: pageSize.value,
      searchKeyword: searchQuery.value || undefined,
      sortField: currentSortOrder.value === "createdOn" ? "id" : "brand.name",
      sortDirection:
        currentSortOrder.value === "brandAsc"
          ? "asc"
          : currentSortOrder.value === "brandDesc"
            ? "desc"
            : "asc",
    };
    const response =
      token && userRole === "SELLER"
        ? await getItemsWithAuth(
          `${import.meta.env.VITE_BACKEND}/v2/sellers/${userId}/sale-items`,
          { params, token }
        )
        : await getItems(
          `${import.meta.env.VITE_BACKEND}/v2/sale-items/page-sale-items`,
          { params }
        );
    items.value = response.content;
    totalPages.value = response.totalPages;
  } catch (err) {
    console.error("Fetch error:", err);
  }
}

async function fetchbrand() {
  try {
    const data = await getItems(`${import.meta.env.VITE_BACKEND}/v1/brands`);
    brandList.value = data.sort((a, b) =>
      a.brandName.toLowerCase().localeCompare(b.brandName.toLowerCase())
    );
  } catch (err) {
    console.error("Error loading brands:", err);
  }
}

watch(currentPage, (newPage) => {
  sessionStorage.setItem("currentPage", newPage);
  fetchItems();
});
watch(pageSize, (newSize) => {
  sessionStorage.setItem("pageSize", newSize);
  currentPage.value = 0;
  fetchItems();
});
watch(selectedBrands, (newVal) => {
  sessionStorage.setItem("selectedBrands", JSON.stringify(newVal));
  currentPage.value = 0;
  fetchItems();
});
watch(currentSortOrder, (val) => {
  sessionStorage.setItem("sortOrder", val);
  currentPage.value = 0;
  fetchItems();
});
watch(priceLower, (val) =>
  sessionStorage.setItem("priceLower", JSON.stringify(val))
);
watch(priceUpper, (val) =>
  sessionStorage.setItem("priceUpper", JSON.stringify(val))
);
watch(selectedStorages, (val) => {
  sessionStorage.setItem("selectedStorages", JSON.stringify(val));
  currentPage.value = 0;
  fetchItems();
});
watch(searchQuery, (val) => {
  sessionStorage.setItem("searchQuery", val);
  debouncedFetchItems();
});

const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = currentPage.value;
  const groupSize = 5;
  if (total <= groupSize) return Array.from({ length: total }, (_, i) => i + 1);
  let start = Math.max(current + 1 - Math.floor(groupSize / 2), 1);
  let end = start + groupSize - 1;
  if (end > total) {
    end = total;
    start = end - groupSize + 1;
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

function goToPage(page) {
  currentPage.value = page;
}
const goToEditItem = (id) => router.push(`/sale-items/${id}/edit`);
const goToCart = () =>
  isAuthenticated.value ? router.push("/cart") : router.push("/signin");
function sortBrandAscending() {
  currentSortOrder.value = "brandAsc";
}
function sortBrandDescending() {
  currentSortOrder.value = "brandDesc";
}
function clearBrandSorting() {
  currentSortOrder.value = "createdOn";
}
function selectPriceRange(range) {
  priceLower.value = range.min;
  priceUpper.value = range.max;
  currentPage.value = 0;
  fetchItems();
}
function savePriceRange() {
  currentPage.value = 0;
  fetchItems();
}
function clearPriceFilter() {
  priceLower.value = null;
  priceUpper.value = null;
  currentPage.value = 0;
  fetchItems();
}
function removeBrandFromFilter(brand) {
  selectedBrands.value = selectedBrands.value.filter((b) => b !== brand);
}
function removeStorageFromFilter(storage) {
  selectedStorages.value = selectedStorages.value.filter((s) => s !== storage);
}
function clearAllFilters() {
  selectedBrands.value = [];
  selectedStorages.value = [];
  priceLower.value = null;
  priceUpper.value = null;
  sessionStorage.removeItem("selectedBrands");
  sessionStorage.removeItem("selectedStorages");
  sessionStorage.removeItem("priceLower");
  sessionStorage.removeItem("priceUpper");
  currentPage.value = 0;
  fetchItems();
}
function addSaleItemButton() {
  router.push("/sale-items/add");
}
const goToPhoneDetails = (id) => router.push(`/sale-items/${id}`);
const checkUserRole = () => {
  const token = Cookies.get("access_token");
  if (token) {
    try {
      userRole.value = jwtDecode(token).role;
    } catch (error) {
      userRole.value = "";
    }
  } else {
    userRole.value = "";
  }
};
onMounted(() => {
  fetchItems();
  fetchbrand();
  checkUserRole();
  loadCartCount();
  decodeTokenAndSetRole();
  window.addEventListener("storage", handleStorageChange);
});
onUnmounted(() => {
  window.removeEventListener("storage", handleStorageChange);
});
const showDeleteConfirmationPopup = ref(false);
const isDeleting = ref(false);
const deleteSale = ref(null);
const deleteproduct = (item) => {
  deleteSale.value = item.id;
  showDeleteConfirmationPopup.value = true;
};
const confirmDelete = async () => {
  showDeleteConfirmationPopup.value = false;
  isDeleting.value = true;
  try {
    const statusCode = await deleteItemById(
      `${import.meta.env.VITE_BACKEND}/v2/sale-items`,
      deleteSale.value
    );
    if (statusCode === 204) {
      setTimeout(() => {
        isDeleting.value = false;
        router.push({ path: route.path, query: { deleteSuccess: "true" } });
        fetchItems();
      }, 1000);
    } else {
      isDeleting.value = false;
    }
  } catch (error) {
    isDeleting.value = false;
  }
};
const cancelDeleteItem = () => {
  showDeleteConfirmationPopup.value = false;
};
const closeSuccessPopup = async () => {
  showAddSuccessPopup.value = false;
  showDeleteSuccessPopup.value = false;
  showfailPopup.value = false;
  showRegisSuccess.value = false;
  showLoginSuccess.value = false;
  showOrderSuccess.value = false;
  router.replace({ path: route.path, query: {} });
  window.location.reload();
  await fetchItems();
};
watch(
  () => route.query.addSuccess,
  (val) => {
    if (val === "true") {
      setTimeout(() => {
        showAddSuccessPopup.value = true;
      }, 200);
      router.replace({ query: {} });
    }
  },
  { immediate: true }
);
watch(
  () => route.query.deleteSuccess,
  (val) => {
    if (val === "true") {
      setTimeout(() => {
        showDeleteSuccessPopup.value = true;
      }, 200);
      router.replace({ query: {} });
    }
  },
  { immediate: true }
);
watch(
  () => route.query.addFail,
  (val) => {
    if (val === "true") {
      setTimeout(() => {
        showfailPopup.value = true;
      }, 200);
      router.replace({ query: {} });
    }
  },
  { immediate: true }
);
watch(
  () => route.query.regisSuccess,
  (val) => {
    if (val === "true") {
      setTimeout(() => {
        showRegisSuccess.value = true;
      }, 200);
      router.replace({ query: {} });
    }
  },
  { immediate: true }
);
watch(
  () => route.query.loginSuccess,
  (val) => {
    if (val === "true") {
      setTimeout(() => {
        showLoginSuccess.value = true;
      }, 200);
      router.replace({ query: {} });
    }
  },
  { immediate: true }
);
watch(
  () => route.query.orderSuccess,
  (val) => {
    if (val === "true") {
      setTimeout(() => {
        showOrderSuccess.value = true;
      }, 200);
      router.replace({ query: {} });
    }
  },
  { immediate: true }
);

const activeFilters = computed(() => {
  const filters = [];
  if (selectedBrands.value.length > 0)
    selectedBrands.value.forEach((brand) =>
      filters.push({ type: "brand", value: brand })
    );
  if (priceLower.value != null || priceUpper.value != null)
    filters.push({ type: "price", value: displayedPrice.value });
  if (selectedStorages.value.length > 0)
    selectedStorages.value.forEach((storage) =>
      filters.push({ type: "storage", value: storage })
    );
  return filters;
});
const removeActiveFilter = (filter) => {
  if (filter.type === "brand") removeBrandFromFilter(filter.value);
  else if (filter.type === "price") clearPriceFilter();
  else if (filter.type === "storage") removeStorageFromFilter(filter.value);
};
</script>


<template>
  <div :class="themeClass" class="itbms-sale-items-page min-h-screen font-sans">

    <transition name="slide-down">
      <div v-if="showNotification"
        class="fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 p-4 rounded-lg shadow-lg"
        :class="notificationSuccess ? 'bg-green-500 text-white' : 'bg-red-500 text-white'">
        <svg v-if="notificationSuccess" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none"
          viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
          stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
            d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span class="font-semibold">{{ notificationMessage }}</span>
      </div>
    </transition>

    <div v-if="isSidebarOpen" @click="isSidebarOpen = false" class="fixed inset-0 bg-black/40 z-30 transition-opacity">
    </div>

    <aside class="fixed top-0 left-0 h-screen w-72 p-6 z-40 transition-transform duration-300 ease-in-out border-r"
      :class="[
        isSidebarOpen ? 'translate-x-0' : '-translate-x-full',
        theme === 'dark' ? 'bg-gray-900 border-gray-800' : 'bg-white border-slate-200'
      ]">
      <div class="flex justify-between items-center mb-8">
        <h2 class="text-xl font-bold tracking-wider cursor-pointer" @click="() => router.push('/')">ITB MSHOP</h2>
        <button @click="isSidebarOpen = false" class="p-2 rounded-full"
          :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"
            stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="space-y-4 h-[calc(100%-4rem)] flex flex-col">
        <div class="flex-grow space-y-4 overflow-y-auto pr-2 -mr-4">
          <div>
            <h3 class="font-semibold mb-3 px-2">Sort By</h3>
            <div class="space-y-1 text-sm">
              <button @click="clearBrandSorting" class="w-full text-left p-2 rounded-lg flex items-center gap-2"
                :class="currentSortOrder === 'createdOn' ? 'bg-indigo-500/10 text-indigo-500 dark:text-indigo-400 font-semibold' : (theme === 'dark' ? 'hover:bg-white/5' : 'hover:bg-black/5')">Default
                (Newest)</button>
              <button @click="sortBrandAscending" class="w-full text-left p-2 rounded-lg"
                :class="currentSortOrder === 'brandAsc' ? 'bg-indigo-500/10 text-indigo-500 dark:text-indigo-400 font-semibold' : (theme === 'dark' ? 'hover:bg-white/5' : 'hover:bg-black/5')">Brand
                (A-Z)</button>
              <button @click="sortBrandDescending" class="w-full text-left p-2 rounded-lg"
                :class="currentSortOrder === 'brandDesc' ? 'bg-indigo-500/10 text-indigo-500 dark:text-indigo-400 font-semibold' : (theme === 'dark' ? 'hover:bg-white/5' : 'hover:bg-black/5')">Brand
                (Z-A)</button>
            </div>
          </div>

          <div class="border-t pt-4" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
            <h3 class="font-semibold mb-2 px-2">Filters</h3>
            <div>
              <button @click="openFilterSection = openFilterSection === 'brand' ? null : 'brand'"
                class="w-full flex justify-between items-center p-2 font-medium rounded-lg"
                :class="theme === 'dark' ? 'hover:bg-white/5' : 'hover:bg-black/5'">
                <span>Brand</span>
                <svg class="w-5 h-5 transition-transform" :class="{ 'rotate-180': openFilterSection === 'brand' }"
                  fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              <div v-if="openFilterSection === 'brand'" class="mt-2 pl-2 pr-1 space-y-1 max-h-40 overflow-y-auto">
                <label v-for="brand in availableBrands" :key="brand"
                  class="flex items-center p-2 rounded-lg cursor-pointer"
                  :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'">
                  <input type="checkbox" :value="brand" v-model="selectedBrands"
                    class="form-checkbox h-4 w-4 rounded-sm" />
                  <span class="ml-3 text-sm">{{ brand }}</span>
                </label>
              </div>
            </div>
            <div>
              <button @click="openFilterSection = openFilterSection === 'price' ? null : 'price'"
                class="w-full flex justify-between items-center p-2 font-medium rounded-lg"
                :class="theme === 'dark' ? 'hover:bg-white/5' : 'hover:bg-black/5'">
                <span>Price</span>
                <svg class="w-5 h-5 transition-transform" :class="{ 'rotate-180': openFilterSection === 'price' }"
                  fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              <div v-if="openFilterSection === 'price'" class="mt-2 px-2 space-y-2">
                <div class="space-y-1">
                  <label v-for="range in priceRanges" :key="range.label"
                    class="flex items-center p-2 rounded-lg cursor-pointer text-sm"
                    :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'">
                    <input type="radio" :name="'price-range'" @change="selectPriceRange(range)"
                      :checked="priceLower === range.min && priceUpper === range.max" class="form-radio h-4 w-4" />
                    <span class="ml-3">{{ range.label }}</span>
                  </label>
                </div>
                <div class="flex items-center gap-2 pt-2">
                  <input type="number" v-model.number="priceLower" placeholder="Min"
                    class="itbms-price-item-min w-1/2 p-2 text-sm rounded-md border"
                    :class="theme === 'dark' ? 'bg-gray-700 text-white border-gray-600' : 'bg-slate-100 text-gray-950 border-gray-300'">
                  <span>-</span>
                  <input type="number" v-model.number="priceUpper" placeholder="Max"
                    class="itbms-price-item-max w-1/2 p-2 text-sm rounded-md border"
                    :class="theme === 'dark' ? 'bg-gray-700 text-white border-gray-600' : 'bg-slate-100 text-gray-950 border-gray-300'">
                </div>
                <button @click="savePriceRange"
                  class="mt-2 w-full px-4 py-2 text-white bg-indigo-600 rounded-md hover:bg-indigo-700 text-sm font-semibold">Apply</button>
              </div>
            </div>
            <div>
              <button @click="openFilterSection = openFilterSection === 'storage' ? null : 'storage'"
                class="w-full flex justify-between items-center p-2 font-medium rounded-lg"
                :class="theme === 'dark' ? 'hover:bg-white/5' : 'hover:bg-black/5'">
                <span>Storage</span>
                <svg class="w-5 h-5 transition-transform" :class="{ 'rotate-180': openFilterSection === 'storage' }"
                  fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              <div v-if="openFilterSection === 'storage'" class="mt-2 pl-2 pr-1 space-y-1 max-h-40 overflow-y-auto">
                <label v-for="storage in storageRanges" :key="storage"
                  class="flex items-center p-2 rounded-lg cursor-pointer"
                  :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'">
                  <input type="checkbox" :value="storage" v-model="selectedStorages"
                    class="form-checkbox h-4 w-4 rounded-sm" />
                  <span class="ml-3 text-sm">{{ storage }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>
        <div class="flex-shrink-0">
          <button @click="clearAllFilters()"
            class="w-full mt-4 flex items-center justify-center gap-1 rounded-full px-4 py-2 text-sm font-semibold transition-colors duration-300 border border-red-500 text-red-500 hover:bg-red-500/10">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
              stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
            Clear All Filters
          </button>
        </div>
      </div>
    </aside>

    <main class="transition-transform duration-300 ease-in-out">
      <div class="container mx-auto py-8 px-6">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4 mb-8">
          <button @click="isSidebarOpen = true"
            class="flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition-colors duration-300 border"
            :class="theme === 'dark' ? 'bg-gray-800 text-white border-gray-700 hover:bg-gray-700' : 'bg-white text-gray-950 border-gray-300 hover:bg-gray-100'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
              stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h7" />
            </svg>
            <span>Menu</span>
          </button>

          <div class="itbms-search-bar relative w-full md:max-w-md">
            <input type="text" placeholder="Search for models, brands..." v-model="searchQuery"
              class="itbms-search-input py-2 pl-10 pr-4 w-full focus:outline-none rounded-full border focus-within:ring-2 focus-within:ring-indigo-500"
              :class="theme === 'dark' ? 'border-gray-700 bg-gray-800 text-white placeholder-gray-400' : 'border-gray-300 bg-white text-gray-950 placeholder-gray-500'" />
            <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none"><svg
                xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M21 21l-6-6m2-6a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg></div>
          </div>

          <div class="flex items-center gap-4">
            <button v-if="userRole === 'SELLER'" @click="addSaleItemButton"
              class="itbms-sale-item-add bg-indigo-600 text-white rounded-full px-5 py-2 text-sm font-semibold transition hover:bg-indigo-700 whitespace-nowrap">+
              Add Item</button>
            <div class="relative itbms-cart-icon cursor-pointer p-2 rounded-full"
              :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'" @click="goToCart">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <span v-if="cartCount > 0"
                class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-bold">{{
                  cartCount > 99 ? "99+" : cartCount }}</span>
            </div>
          </div>
        </div>
        <div class="mb-6 flex flex-wrap items-center gap-2" v-if="activeFilters.length > 0">
          <div v-for="filter in activeFilters" :key="filter.value"
            :class="theme === 'dark' ? 'bg-indigo-500/20 text-indigo-300' : 'bg-indigo-100 text-indigo-800'"
            class="flex items-center rounded-full pl-3 pr-2 py-1 text-sm font-medium">
            <span>{{ filter.value }}</span>
            <button @click="removeActiveFilter(filter)" class="ml-2 focus:outline-none transition-colors rounded-full"
              :class="theme === 'dark' ? 'hover:bg-white/20' : 'hover:bg-black/10'"><svg
                xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg></button>
          </div>
        </div>
        <div v-if="isGridView">
          <div v-if="items && items.length > 0"
            class="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            <div v-for="(item, index) in items" :key="item.id"
              class="itbms-row rounded-2xl p-4 transition-all duration-300 transform hover:-translate-y-1 cursor-pointer group flex flex-col"
              :class="theme === 'dark' ? 'bg-gray-800/50 hover:bg-gray-800' : 'bg-white shadow-sm hover:shadow-lg'"
              :style="{ animationDelay: (index % pageSize * 50) + 'ms' }" @click="goToPhoneDetails(item.id)">
              <img :src="'/sy4/phone/iPhone.png'" alt="phone" class="w-full h-40 object-contain mb-4" />
              <div class="flex-grow">
                <div class="itbms-brand font-bold text-sm text-indigo-500 dark:text-indigo-400">{{ item.brandName }}
                </div>
                <div class="itbms-model font-semibold mt-1 truncate">{{ item.model }}</div>
                <div class="text-xs mt-1" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'"><span
                    class="itbms-storageGb">{{ item.storageGb || "-" }} GB / {{ item.rabGb || "-" }} GB</span></div>
              </div>
              <div class="mt-4">
                <div class="itbms-price font-extrabold text-xl">{{ item.price.toLocaleString() }} <span
                    class="text-sm font-normal">THB</span></div>
                <button @click.stop="addToCart(item)" :disabled="isItemSoldOut(item)"
                  class="mt-2 w-full px-4 py-2 text-sm rounded-full font-semibold transition-colors duration-300 disabled:bg-slate-300 disabled:text-slate-500 disabled:cursor-not-allowed dark:disabled:bg-gray-600 dark:disabled:text-gray-400"
                  :class="theme === 'dark' ? 'bg-indigo-500 text-white hover:bg-indigo-600' : 'bg-indigo-100 text-indigo-700 hover:bg-indigo-200'">{{
                    isItemSoldOut(item) ? "In Cart" : "Add to Cart" }}</button>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-20 text-slate-500 dark:text-slate-400">
            <p class="text-xl font-semibold">No items found</p>
            <p>Try adjusting your search or filter criteria.</p>
          </div>
        </div>
        <div v-else>
          <div v-if="items && items.length > 0" class="rounded-2xl overflow-hidden"
            :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-white shadow-sm'">
            <table class="w-full text-sm text-left">
              <thead :class="theme === 'dark' ? 'bg-gray-700/30 text-slate-300' : 'bg-slate-100 text-slate-600'">
                <tr>
                  <th class="px-4 py-3">Id</th>
                  <th class="px-4 py-3">Brand</th>
                  <th class="px-4 py-3">Model</th>
                  <th class="px-4 py-3">Ram</th>
                  <th class="px-4 py-3">Storage</th>
                  <th class="px-4 py-3">Color</th>
                  <th class="px-4 py-3">Price</th>
                  <th class="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in items" :key="item.id" class="itbms-row transition-colors duration-200 border-t"
                  :class="theme === 'dark' ? 'border-gray-800 hover:bg-gray-800' : 'border-slate-100 hover:bg-slate-50'">
                  <td class="itbms-id px-4 py-3">{{ item.id }}</td>
                  <td class="itbms-brand px-4 py-3">{{ item.brandName }}</td>
                  <td class="itbms-model px-4 py-3">{{ item.model }}</td>
                  <td class="itbms-model px-4 py-3">{{ item.ramGb || "-" }}</td>
                  <td class="itbms-model px-4 py-3">{{ item.storageGb || "-" }}</td>
                  <td class="itbms-model px-4 py-3">{{ item.color || "-"}}</td>
                  <td class="itbms-price px-4 py-3">{{ item.price.toLocaleString() }}</td>
                  <td class="px-4 py-3 text-right space-x-2"><button @click.stop="goToEditItem(item.id)"
                      class="itbms-edit-button font-semibold bg-yellow-500/10 text-yellow-600 dark:text-yellow-400 rounded-full px-4 py-1 transition-colors duration-300 hover:bg-yellow-500/20">Edit</button><button
                      @click.stop="deleteproduct(item)"
                      class="itbms-delete-button font-semibold bg-red-500/10 text-red-600 dark:text-red-400 rounded-full px-4 py-1 transition-colors duration-300 hover:bg-red-500/20">Delete</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="text-center py-20 text-slate-500 dark:text-slate-400">
            <p class="text-xl font-semibold">No items listed</p>
            <p>Click "+ Add Item" to start selling.</p>
          </div>
        </div>
        <div v-if="totalPages > 1" class="flex justify-center mt-10">
          <div class="flex items-center gap-2 rounded-full p-1 text-sm font-semibold"
            :class="theme === 'dark' ? 'bg-gray-800' : 'bg-slate-200'">
            <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 0"
              class="px-3 py-1 rounded-full disabled:opacity-50"
              :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/10'">&lt; Prev</button>
            <button v-for="page in visiblePages" :key="page" @click="goToPage(page - 1)" class="px-3 py-1 rounded-full"
              :class="currentPage === page - 1 ? 'bg-indigo-600 text-white' : (theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/10')">{{
              page }}</button>
            <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages - 1"
              class="px-3 py-1 rounded-full disabled:opacity-50"
              :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/10'">Next &gt;</button>
          </div>
        </div>
      </div>
    </main>

    <transition name="bounce-popup">
      <div v-if="showDeleteConfirmationPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="rounded-3xl p-8 shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Confirm delete the product</h2>
          <p class="itbms-message mb-4">Do you want to delete this sale item?</p>
          <div class="flex justify-center gap-4">
            <button @click="confirmDelete"
              class="itbms-confirm-button bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Yes</button>
            <button @click="cancelDeleteItem"
              class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold hover:cursor-pointer">No</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade-background">
      <div v-if="isDeleting"
        class="fixed top-0 left-0 w-full h-full bg-black flex items-center justify-center z-50 loading-overlay">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none"
            viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor"
              d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
          </svg>
          <p class="itbms-message text-sm font-medium">Deleting product...</p>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showAddSuccessPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The sale item has been successfully added.</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showRegisSuccess"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <DotLottieVue src="/sy4/animation/Success.lottie" autoplay class="w-24 h-24 mx-auto mb-4" />
          <h2 class="text-xl font-semibold mb-4">Account Created!</h2>
          <p class="itbms-message mb-4">We've sent a verification link to your email.</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showLoginSuccess"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <DotLottieVue src="/sy4/animation/Success.lottie" autoplay class="w-24 h-24 mx-auto mb-4" />
          <h2 class="text-xl font-semibold mb-4">Login Successful</h2>
          <p class="itbms-message mb-4">You have been successfully logged into your account.</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showDeleteSuccessPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The sale item has been successfully deleted!</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showOrderSuccess"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <DotLottieVue src="/sy4/animation/Success.lottie" autoplay class="w-24 h-24 mx-auto mb-4" />
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">Your order has been placed and confirmed</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showfailPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">The sale item has been Fail added!</h2>
          <p class="itbms-message mb-4">Please try again later.</p>
          <button @click="closeSuccessPopup"
            class="bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold hover:cursor-pointer">Done</button>
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
  /* Smoother easing */
}

.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-150%);
  opacity: 0;
}

/* Custom styles for checkbox to match the new theme */
.form-checkbox,
.form-radio {
  appearance: none;
  background-color: transparent;
  border-radius: 0.25rem;
  border-width: 2px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.dark .form-checkbox,
.dark .form-radio {
  border-color: #4b5563;
}

.light .form-checkbox,
.light .form-radio {
  border-color: #cbd5e1;
}

.form-checkbox:checked,
.form-radio:checked {
  background-color: #6366f1;
  border-color: #6366f1;
}

.form-checkbox:checked {
  background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
}

.form-radio {
  border-radius: 9999px;
}

.form-radio:checked {
  background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3ccircle cx='8' cy='8' r='3'/%3e%3c/svg%3e");
}

.form-checkbox:focus,
.form-radio:focus {
  outline: 2px solid transparent;
  outline-offset: 2px;
  box-shadow: 0 0 0 2px #818cf8;
}

@keyframes fadeInUp {
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
  animation: fadeInUp 0.5s ease forwards;
}

.itbms-bg {
  background-color: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
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

.fade-background-enter-from,
.fade-background-leave-to {
  background-color: rgba(0, 0, 0, 0);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

.loading-overlay {
  background-color: rgba(0, 0, 0, 0.2);
}
</style>