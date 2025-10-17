<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems } from '@/libs/fetchUtilsOur';
import { deleteItemById } from '@/libs/fetchUtilsOur'
import { theme } from '@/stores/themeStore.js' // 1. ดึง theme จาก store

// --- Script ทั้งหมดเหมือนเดิม ไม่มีการเปลี่ยนแปลง ---

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

const router = useRouter()
const route = useRoute()
const items = ref([])
const saleItems = ref([])
const searchQuery = ref('')
const filterBy = ref('')
const viewMode = ref('grid') 

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

onMounted(async () => {
  try {
    const data = await getItems(`${import.meta.env.VITE_BACKEND}/v1/brands`)
    items.value = data.sort((a, b) => a.id - b.id)
    const saleItemsData = await getItems(`${import.meta.env.VITE_BACKEND}/v2/sale-items`)
    saleItems.value = saleItemsData
  } catch (err) {
    console.error('Error loading items:', err)
  }
})

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
    const statusCode = await deleteItemById(`${import.meta.env.VITE_BACKEND}/v1/brands`, deleteId.value);
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
  
// watch(currentPage, (newPage) => {
//   router.replace({ query: { ...route.query, page: newPage } })
// })
  
watch([pageSize, searchQuery], () => {
  currentPage.value = 1
})
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans transition-colors duration-500">
    
    <main class="px-8 md:px-24 py-16">
      <section class="text-center mb-12 animate-fade-in-up">
        <h1 class="text-4xl md:text-5xl font-bold">
          Manage
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">Brands</span>
        </h1>
        <p class="mt-4 text-lg max-w-2xl mx-auto" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
          Add, edit, or remove brands from your collection.
        </p>
      </section>

      <section class="mb-10 animate-fade-in-up" style="animation-delay: 0.2s;">
        <div class="flex flex-col md:flex-row gap-4 justify-between items-center">
          <div class="flex flex-col sm:flex-row gap-4 w-full md:w-auto">
            <div class="relative w-full sm:w-80">
              <svg class="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5" :class="theme === 'dark' ? 'text-gray-500' : 'text-gray-400'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" /></svg>
              <input 
                type="text" 
                placeholder="Search brand..." 
                v-model="searchQuery"
                class="w-full pl-11 pr-4 py-3 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" 
                :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'"
              />
            </div>
            <button @click="addBrandtemButton" class="flex items-center justify-center gap-2 w-full sm:w-auto px-6 py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:scale-105">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" /></svg>
              Add Brand
            </button>
          </div>

          <div class="flex items-center gap-4">
            <button @click="goToSaleItemsList" class="px-6 py-3 font-semibold rounded-full transition-all duration-300" :class="theme === 'dark' ? 'bg-white/10 text-white hover:bg-white/20' : 'bg-black/5 text-black hover:bg-black/10'">
              Sale Items
            </button>

            <div class="flex items-center p-1 rounded-full" :class="theme === 'dark' ? 'bg-white/10' : 'bg-black/5'">
              <button @click="setViewMode('grid')" class="p-2 rounded-full transition-all duration-300" :class="{'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-md': viewMode === 'grid', 'text-gray-500 hover:text-white': theme === 'dark' && viewMode !== 'grid', 'text-gray-500 hover:text-black': theme !== 'dark' && viewMode !== 'grid'}">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" /></svg>
              </button>
              <button @click="setViewMode('list')" class="p-2 rounded-full transition-all duration-300" :class="{'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-md': viewMode === 'list', 'text-gray-500 hover:text-white': theme === 'dark' && viewMode !== 'list', 'text-gray-500 hover:text-black': theme !== 'dark' && viewMode !== 'list'}">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" /></svg>
              </button>
            </div>
          </div>
        </div>
      </section>

      <div v-if="filteredAndSortedItems.length === 0" class="text-center py-20 text-gray-500 animate-fade-in-up">
        <p class="text-xl">No brands found.</p>
        <p>Try adjusting your search or add a new brand.</p>
      </div>

      <div v-else>
        <div v-if="viewMode === 'grid'" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <div
            v-for="(item, index) in paginatedItems"
            :key="item.id"
            class="group relative text-center p-6 rounded-3xl transition-all duration-300 cursor-pointer animate-fade-in-up"
            :class="theme === 'dark' ? 'bg-white/5 hover:bg-white/10' : 'bg-black/5 hover:bg-black/10'"
            :style="{ animationDelay: (index % pageSize * 50) + 'ms' }"
          >
            <img :src="`/sy4/logobrands/${item.id}.png`" alt="brand" class="w-full h-32 object-contain mb-4 transition-transform duration-300 group-hover:scale-110" />
            <div class="font-bold text-lg text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">{{ item.brandName }}</div>
          </div>
        </div>

        <div v-else class="space-y-3">
          <div class="hidden md:grid grid-cols-12 gap-4 px-6 text-xs font-semibold uppercase tracking-wider" :class="theme === 'dark' ? 'text-gray-500' : 'text-gray-400'">
            <div class="col-span-1">ID</div>
            <div class="col-span-8">Brand</div>
            <div class="col-span-3 text-right">Actions</div>
          </div>
          <div
            v-for="(item, index) in paginatedItems"
            :key="item.id"
            class="grid grid-cols-1 md:grid-cols-12 gap-4 items-center p-4 rounded-2xl transition-all duration-300 animate-fade-in-up"
            :class="theme === 'dark' ? 'bg-white/5 hover:bg-white/10' : 'bg-black/5 hover:bg-black/10'"
            :style="{ animationDelay: (index % pageSize * 50) + 'ms' }"
          >
            <div class="md:col-span-1 text-sm font-bold" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">#{{ item.id }}</div>
            <div class="md:col-span-8 flex items-center gap-4">
              <img :src="`/sy4/logobrands/${item.id}.png`" alt="brand" class="w-10 h-10 object-contain rounded-lg p-1" :class="theme === 'dark' ? 'bg-black/20' : 'bg-white'" />
              <span class="font-semibold text-base">{{ item.brandName }}</span>
            </div>
            <div class="md:col-span-3 flex justify-start md:justify-end items-center gap-2">
              <button @click="router.push(`/brands/${item.id}/edit`)" class="px-5 py-2 text-sm font-semibold rounded-full transition-colors duration-300" :class="theme === 'dark' ? 'bg-yellow-500/10 text-yellow-400 hover:bg-yellow-500/20' : 'bg-yellow-500/10 text-yellow-600 hover:bg-yellow-500/20'">Edit</button>
              <button @click.stop="deletebrand(item)" class="px-5 py-2 text-sm font-semibold rounded-full transition-colors duration-300" :class="theme === 'dark' ? 'bg-red-500/10 text-red-400 hover:bg-red-500/20' : 'bg-red-500/10 text-red-600 hover:bg-red-500/20'">Delete</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="flex justify-center mt-12 flex-wrap gap-2 animate-fade-in-up">
        <button
          v-for="page in visiblePages"
          :key="'page-' + page"
          @click="() => { lastAction = ''; currentPage = page }"
          class="px-4 py-2 rounded-full text-sm font-semibold transition-all duration-300"
          :class="[
            currentPage === page
              ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg'
              : theme === 'dark'
              ? 'bg-white/10 text-gray-300 hover:bg-white/20'
              : 'bg-black/5 text-gray-800 hover:bg-black/10'
          ]"
        >
          {{ page }}
        </button>
      </div>

    </main>

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
          <h2 class="text-xl font-semibold mb-4">The brand has been fail to Edit!</h2>
          <p class="itbms-message mb-4">Please try again later.</p>
          <button @click="closeSuccessPopup" class="bg-red-500 text-white border-2 border-red-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-red-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showfailPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl text-red-500 font-semibold mb-4">The brand has been fail to added!</h2>
          <p class="itbms-message mb-4">Please try again later.</p>
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

  </div>
</template>

<style scoped>
/* Keyframes from landing page for consistency */
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

.animate-fade-in-up {
  animation: fade-in-up 0.6s ease-out forwards;
}

/* Transitions for popups (unchanged) */
.itbms-bg {
  background-color: rgba(0, 0, 0, 0.5);
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
  transition: all 0.3s ease;
}

.fade-background-enter-from,
.fade-background-leave-to {
  opacity: 0;
}
</style>