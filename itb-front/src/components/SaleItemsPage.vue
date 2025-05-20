<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems, deleteItemById } from '@/libs/fetchUtilsOur';
import Footer from './Footer.vue'

const router = useRouter()
const items = ref([])
const searchQuery = ref('')
const filterBy = ref('')
const route = useRoute()
const showAddSuccessPopup = ref(false)
const showDeleteSuccessPopup = ref(false)
const showfailPopup = ref(false)
const isGridView = computed(() => route.path !== '/sale-items/list')

const addSaleItemButton = () => {
  router.push('/sale-items/add')
}

const goToManageBrand = () => {
  router.push('/brands')
}

const goToPhoneDetails = (id) => {
  router.push(`/sale-items/${id}`)
}

const goToEditItem = (id) => {
  router.push(`/sale-items/${id}/edit`)
}

onMounted(async () => {
  try {
    const data = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items')
    items.value = data
  } catch (err) {
    console.error('Error loading items:', err)
  }
})

watch(
  () => route.query.addSuccess,
  (addSuccess) => {
    if (addSuccess === 'true') {
      setTimeout(() => {
        showAddSuccessPopup.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

watch(
  () => route.query.deleteSuccess,
  (deleteSuccess) => {
    if (deleteSuccess === 'true') {
      setTimeout(() => {
        showDeleteSuccessPopup.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

watch(
  () => route.query.addFail,
  (addFail) => {
    if (addFail === 'true') {
      setTimeout(() => {
        showfailPopup.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

const sortedItemsByCreatedOn = computed(() => {
  return [...items.value].sort((a, b) => new Date(b.createdOn) - new Date(a.createdOn));
})

const filteredAndSortedItems = computed(() => {
  let result = [...sortedItemsByCreatedOn.value]  

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item =>
      item.brandName.toLowerCase().includes(query) ||
      item.model.toLowerCase().includes(query)
    )
  }

  return result
})

const deleteResponseMessage = ref('')
const showDeleteConfirmationPopup = ref(false)
const isDeleting = ref(false)
const deleteSale = ref(null)
const deleteproduct = async (item) => {
  deleteSale.value = item.id
  showDeleteConfirmationPopup.value = true
}

const confirmDelete = async () => {
  showDeleteConfirmationPopup.value = false
  isDeleting.value = true
  try {
    const statusCode = await deleteItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items', deleteSale.value);
    if (statusCode === 204) {
      setTimeout(() => {
        isDeleting.value = false
        router.push({ path: '/sale-items/list', query: { deleteSuccess: 'true' } })
      }, 1000)
    }else if (statusCode === 404) {
      // กรณีได้รับ 404 ตอนลบ แสดงว่าข้อมูลไม่มีแล้ว
      isDeleting.value = false;
      showNotFoundPopup.value = true;
      startCountdown();
      setTimeout(() => {
        router.push('/sale-items/list');
      }, 3000);
    }
  } catch (error) {
    console.error("delete Fall:", error);
    deleteResponseMessage.value = ('เกิดข้อผิดพลาดในการลบสินค้า')
    isDeleting.value = false
  }
}

const cancelDeleteItem = () => {
  showDeleteConfirmationPopup.value = false
}

const closeSuccessPopup = () => {
  showAddSuccessPopup.value = false
  showDeleteSuccessPopup.value = false
  showfailPopup.value = false
  router.replace(route.path)
  router.go(0)
}
</script>

<template>
  <div class="Itbms-sale-items-page bg-white min-h-screen">
    <div class="Itbms-header container mx-auto py-8 flex items-center justify-between">
      <div class="Itbms-logo font-bold text-3xl text-black">ITB MShop</div>
      <div class="flex-grow flex justify-center">
        <div class="Itbms-search-bar flex items-center rounded-md border border-gray-300 focus-within:border-blue-500 w-full max-w-md">
          <input type="text" placeholder="Search..." v-model="searchQuery" class="Itbms-search-input py-2 px-3 w-full focus:outline-none rounded-l-md text-black" />
          <button class="Itbms-search-button bg-gray-100 hover:bg-gray-200 p-2 rounded-r-md focus:outline-none">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-6a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
        </div>
      </div>
      <div class="Itbms-icons flex flex-col items-end space-y-2">
        <div class="flex items-center space-x-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-black cursor-pointer" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0" y="0" class="h-8 w-8 text-black cursor-pointer" viewBox="0 0 128 128">
            <g><path d="M125.1 43.6h-20.4V17.5H84.4v-2.9H46.5v2.9H26.2v26.2H2.9C1.3 43.7 0 45 0 46.6v8.7c0 1.6 1.3 2.9 2.9 2.9h122.2c1.6 0 2.9-1.3 2.9-2.9v-8.7c0-1.7-1.3-3-2.9-3zm-26.2 0H32V23.3h14.5v2.9h37.8v-2.9h14.5v20.3zm-78.5 64c0 3.2 2.6 5.8 5.8 5.8h72.7c3.2 0 5.8-2.6 5.8-5.8l14.5-46.5H8.7l11.7 46.5zm61.1-36.3c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3z"/></g>
          </svg>
        </div>
      </div>
    </div>
    <div class="ml-[8%] flex items-start justify-between mb-4">
      <button
        class="itbms-sale-item-add bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500"
        @click="addSaleItemButton"
      >
        + Add Sell Item
      </button>
      <div class="mr-[5%]">
        <button
          v-if="!isGridView"
          @click="goToManageBrand"
          class="itbms-manage-brand bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500"
        >
          Manage Brand
        </button>
      </div>
    </div>

    <div v-if="isGridView" class="p-6">
      <div v-if="filteredAndSortedItems.length === 0" class="text-gray-500 text-center">No sale items found.</div>
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        <div
          v-for="(item, index) in filteredAndSortedItems"
          :key="item.id"
          class="itbms-row border rounded-lg p-4 shadow hover:shadow-lg text-black cursor-pointer"
          :style="{ animationDelay: (index * 50) + 'ms' }"
          @click="goToPhoneDetails(item.id)"
        >
          <img
            :src="'/sy4/phone/iPhone.jpg'"
            alt="phone"
            class="w-full h-40 object-contain mb-4"
          />
          <div class="itbms-brand font-semibold">{{ item.brandName }}</div>
          <div class="itbms-model text-sm">{{ item.model }}</div>
          <span class="itbms-ramGb text-sm">{{ item.ramGb || '-' }}</span>/<span class="itbms-storageGb text-sm">{{ item.storageGb || '-' }}</span> <span class="itbms-storageGb-unit text-sm">GB</span>
          <div class="itbms-price mt-2 font-bold text-lg">{{ item.price.toLocaleString() }}</div> <div class="itbms-price-unit text-sm">Baht</div>
        </div>
      </div>
    </div>

    <div v-else class="p-6">
      <div v-if="filteredAndSortedItems.length === 0" class="text-gray-500 text-center">
        No sale items found.
      </div>
      <table v-else class="w-full border-collapse border text-sm text-left text-black">
        <thead>
          <tr class="bg-gray-100">
            <th class="border px-4 py-2">Id</th>
            <th class="border px-4 py-2">Brand</th>
            <th class="border px-4 py-2">Model</th>
            <th class="border px-4 py-2">Ram</th>
            <th class="border px-4 py-2">Storage</th>
            <th class="border px-4 py-2">Color</th>
            <th class="border px-4 py-2">Price</th>
            <th class="border px-4 py-2">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in filteredAndSortedItems"
            :key="item.id"
            class="itbms-row hover:bg-gray-50 transition"
          >
            <td class="itbms-id border px-4 py-2">{{ item.id }}</td>
            <td class="itbms-brand border px-4 py-2">{{ item.brandName }}</td>
            <td class="itbms-model border px-4 py-2">{{ item.model }}</td>
            <td class="itbms-ramGb border px-4 py-2">{{ item.ramGb || '-'}}</td>
            <td class="itbms-storageGb border px-4 py-2">{{ item.storageGb || '-' }}</td>
            <td class="itbms-color border px-4 py-2">{{ item.color || '-'}}</td>
            <td class="itbms-price border px-4 py-2">{{ item.price.toLocaleString() }}</td>
            <td class="border px-4 py-2 space-x-1">
              <button
                @click.stop="goToEditItem(item.id)"
                class="itbms-edit-button bg-yellow-500 text-white border-2 border-yellow-500 rounded px-2 py-1 hover:bg-transparent hover:text-yellow-500 transition-colors duration-300 cursor-pointer"
              >
                Edit
              </button>
              <button
                @click.stop="deleteproduct(item)"
                class="itbms-delete-button bg-red-500 text-white border-2 border-red-500 rounded px-2 py-1 hover:bg-transparent hover:text-red-500 transition-colors duration-300 cursor-pointer"
              >
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
    v-if="showDeleteConfirmationPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Confirm delete the product</h2>
      <p class="itbms-message mb-4">Do you want to delete this sale item?</p>
      <div class="flex justify-center gap-4">
        <button @click="confirmDelete" class="itbms-confirm-button bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
        <button @click="cancelDeleteItem" class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>     
      </div>
    </div>
  </div>
  <transition name="fade-background">
      <div v-if="isDeleting" class="fixed top-0 left-0 w-full h-full bg-black flex items-center justify-center z-50 loading-overlay">
        <div class="bg-white text-black p-6 rounded-lg shadow-lg text-center">
          <svg class="animate-spin h-8 w-8 text-blue-600 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z"/>
          </svg>
          <p class="itbms-message text-sm font-medium">Deleting product...</p>
        </div>
      </div>
    </transition>
    <transition name="bounce-popup">
      <div
        v-if="showAddSuccessPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
      >
        <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The sale item has been successfully added!</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
        </div>
      </div>
    </transition>
    <transition name="bounce-popup">
      <div
        v-if="showDeleteSuccessPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
      >
        <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The sale item has been successfully deleted!</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div
        v-if="showfailPopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
      >
        <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
          <h2 class="text-xl font-semibold mb-4">Error 500!</h2>
          <p class="itbms-message mb-4">The sale item has been Fail added!</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
        </div>
      </div>
    </transition>
  </div>
  <Footer />
</template>

<style scoped>
.active {
  background: white;
  border-radius: 9999px;
  color: #1D232A;
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

/* Style for the list view item */
.itbms-list-item {
  transition: transform 0.2s ease-in-out;
}

.itbms-list-item:hover {
  transform: scale(1.02);
}
.itbms-list-item {
  opacity: 0;
  animation: fadeInUp 0.5s ease forwards;
}

/* สไตล์พื้นหลัง popup overlay */
.itbms-bg {
  background-color: rgba(0, 0, 0, 0.3); /* opacity 0.3 = โปร่งนุ่มขึ้น */
  backdrop-filter: blur(2px); /* เพิ่ม blur ด้านหลังให้หรู */
}

.bounce-popup-enter-active,
.bounce-popup-leave-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); /* bounce effect */
}

.bounce-popup-enter-from {
  transform: scale(0.8);
  opacity: 0;
}

.bounce-popup-leave-to {
  transform: scale(1.2);
  opacity: 0;
}

/* Animation สำหรับ Fade In/Out ของพื้นหลัง */
.fade-background-enter-active,
.fade-background-leave-active {
  transition: background-color 0.3s ease;
}

.fade-background-enter-from {
  background-color: rgba(0, 0, 0, 0); /* เริ่มจาก Opacity 0 */
}

.fade-background-leave-to {
  background-color: rgba(0, 0, 0, 0); /* จบที่ Opacity 0 */
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
  background-color: rgba(0, 0, 0, 0.2); /* หรือค่าอื่น ๆ ที่คุณต้องการ */
}
</style>
