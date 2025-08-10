<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '@/libs/fetchUtilsOur';
import { deleteItemById } from '@/libs/fetchUtilsOur'

// 1. ดึงค่า theme จาก localStorage ตั้งแต่แรก
const theme = ref(localStorage.getItem('theme') || 'dark')

// 2. ฟังก์ชันสำหรับสลับ Theme และอัปเดต localStorage
const toggleTheme = () => {
  const newTheme = theme.value === 'dark' ? 'light' : 'dark'
  theme.value = newTheme
  localStorage.setItem('theme', newTheme) // บันทึกค่าใหม่ลงใน localStorage
}

// 3. ตั้งค่า Event Listener เมื่อ component ถูก mount
onMounted(async () => {
  // ตั้งค่า theme ตาม localStorage
  const storedTheme = localStorage.getItem('theme');
  if (storedTheme) {
    theme.value = storedTheme;
  }
  
  // เพิ่ม event listener เพื่อฟังการเปลี่ยนแปลงของ localStorage จากหน้าอื่น
  window.addEventListener('storage', (event) => {
    if (event.key === 'theme') {
      theme.value = event.newValue;
    }
  });

  try {
    const data = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands')
    items.value = data.sort((a, b) => a.id - b.id)
    const saleItemsData = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items')
    saleItems.value = saleItemsData
  } catch (err) {
    console.error('Error loading items:', err)
  }
})

// 4. Watcher สำหรับอัปเดต class ของ body ตาม theme
watch(theme, (newTheme) => {
  document.body.className = newTheme === 'dark' ? 'dark-theme' : '';
});

// โค้ดส่วนอื่นๆ ที่เกี่ยวกับ Theme ยังคงเดิม
const themeClass = computed(() => (theme.value === 'dark' ? 'dark bg-gray-900 text-white' : 'light bg-white text-gray-950'))
const iconComponent = computed(() => {
  return theme.value === 'dark'
    ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>` // sun icon
    : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-800" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>` // moon icon
})

const router = useRouter()
const route = useRoute()
const items = ref([])
const saleItems = ref([])
const searchQuery = ref('')
const filterBy = ref('')
const viewMode = ref('list') 

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
      startCountdown() 
    }, 1000)
  }
}

const addBrandtemButton = () => {
  router.push('/brands/add')
}

const goToSaleItemsList = () => {
  router.push('/sale-items/list') 
}


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
  const hasRelated = saleItems.value.some(saleItem => saleItem.brandName === item.brandName);
  hasRelatedSaleItemsOnPopup.value = hasRelated;
  deleteId.value = item.id;
  showBrandNameDelete.value = item.brandName;
  if (hasRelated) {
    deleteMessage.value = `Delete ${item.brandName} is not allowed. There are sale items with ${item.brandName} brand.`;
  } else {
    deleteMessage.value = `Do you want to delete ${item.brandName} brand?`;
  }
  showDeleteConfirmationPopup.value = true;
}

const deleteMessage = ref('')
const hasRelatedSaleItemsOnPopup = ref(false)

const confirmDelete = async () => {
  showDeleteConfirmationPopup.value = false
  if (hasRelatedSaleItemsOnPopup.value) {
    setTimeout(() => {
      showcannotDeletePopup.value = true; 
    }, 200);
    return;
  }

  isDeleting.value = true
  try {
    const statusCode = await deleteItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands', deleteId.value);
    if (statusCode === 204) {
      setTimeout(() => {
        isDeleting.value = false
        items.value = items.value.filter(item => item.id !== deleteId.value)
        showDeleteSuccessPopup.value = true
      }, 1000)
    }else if (statusCode === 404) {
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

const currentPage = ref(parseInt(route.query.page) || 1)
const pageSize = ref(10)
  
const totalPages = computed(() =>
  Math.ceil(filteredAndSortedItems.value.length / pageSize.value)
)
  
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAndSortedItems.value.slice(start, end)
})

const lastAction = ref('') 
const fixedStart = ref(1) 
const maxVisible = 10
  
const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const action = lastAction.value
  
  let start, end
  
  if (action === 'next') {
    const endOfGroup = fixedStart.value + maxVisible - 1
  
    if (current > endOfGroup) {
      end = Math.min(current, total)
      start = Math.max(end - maxVisible + 1, 1)
      fixedStart.value = start
    }
  }
  
  if (action === 'prev') {
    if (current < fixedStart.value) {
      start = Math.max(current, 1)
      fixedStart.value = start
    }
  }
  
  start = fixedStart.value
  end = Math.min(start + maxVisible - 1, total)
  
  if (start < 1) start = 1
  if (end > total) {
    end = total
    start = Math.max(end - maxVisible + 1, 1)
  }
  
  const pages = []
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})
  
  
  
watch(currentPage, (newPage) => {
  router.replace({ query: { ...route.query, page: newPage } })
})
  
watch([pageSize, searchQuery], () => {
  currentPage.value = 1
})

</script>

<template>
  <div :class="themeClass" class="itbms-brands-page min-h-screen font-sans overflow-hidden">
    <div class="container mx-auto py-8 px-6 md:px-0 flex flex-col md:flex-row items-center justify-between gap-4">
      <div class="itbms-logo font-extrabold text-3xl">ITB MShop</div>
      
      <div class="flex-grow flex flex-col md:flex-row items-center gap-4 w-full md:w-auto">
        <div class="itbms-search-bar flex items-center rounded-full border focus-within:border-orange-500 w-full md:max-w-md" :class="theme === 'dark' ? 'border-gray-700 bg-gray-800' : 'border-gray-300 bg-white'">
          <input type="text" placeholder="Search..." v-model="searchQuery" class="itbms-search-input py-2 px-4 w-full focus:outline-none rounded-l-full" :class="theme === 'dark' ? 'bg-gray-800 text-white placeholder-gray-400' : 'bg-white text-gray-950 placeholder-gray-500'" />
          <button class="itbms-search-button p-2 rounded-r-full transition-colors duration-300" :class="theme === 'dark' ? 'bg-gray-700 hover:bg-gray-600' : 'bg-gray-100 hover:bg-gray-200'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-6a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
        </div>
      </div>
      
      <div class="itbms-icons flex flex-col items-end space-y-2">
        <div class="flex items-center space-x-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 cursor-pointer" :class="theme === 'dark' ? 'text-white' : 'text-black'" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0" y="0" class="h-8 w-8 cursor-pointer" :class="theme === 'dark' ? 'text-white' : 'text-black'" viewBox="0 0 128 128">
            <g><path d="M125.1 43.6h-20.4V17.5H84.4v-2.9H46.5v2.9H26.2v26.2H2.9C1.3 43.7 0 45 0 46.6v8.7c0 1.6 1.3 2.9 2.9 2.9h122.2c1.6 0 2.9-1.3 2.9-2.9v-8.7c0-1.7-1.3-3-2.9-3zm-26.2 0H32V23.3h14.5v2.9h37.8v-2.9h14.5v20.3zm-78.5 64c0 3.2 2.6 5.8 5.8 5.8h72.7c3.2 0 5.8-2.6 5.8-5.8l14.5-46.5H8.7l11.7 46.5zm61.1-36.3c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3z"/></g>
          </svg>
        </div>
      </div>
    </div>

    <div class="px-6 md:px-20 mt-8">
      <div class="flex flex-col md:flex-row items-start justify-between mb-6 gap-4">
        <div class="flex flex-wrap gap-4">
          <button
            @click="addBrandtemButton"
            class="itbms-add-button border-2 rounded-full px-6 py-2 cursor-pointer transition-colors duration-300 font-semibold"
            :class="theme === 'dark' ? 'bg-green-500 text-white border-green-500 hover:bg-transparent hover:text-green-500' : 'bg-green-500 text-white border-green-500 hover:bg-transparent hover:text-green-500'"
          >
            + Add Brand
          </button>
          <button
            @click="goToSaleItemsList"
            class="itbms-item-list border-2 rounded-full px-6 py-2 cursor-pointer transition-colors duration-300 font-semibold"
            :class="theme === 'dark' ? 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500' : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500'"
          >
            Sale Item List
          </button>
        </div>
        <div class="flex items-center space-x-4">
          <div class="bg-gray-200 text-sm leading-none border-2 border-gray-200 rounded-full inline-flex" :class="theme === 'dark' ? 'bg-gray-700 text-gray-300 border-gray-700' : 'bg-gray-200 text-gray-500 border-gray-200'">
  <button
    :class="[
      'inline-flex items-center transition-colors duration-300 ease-in focus:outline-none rounded-l-full px-4 py-2',
      { 'hover:text-gray-900 focus:text-gray-900': theme === 'light', 'hover:text-white focus:text-white': theme === 'dark' },
      { 'bg-orange-500 text-white': viewMode === 'grid' }
    ]"
    id="grid"
    @click="setViewMode('grid')"
  >
    <svg xmlns="http://www.w3.org/2000/svg" class="fill-current w-4 h-4 mr-2" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
    <span>Grid</span>
  </button>
  <button
    :class="[
      'itbms-manage-brand inline-flex items-center transition-colors duration-300 ease-in focus:outline-none rounded-r-full px-4 py-2',
      { 'hover:text-gray-900 focus:text-gray-900': theme === 'light', 'hover:text-white focus:text-white': theme === 'dark' },
      { 'bg-orange-500 text-white': viewMode === 'list' }
    ]"
    id="list"
    @click="setViewMode('list')"
  >
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="fill-current w-4 h-4 mr-2"><line x1="8" y1="6" x2="21" y2="6"></line><line x1="8" y1="12" x2="21" y2="12"></line><line x1="8" y1="18" x2="21" y2="18"></line><line x1="3" y1="6" x2="3.01" y2="6"></line><line x1="3" y1="12" x2="3.01" y2="12"></line><line x1="3" y1="18" x2="3.01" y2="18"></line></svg>
    <span>List</span>
  </button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="px-6 md:px-20 mb-6 flex items-center gap-3">
      <label for="page-size" class="text-sm font-medium">Items per page:</label>
      <select
        id="page-size"
        v-model="pageSize"
        class="itbms-page-size border-2 rounded-full px-3 py-1 text-sm font-semibold shadow-sm transition duration-200 focus:outline-none focus:ring-2"
        :class="theme === 'dark' ? 'bg-gray-800 text-gray-300 border-gray-700 focus:ring-orange-500 hover:bg-gray-700' : 'bg-white text-gray-800 border-gray-300 focus:ring-blue-400 hover:bg-gray-100'"
      >
        <option :value="5">5</option>
        <option :value="10">10</option>
        <option :value="20">20</option>
      </select>
    </div>

    <div class="px-6 md:px-20">
      <div v-if="viewMode === 'grid'" class="p-6">
        <div v-if="filteredAndSortedItems.length === 0" class="text-center">No brand found.</div>
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <div
            v-for="(item, index) in paginatedItems"
            :key="item.id"
            class="itbms-row rounded-3xl p-6 shadow-lg transition-all duration-300 transform hover:scale-105 hover:shadow-xl cursor-pointer"
            :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-gray-100 text-gray-950'"
            :style="{ animationDelay: (index * 50) + 'ms' }"
          >
            <img :src="`/sy4/logobrands/${item.id}.png`" alt="brand" class="w-full h-40 object-contain mb-4 rounded-xl" />
            <div class="itbms-brand font-bold text-lg text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-pink-500 text-center">{{ item.brandName }}</div>
          </div>
        </div>
      </div>

      <div v-else class="p-6">
        <div v-if="filteredAndSortedItems.length === 0" class="text-center">No brand found.</div>
        <table v-else class="w-full text-sm text-left rounded-xl overflow-hidden shadow-lg">
          <thead :class="theme === 'dark' ? 'bg-gray-800 text-gray-300' : 'bg-gray-200 text-gray-800'">
            <tr>
              <th class="px-4 py-3">Id</th>
              <th class="px-4 py-3">Name</th>
              <th class="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in paginatedItems"
              :key="item.id"
              class="itbms-row transition-colors duration-200"
              :class="theme === 'dark' ? 'bg-gray-900 text-gray-200 hover:bg-gray-800' : 'bg-white text-gray-800 hover:bg-gray-100'"
            >
              <td class="px-4 py-3">{{ item.id }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center space-x-3">
                  <img
                    :src="`/sy4/logobrands/${item.id}.png`"
                    alt="brand"
                    class="w-10 h-10 object-contain rounded-xl"
                  />
                  <span class="itbms-name">{{ item.brandName }}</span>
                </div>
              </td>
              <td class="px-4 py-3 space-x-2">
                <button
                  @click="router.push(`/brands/${item.id}/edit`)"
                  class="itbms-edit-button font-semibold border-2 rounded-full px-4 py-1 transition-colors duration-300"
                  :class="theme === 'dark' ? 'bg-yellow-500 text-white border-yellow-500 hover:bg-transparent hover:text-yellow-500' : 'bg-yellow-500 text-white border-yellow-500 hover:bg-transparent hover:text-yellow-500'"
                >
                  Edit
                </button>
                <button
                  @click.stop="deletebrand(item)"
                  class="itbms-delete-button font-semibold border-2 rounded-full px-4 py-1 transition-colors duration-300"
                  :class="theme === 'dark' ? 'bg-red-500 text-white border-red-500 hover:bg-transparent hover:text-red-500' : 'bg-red-500 text-white border-red-500 hover:bg-transparent hover:text-red-500'"
                >
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div :class="totalPages === 1 ? 'invisible' : 'visible'" class="flex justify-center mt-6 flex-wrap gap-2 px-6 md:px-20 mb-8 overflow-hidden">
      <button
        @click="() => { lastAction = 'first'; currentPage = 1 }"
        :disabled="currentPage === 1"
        class="itbms-page-first rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === 1
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        &lt;&lt; First
      </button>

      <button
        @click="() => { lastAction = 'prev'; currentPage-- }"
        :disabled="currentPage === 1"
        class="itbms-page-prev rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === 1
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        &lt; Prev
      </button>

      <button
        v-for="page in visiblePages"
        :key="'page-' + page"
        @click="() => { lastAction = ''; currentPage = page }"
        class="px-4 py-2 rounded-full text-sm font-semibold transition-all duration-300 shadow-sm"
        :class="[
          currentPage === page
            ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-lg'
            : theme === 'dark'
            ? 'bg-gray-800 text-gray-300 hover:bg-gray-700'
            : 'bg-white text-gray-800 hover:bg-gray-100'
        ]"
      >
        {{ page }}
      </button>

      <button
        @click="() => { lastAction = 'next'; currentPage++ }"
        :disabled="currentPage === totalPages"
        class="itbms-page-next rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === totalPages
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        Next &gt;
      </button>

      <button
        @click="() => { lastAction = 'last'; currentPage = totalPages }"
        :disabled="currentPage === totalPages"
        class="itbms-page-last rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === totalPages
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        Last &gt;&gt;
      </button>
    </div>

    <transition name="bounce-popup">
      <div v-if="showDeleteConfirmationPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Confirm delete the brand</h2>
          <p class="itbms-message mb-4">{{ deleteMessage }}</p>
          <div class="flex justify-center gap-4">
            <button v-if="!hasRelatedSaleItemsOnPopup" @click="confirmDelete" class="itbms-confirm-button bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Yes</button>
            <button @click="cancelDeleteItem" class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold">No</button>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade-background">
      <div v-if="isDeleting" class="fixed top-0 left-0 w-full h-full bg-black flex items-center justify-center z-50 loading-overlay">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z"/>
          </svg>
          <p class="itbms-message text-sm font-medium">Deleting brand...</p>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showNotFoundPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">⚠️ Item not found.</h2>
          <p class="itbms-message mb-2">An error has occurred, the brand does not exist.</p>
          <p class="text-sm text-gray-500">Bring You Back in {{ countdown }} second<span v-if="countdown > 1">s</span>...</p>
        </div>
      </div>
    </transition>
    
    <transition name="bounce-popup">
      <div v-if="showAddSuccessPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The brand has been successfully added!</p>
          <button @click="closeSuccessPopup" class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>
    
    <transition name="bounce-popup">
      <div v-if="showDeleteSuccessPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The brand has been successfully deleted!</p>
          <button @click="closeSuccessPopup" class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showEditSuccessPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="ext-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The brand has been successfully updated!</p>
          <button @click="closeSuccessPopup" class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showEditFailPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Error 500!</h2>
          <p class="itbms-message mb-4">The brand has been fail to updated!</p>
          <button @click="closeSuccessPopup" class="bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showfailPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl text-red-500 font-semibold mb-4">Error 500!</h2>
          <p class="itbms-message mb-4">The status could not be added.</p>
          <button @click="closeSuccessPopup" class="bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showcannotDeletePopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl text-red-500 font-semibold mb-4">Delete this brand is not allow!</h2>
          <p class="itbms-message mb-4">There are sale items with this brand.</p>
          <button @click="closeSuccessPopup" class="bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>
    
    <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
    </button>
  </div>
</template>

<style scoped>
.active {
  background: white;
  border-radius: 9999px;
  color: #1D232A;
}
.dark .active {
  background: #374151; /* Darker shade for active button in dark mode */
  color: white;
}
.itbms-brands-page.dark {
  color: #f3f4f6; /* light gray for text */
}
.itbms-brands-page.light {
  color: #1f2937; /* dark gray for text */
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

.itbms-bg {
  background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent black */
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

.fade-background-enter-from {
  background-color: rgba(0, 0, 0, 0); 
}

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
