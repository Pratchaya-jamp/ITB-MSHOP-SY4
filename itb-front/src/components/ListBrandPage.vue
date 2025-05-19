<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '@/libs/fetchUtilsOur';
import Footer from './Footer.vue'
import { deleteItemById } from '@/libs/fetchUtilsOur'

const router = useRouter()
const route = useRoute()
const items = ref([])
const searchQuery = ref('')
const filterBy = ref('')
const viewMode = ref('list') // กำหนดค่าเริ่มต้นเป็น 'grid'

const showBrandNameDelete = ref('')
const isDeleting = ref(false)
const showDeleteConfirmationPopup = ref(false)
const showDeleteSuccessPopup = ref(false)
const showAddSuccessPopup = ref(false)
const showEditSuccessPopup = ref(false)
const showEditFailPopup = ref(false)
const showfailPopup = ref(false)
const showNotFoundPopup = ref(false)
const showcannotDeletePopup =ref(false)
const deleteId = ref(null)
const countdown = ref(3)
const startCountdown = () => {
  if (countdown.value > 0) {
    setTimeout(() => {
      countdown.value--
      startCountdown() // เรียกตัวเองซ้ำ
    }, 1000)
  }
}

const addBrandtemButton = () => {
  router.push('/brands/add')
}

const goToSaleItemsList = () => {
  router.push('/sale-items/list') // ไปยังหน้า Sale Items แบบ List
}

onMounted(async () => {
  try {
    const data = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/api/v1/brands')
    items.value = data.sort((a, b) => a.id - b.id)
  } catch (err) {
    console.error('Error loading items:', err)
  }
})

const filteredAndSortedItems = computed(() => {
  let result = [...items.value]

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item =>
      item.brandName.toLowerCase().includes(query)
    )
  }

  return result
})

watch(
  () => route.query.addSuccess,
  (addSuccess) => {
    if (addSuccess === 'true') {
      setTimeout (() => {
        showAddSuccessPopup.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

watch(
  () => route.query.editSuccess,
  (editSuccess) => {
    if (editSuccess === 'true') {
      setTimeout(() => {
        showEditSuccessPopup.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

watch(
  () => route.query.editFail,
  (editFail) => {
    if (editFail === 'true') {
      setTimeout(() => {
        showEditFailPopup.value = true
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

const deletebrand = async (item) => {
  deleteId.value = item.id
  showBrandNameDelete.value = item.brandName
  showDeleteConfirmationPopup.value = true
}

const confirmDelete = async () => {
  showDeleteConfirmationPopup.value = false
  isDeleting.value = true
  try {
    const statusCode = await deleteItemById('http://intproj24.sit.kmutt.ac.th/sy4/api/v1/brands', deleteId.value);
    if (statusCode === 204) {
      setTimeout(() => {
        isDeleting.value = false
        items.value = items.value.filter(item => item.id !== deleteId.value)
        showDeleteSuccessPopup.value = true
      }, 1000)
    }else if (statusCode === 404) {
      // กรณีได้รับ 404 ตอนลบ แสดงว่าข้อมูลไม่มีแล้ว
      isDeleting.value = false;
      showNotFoundPopup.value = true;
      startCountdown();
      setTimeout(() => {
        showNotFoundPopup.value = false;
      }, 3000);
    }else if (statusCode === 400) {
      isDeleting.value = false;
      setTimeout(() => {
        showcannotDeletePopup.value = true;
      }, 1000);
    }
  } catch (error) {
    console.error("delete Fail:", error);
    deleteResponseMessage.value = ('เกิดข้อผิดพลาดในการลบสินค้า')
    isDeleting.value = false
  }
}

const cancelDeleteItem = () => {
  showDeleteConfirmationPopup.value = false
}

const closeSuccessPopup = () => {
  showAddSuccessPopup.value = false
  showEditSuccessPopup.value = false
  showDeleteSuccessPopup.value = false
  showEditFailPopup.value =false
  showfailPopup.value = false
  showcannotDeletePopup.value = false
}

const setViewMode = (mode) => {
  viewMode.value = mode;
}

</script>

<template>
  <div class="Itbms-brands-page bg-white min-h-screen">
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
      <div class="flex space-x-4 items-center">
        <button
          @click="goToSaleItemsList"
          class="itbms-item-list bg-gray-300 text-black border-2 border-gray-300 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-gray-300"
        >
          Sale Item List
        </button>
        <button
          @click="addBrandtemButton"
          class="itbms-add-button bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500"
        >
          + Add Brand
        </button>
      </div>
      <div class="flex flex-col items-end mr-[5%]">
        <div class="mb-2">
        </div>
        <div class="bg-gray-200 text-sm text-gray-500 leading-none border-2 border-gray-200 rounded-full inline-flex">
          <button
            :class="['inline-flex items-center transition-colors duration-300 ease-in focus:outline-none hover:text-gray-900 focus:text-gray-900 rounded-l-full px-4 py-2', { 'active': viewMode === 'grid' }]"
            id="grid"
            @click="setViewMode('grid')"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="fill-current w-4 h-4 mr-2" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
            <span>Grid</span>
          </button>
          <button
            :class="['itbms-manage-brand inline-flex items-center transition-colors duration-300 ease-in focus:outline-none hover:text-gray-900 focus:text-gray-900 rounded-r-full px-4 py-2', { 'active': viewMode === 'list' }]"
            id="list"
            @click="setViewMode('list')"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="fill-current w-4 h-4 mr-2"><line x1="8" y1="6" x2="21" y2="6"></line><line x1="8" y1="12" x2="21" y2="12"></line><line x1="8" y1="18" x2="21" y2="18"></line><line x1="3" y1="6" x2="3.01" y2="6"></line><line x1="3" y1="12" x2="3.01" y2="12"></line><line x1="3" y1="18" x2="3.01" y2="18"></line></svg>
            <span>List</span>
          </button>
        </div>
      </div>
    </div>

    <div class="p-6" v-if="viewMode === 'grid'">
      <div v-if="filteredAndSortedItems.length === 0" class="text-gray-500 text-center">No brand found.</div>
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        <div
          v-for="item in filteredAndSortedItems"
          :key="item.id"
          class="itbms-row border rounded-lg p-4 shadow hover:shadow-lg text-black cursor-pointer"
        >
          <img :src="`brands/${item.id}.png`" alt="brand" class="w-full h-40 object-contain mb-4" />
          <div class="font-semibold text-center">{{ item.brandName }}</div>
        </div>
      </div>
    </div>

    <div class="p-6" v-else>
      <div v-if="filteredAndSortedItems.length === 0" class="text-gray-500 text-center">
        No brand found.
      </div>
      <table v-else class="w-full border-collapse border text-sm text-left text-black">
        <thead>
          <tr class="bg-gray-100">
            <th class="border px-4 py-2">Id</th>
            <th class="border px-4 py-2">Name</th>
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

            <td class="border  px-4 py-2">
              <div class="flex items-center space-x-3">
                <img
                  :src="`brands/${item.id}.png`"
                  alt="brand"
                  class="w-10 h-10 object-contain rounded"
                />
                <span class="itbms-name">{{ item.brandName }}</span>
              </div>
            </td>

            <td class="border px-4 py-2">
              <div class="flex space-x-1">
                <button
                  @click="router.push(`/brands/${item.id}/edit`)"
                  class="itbms-edit-button bg-yellow-500 text-white border-2 border-yellow-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-yellow-500 text-sm"
                >
                  Edit
                </button>
                <button
                  @click.stop="deletebrand(item)"
                  class="itbms-delete-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500 text-sm"
                >
                  Delete
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    
  </div>
  <Footer />
<transition name="bounce-popup">
  <div
    v-if="showDeleteConfirmationPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Confirm delete the brand</h2>
      <p class="itbms-message mb-4">Do you want to delete {{ showBrandNameDelete }} brand ?</p>
      <div class="flex justify-center gap-4">
        <button @click="confirmDelete" class="itbms-confirm-button bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
        <button @click="cancelDeleteItem" class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>      
      </div>
    </div>
  </div>
</transition>
<transition name="fade-background">
      <div v-if="isDeleting" class="fixed top-0 left-0 w-full h-full bg-black flex items-center justify-center z-50 loading-overlay">
        <div class="bg-white text-black p-6 rounded-lg shadow-lg text-center">
          <svg class="animate-spin h-8 w-8 text-blue-600 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z"/>
          </svg>
          <p class="itbms-message text-sm font-medium">Deleting Brand...</p>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
  <div
    v-if="showNotFoundPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50"
  >
    <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center max-w-sm w-full">
      <h2 class="text-xl font-semibold mb-4">⚠️ Item not found.</h2>
      <p class="itbms-message mb-2">The requested brand does not exist.</p>
      <p class="text-sm text-gray-500">Bring You Back in {{ countdown }} second<span v-if="countdown > 1">s</span>...</p>
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
          <p class="itbms-message mb-4">The brand has been successfully deleted!</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
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
          <p class="itbms-message mb-4">The brand has been successfully added!</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
  <div
    v-if="showEditSuccessPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
      <h2 class="ext-xl font-semibold mb-4">Success!</h2>
      <p class="itbms-message mb-4">The brand has been successfully updated!</p>
      <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
    </div>
  </div>
</transition>

<transition name="bounce-popup">
  <div
    v-if="showEditFailPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Error 500!</h2>
      <p class="itbms-message mb-4">The brand has been fail to updated!</p>
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
          <h2 class="text-xl text-red-500 font-semibold mb-4">Error 500!</h2>
          <p class="itbms-message mb-4">The status could not be added.</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div
        v-if="showcannotDeletePopup"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
      >
        <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
          <h2 class="text-xl text-red-500 font-semibold mb-4">Delete this brand is not allow!</h2>
          <p class="itbms-message mb-4">There are sale items with this brand.</p>
          <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
        </div>
      </div>
    </transition>
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
