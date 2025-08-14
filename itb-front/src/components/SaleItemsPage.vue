<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems, deleteItemById } from '@/libs/fetchUtilsOur';

const router = useRouter()
const route = useRoute()

const showAddSuccessPopup = ref(false)
const showDeleteSuccessPopup = ref(false)
const showfailPopup = ref(false)
const isGridView = computed(() => route.path !== '/sale-items/list')

const showBrandFilterModal = ref(false)
const showPriceFilterModal = ref(false)
const showStorageFilterModal = ref(false)
const items = ref([])
const totalPages = ref(0)
const brandList = ref([])
const savedPageSize = sessionStorage.getItem('pageSize')
const savedPage = parseInt(sessionStorage.getItem('currentPage'))
const currentPage = ref(!isNaN(savedPage) ? savedPage : 0)
const pageSize = ref(savedPageSize ? parseInt(savedPageSize) : 10)

// Price
const displayedPrice = computed(() => {
  if (priceLower.value != null && priceUpper.value != null) {
    return `${priceLower.value.toLocaleString()} – ${priceUpper.value.toLocaleString()} Baht`
  } else if (priceLower.value != null) {
    return `= ${priceLower.value.toLocaleString()} Baht`
  } else if (priceUpper.value != null) {
    return `≤ ${priceUpper.value.toLocaleString()} Baht`
  }
  return ''
})

const priceLower = ref(
  JSON.parse(sessionStorage.getItem('priceLower') || 'null') ??
  (route.query.filterPriceLower ? Number(route.query.filterPriceLower) : null)
)
const priceUpper = ref(
  JSON.parse(sessionStorage.getItem('priceUpper') || 'null') ??
  (route.query.filterPriceUpper ? Number(route.query.filterPriceUpper) : null)
)
const priceRanges = [
  { min: 0, max: 5000, label: "0 – 5,000 Baht" },
  { min: 5001, max: 10000, label: "5,001 – 10,000 Baht" },
  { min: 10001, max: 20000, label: "10,001 – 20,000 Baht" },
  { min: 20001, max: 30000, label: "20,001 – 30,000 Baht" },
  { min: 30001, max: 40000, label: "30,001 – 40,000 Baht" },
  { min: 40001, max: 50000, label: "40,001 – 50,000 Baht" }
]


// Storage
const selectedStorages = ref(
  JSON.parse(sessionStorage.getItem('selectedStorages') || 'null') ??
  (route.query.filterStorages ? [].concat(route.query.filterStorages) : [])
)

const searchQuery = ref(route.query.search || '')
const selectedBrands = ref(
  JSON.parse(sessionStorage.getItem('selectedBrands') || 'null') ??
  (route.query.filterBrands ? [].concat(route.query.filterBrands) : [])
)

const savedSort = sessionStorage.getItem('sortOrder')
const currentSortOrder = ref(savedSort || 'createdOn')

const availableStorages = ref([])

const availableBrands = computed(() => {
  return brandList.value
    .map(b => b.brandName)
    .filter(name => !!name)
});

// Theme Logic
const theme = ref('light') // ตั้งค่าเริ่มต้นให้เป็น light เพื่อให้สอดคล้องกับ landing page ที่ไม่มีการบันทึก theme
const themeClass = computed(() => {
  return theme.value === 'dark' ? 'bg-gray-950 text-white' : 'bg-white text-gray-950'
})

const toggleTheme = () => {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  document.body.classList.toggle('dark', theme.value === 'dark')
  localStorage.setItem('theme', theme.value)
}

const iconComponent = computed(() => {
  return theme.value === 'dark'
    ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>` // sun icon
    : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-800" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>` // moon icon
})

// Fetch
async function fetchItems() {
  try {
    let lower = priceLower.value != null ? priceLower.value : undefined
    let upper = priceUpper.value != null ? priceUpper.value : undefined

    if (lower != null && upper == null) {
      upper = lower
    }

    const response = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/sale-items', {
      params: {
        filterBrands: selectedBrands.value.length ? selectedBrands.value : undefined,
        filterStorages: selectedStorages.value.length ? selectedStorages.value : undefined,
        filterPriceLower: lower,
        filterPriceUpper: upper,
        page: currentPage.value,
        size: pageSize.value,
        sortField: currentSortOrder.value === 'createdOn' ? 'id' : 'brand.name',
        sortDirection:
          currentSortOrder.value === 'brandAsc'
            ? 'asc'
            : currentSortOrder.value === 'brandDesc'
              ? 'desc'
              : 'asc',
      },
    })

    items.value = response.content
    totalPages.value = response.totalPages
  } catch (err) {
    console.error('Fetch error:', err)
  }
}

async function fetchbrand() {
  try {
    const data = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands')
    brandList.value = data.sort((a, b) => {
      const brandA = a.brandName ? a.brandName.toLowerCase() : ''
      const brandB = b.brandName ? b.brandName.toLowerCase() : ''

      if (brandA < brandB) {
        return -1
      }
      if (brandA > brandB) {
        return 1
      }
      return 0
    });
  } catch (err) {
    console.error('Error loading items:', err)
  }
}

async function fetchStorage() {
  try {
    const response = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/sale-items', {
      params: {
        page: 0,
        size: 9999
      }
    })

    const allStorages = response.content
      .map(item => item.storageGb)
      .filter(s => s != null)

    availableStorages.value = [...new Set(allStorages)]
  } catch (err) {
    console.error('Error fetching storages:', err)
  }
}

watch(
  [selectedBrands, selectedStorages, searchQuery],
  () => {
    currentPage.value = 0
    lastAction.value = ''
    fetchItems()
  }
)

watch(currentPage, (newPage) => {
  sessionStorage.setItem('currentPage', newPage)
})

watch(pageSize, (newSize) => {
  sessionStorage.setItem('pageSize', newSize)
})

watch(selectedBrands, (newVal) => {
  sessionStorage.setItem('selectedBrands', JSON.stringify(newVal))
})

watch(currentSortOrder, (val) => {
  sessionStorage.setItem('sortOrder', val)
})

watch(selectedBrands, val => sessionStorage.setItem('selectedBrands', JSON.stringify(val)))
watch(priceLower, val => sessionStorage.setItem('priceLower', JSON.stringify(val)))
watch(priceUpper, val => sessionStorage.setItem('priceUpper', JSON.stringify(val)))
watch(selectedStorages, val => sessionStorage.setItem('selectedStorages', JSON.stringify(val)))

const fixedStart = ref(0)
const lastAction = ref('')
const groupSize = 10
const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const action = lastAction.value

  let start, end

  if (!fixedStart.value) {
    fixedStart.value = 1
  }

  const currentPage1Based = current + 1

  if (action === 'next') {
    const endOfGroup = fixedStart.value + groupSize - 1
    if (currentPage1Based > endOfGroup) {
      end = Math.min(currentPage1Based, total)
      start = Math.max(end - groupSize + 1, 1)
      fixedStart.value = start
    }
  }

  if (action === 'prev') {
    if (currentPage1Based < fixedStart.value) {
      start = Math.max(currentPage1Based, 1)
      fixedStart.value = start
    }
  }

  if (action === 'first') {
    fixedStart.value = 1
  }

  if (action === 'last') {
    start = Math.max(total - groupSize + 1, 1)
    fixedStart.value = start
  }

  start = fixedStart.value
  end = Math.min(start + groupSize - 1, total)

  const pages = []
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})


// Pagination
function goToPage(page) {
  currentPage.value = page
  fetchItems()
}

//edit
const goToEditItem = (id) => {
  router.push(`/sale-items/${id}/edit`)
}

// Sort
function sortBrandAscending() {
  currentSortOrder.value = 'brandAsc'
  currentPage.value = 0
  fetchItems()
}
function sortBrandDescending() {
  currentSortOrder.value = 'brandDesc'
  currentPage.value = 0
  fetchItems()
}
function clearBrandSorting() {
  currentSortOrder.value = 'createdOn'
  currentPage.value = 0
  fetchItems()
}

watch(pageSize, () => {
  currentPage.value = 0
  fixedStart.value = 0
  fetchItems()
})

// Filter
// Brand filter function
function toggleBrandFilterModal() {
  showBrandFilterModal.value = !showBrandFilterModal.value
}
function removeBrandFromFilter(brand) {
  selectedBrands.value = selectedBrands.value.filter(b => b !== brand)
  currentPage.value = 0
  fetchItems()
}
function clearBrandFilter() {
  selectedBrands.value = []
  currentPage.value = 0
  fetchItems()
}
// Price filter function

function selectPriceRange(range) {
  priceLower.value = range.min
  priceUpper.value = range.max
  savePriceRange()
}

function savePriceRange() {
  sessionStorage.setItem('priceLower', JSON.stringify(priceLower.value))
  sessionStorage.setItem('priceUpper', JSON.stringify(priceUpper.value))
  fetchItems()
  showPriceFilterModal.value = false
}

function togglePriceFilterModal() {
  showPriceFilterModal.value = !showPriceFilterModal.value
}
function clearPriceFilter() {
  priceLower.value = null
  priceUpper.value = null
  sessionStorage.removeItem('priceLower')
  sessionStorage.removeItem('priceUpper')
  currentPage.value = 0
  fetchItems()
}
// storage filter function
function toggleStorageFilterModal() {
  showStorageFilterModal.value = !showStorageFilterModal.value
}
function removeStorageFromFilter(storage) {
  selectedStorages.value = selectedStorages.value.filter(s => s !== storage)
  currentPage.value = 0
  fetchItems()
}
function clearStorageFilter() {
  selectedStorages.value = []
  currentPage.value = 0
  fetchItems()
}

// clear all filter
function clearAllFilters() {
  selectedBrands.value = []
  selectedStorages.value = []
  priceLower.value = null
  priceUpper.value = null
  sessionStorage.removeItem('priceLower')
  sessionStorage.removeItem('priceUpper')
  currentPage.value = 0
  fetchItems()
}

// Navigation
function addSaleItemButton() {
  router.push('/sale-items/add')
}
function goToManageBrand() {
  router.push('/brands')
}
const goToPhoneDetails = (id) => {
  router.push(`/sale-items/${id}`)
}

// Watch fetch trigger
watch([searchQuery, selectedBrands, currentSortOrder, currentPage], fetchItems, { immediate: true })

onMounted(() => {
  fetchItems()
  fetchbrand()
  fetchStorage()

  // Load theme from localStorage on component mount
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    theme.value = savedTheme
  }
  document.body.classList.toggle('dark', theme.value === 'dark')

  window.addEventListener('storage', (event) => {
    if (event.key === 'theme') {
       theme.value = event.newValue;
    }
  })
})

// --- Delete Item Functions ---
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
      setTimeout(async () => {
        isDeleting.value = false
        router.push({ path: route.path, query: { deleteSuccess: 'true' } })
      }, 1000)
    } else if (statusCode === 404) {
      isDeleting.value = false;
      setTimeout(() => {
        router.push(route.path);
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

// --- Popup Close Function ---
const closeSuccessPopup = async () => {
  showAddSuccessPopup.value = false
  showDeleteSuccessPopup.value = false
  showfailPopup.value = false
  router.replace({ path: route.path, query: {} });
  await fetchItems();
}

// Watchers for URL query parameters (for popups)
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
</script>

<template>
  <div :class="themeClass" class="itbms-sale-items-page min-h-screen font-sans overflow-hidden">

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

        <div class="flex items-center border rounded-full overflow-hidden w-full max-w-2xl">
          <!-- Brand Filter -->
          <div class="itbms-brand-filter"
            :class="theme === 'dark' ? 'border-gray-700 bg-gray-800' : 'border-gray-300 bg-white'">
            <div class="flex flex-grow flex-wrap items-center gap-1 p-2 min-h-[40px] max-h-[40px] overflow-y-auto">
              <span v-for="brand in selectedBrands" :key="brand"
                class="itbms-brand-item text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center whitespace-nowrap"
                :class="theme === 'dark' ? 'bg-blue-800 text-blue-100' : 'bg-blue-100 text-blue-800'">
                {{ brand }}
                <button @click.stop="removeBrandFromFilter(brand)"
                  class="itbms-brand-item-clear ml-1 focus:outline-none"
                  :class="theme === 'dark' ? 'text-blue-200 hover:text-white' : 'text-blue-800 hover:text-blue-600'">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clip-rule="evenodd" />
                  </svg>
                </button>
              </span>
              <input type="text" :placeholder="selectedBrands.length === 0 ? 'Filter by brand(s)' : ''" readonly
                class="itbms-brand-filter-input h-full w-full px-3 py-0 flex-grow focus:outline-none cursor-pointer"
                :class="theme === 'dark' ? 'bg-gray-800 text-white placeholder-gray-400' : 'bg-white text-gray-950 placeholder-gray-500'"
                @click="toggleBrandFilterModal" />
            </div>
          </div>

          <!-- Separator -->
          <div class="border-l border-gray-300 h-6"></div>

        <!-- Price Filter -->
          <div class="itbms-price-filter"
            :class="theme === 'dark' ? 'border-gray-700 bg-gray-800' : 'border-gray-300 bg-white'">
            <div class="flex flex-grow flex-wrap items-center gap-1 p-2 min-h-[40px] max-h-[40px] overflow-y-auto">

              <span v-if="displayedPrice"
                class="itbms-price-item text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center whitespace-nowrap"
                :class="theme === 'dark' ? 'bg-blue-800 text-blue-100' : 'bg-blue-100 text-blue-800'">
                {{ displayedPrice }}
                <button @click.stop="clearPriceFilter()" class="itbms-price-item-clear ml-1 focus:outline-none"
                  :class="theme === 'dark' ? 'text-blue-200 hover:text-white' : 'text-blue-800 hover:text-blue-600'">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clip-rule="evenodd" />
                  </svg>
                </button>
              </span>

              <input type="text" :placeholder="!displayedPrice ? 'Filter by price' : ''" readonly
                class="itbms-price-item-input h-full w-full px-3 py-0 flex-grow focus:outline-none cursor-pointer"
                :class="theme === 'dark' ? 'bg-gray-800 text-white placeholder-gray-400' : 'bg-white text-gray-950 placeholder-gray-500'"
                @click="togglePriceFilterModal()" />
            </div>
          </div>

          <!-- Separator -->
          <div class="border-l border-gray-300 h-6"></div>

          <!-- Storage Filter -->
          <div class="itbms-storage-filter"
            :class="theme === 'dark' ? 'border-gray-700 bg-gray-800' : 'border-gray-300 bg-white'">
            <div class="flex flex-grow flex-wrap items-center gap-1 p-2 min-h-[40px] max-h-[40px] overflow-y-auto">
              <span v-for="storage in selectedStorages" :key="storage"
                class="itbms-storage-item text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center whitespace-nowrap"
                :class="theme === 'dark' ? 'bg-blue-800 text-blue-100' : 'bg-blue-100 text-blue-800'">
                {{ storage }}
                <button @click.stop="removeStorageFromFilter(storage)"
                  class="itbms-storage-item-clear ml-1 focus:outline-none"
                  :class="theme === 'dark' ? 'text-blue-200 hover:text-white' : 'text-blue-800 hover:text-blue-600'">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clip-rule="evenodd" />
                  </svg>
                </button>
              </span>
              <input type="text" :placeholder="selectedStorages.length === 0 ? 'Filter by storage(s)' : ''" readonly
                class="itbms-storage-filter-input h-full w-full px-3 py-0 flex-grow focus:outline-none cursor-pointer"
                :class="theme === 'dark' ? 'bg-gray-800 text-white placeholder-gray-400' : 'bg-white text-gray-950 placeholder-gray-500'"
                @click="toggleStorageFilterModal" />
            </div>
          </div>
          <button @click="clearAllFilters()"
          class="itbms-brand-filter-clear text-white p-2 rounded-r-full transition-colors duration-300 border-2 border-red-500 hover:bg-red-600"
          :class="theme === 'dark' ? 'bg-red-500 hover:text-white' : 'bg-red-500 hover:text-white'">
          Clear
        </button>
        </div>
      </div>

      <div class="itbms-icons flex flex-col items-end space-y-2">
        <div class="flex items-center space-x-4">
          <!-- <div @click="toggleTheme" class="itbms-theme-toggle cursor-pointer p-2 rounded-full transition-colors duration-300" :class="theme === 'dark' ? 'bg-gray-700 hover:bg-gray-600' : 'bg-gray-200 hover:bg-gray-300'" v-html="iconComponent">
          </div> -->

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
            class="itbms-sale-item-add border-2 rounded-full px-6 py-2 cursor-pointer transition-colors duration-300 font-semibold"
            :class="theme === 'dark' ? 'bg-green-500 text-white border-green-500 hover:bg-transparent hover:text-green-500' : 'bg-green-500 text-white border-green-500 hover:bg-transparent hover:text-green-500'"
            @click="addSaleItemButton"
          >
            + Add Sell Item
          </button>

          <button
            v-if="!isGridView"
            @click="goToManageBrand"
            class="itbms-manage-brand border-2 rounded-full px-6 py-2 cursor-pointer transition-colors duration-300 font-semibold"
            :class="theme === 'dark' ? 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500' : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500'"
          >
            Manage Brand
          </button>
        </div>

        <div class="flex items-center space-x-4">
          <button
            v-if="isGridView"
            @click="sortBrandAscending"
            class="itbms-brand-asc border-2 rounded-full px-4 py-2 cursor-pointer transition-colors duration-300"
            :class="[theme === 'dark' ? 'text-gray-300 border-gray-700 hover:bg-gray-700' : 'text-gray-600 border-gray-300 hover:bg-gray-100', currentSortOrder === 'brandAsc' ? 'bg-blue-500 text-white border-blue-500' : '']"
            title="Sort by Brand Ascending"
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 4.5h14.25M3 9h9.75M3 13.5h9.75m4.5-4.5v9.75m0 0-3-3m3 3 3-3" />
            </svg>
            <span class="sr-only">Sort by Brand Ascending</span>
          </button>

          <button
            v-if="isGridView"
            @click="sortBrandDescending"
            class="itbms-brand-desc border-2 rounded-full px-4 py-2 cursor-pointer transition-colors duration-300"
            :class="[theme === 'dark' ? 'text-gray-300 border-gray-700 hover:bg-gray-700' : 'text-gray-600 border-gray-300 hover:bg-gray-100', currentSortOrder === 'brandDesc' ? 'bg-blue-500 text-white border-blue-500' : '']"
            title="Sort by Brand Descending"
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 4.5h14.25M3 9h9.75M3 13.5h9.75m4.5 4.5V8.25m0 0-3 3m-3-3-3 3" />
            </svg>
            <span class="sr-only">Sort by Brand Descending</span>
          </button>

          <button
            v-if="isGridView"
            @click="clearBrandSorting"
            class="itbms-brand-none border-2 rounded-full px-4 py-2 cursor-pointer transition-colors duration-300"
            :class="[theme === 'dark' ? 'text-gray-300 border-gray-700 hover:bg-gray-700' : 'text-gray-600 border-gray-300 hover:bg-gray-100', currentSortOrder === 'createdOn' ? 'bg-blue-500 text-white border-blue-500' : '']"
            title="Clear Sort (Default: Created On)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5a.75.75 0 0 0-1.5 0v7.5a.75.75 0 0 0 .75.75h4.5a.75.75 0 0 0 0-1.5h-3.75V4.5Z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12c0 5.66 4.62 10.25 10.25 10.25S22.75 17.66 22.75 12A10.25 10.25 0 0 0 12 1.75 10.07 10.07 0 0 0 4.25 4.5" />
            </svg>
            <span class="sr-only">Clear Sort</span>
          </button>
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
      <div v-if="isGridView" class="p-6">
        <div v-if="items && items.length === 0" class="text-center">No sale items found.</div>
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <div
            v-for="(item, index) in items"
            :key="item.id"
            class="itbms-row rounded-3xl p-6 shadow-lg transition-all duration-300 transform hover:scale-105 hover:shadow-xl cursor-pointer"
            :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-gray-100 text-gray-950'"
            :style="{ animationDelay: (index * 50) + 'ms' }"
            @click="goToPhoneDetails(item.id)"
          >
            <img
              :src="'/sy4/phone/iPhone.png'"
              alt="phone"
              class="w-full h-40 object-contain mb-4 rounded-xl"
            />
            <div class="itbms-brand font-bold text-lg text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-pink-500">{{ item.brandName }}</div>
            <div class="itbms-model text-sm font-semibold">{{ item.model }}</div>
            <div class="text-sm">
              <span class="itbms-ramGb">{{ item.ramGb || '-' }}</span>/<span class="itbms-storageGb">{{ item.storageGb || '-' }}</span> GB
            </div>
            <div class="itbms-price mt-2 font-extrabold text-2xl">{{ item.price.toLocaleString() }}</div>
            <div class="itbms-price-unit text-sm font-light opacity-80">Baht</div>
          </div>
        </div>
      </div>

      <div v-else class="p-6">
        <div v-if="items && items.length === 0" class="text-center">No sale items found.</div>
        <table v-else class="w-full text-sm text-left rounded-xl overflow-hidden shadow-lg">
          <thead :class="theme === 'dark' ? 'bg-gray-800 text-gray-300' : 'bg-gray-200 text-gray-800'">
            <tr>
              <th class="px-4 py-3">Id</th>
              <th class="px-4 py-3">Brand</th>
              <th class="px-4 py-3">Model</th>
              <th class="px-4 py-3">Ram</th>
              <th class="px-4 py-3">Storage</th>
              <th class="px-4 py-3">Color</th>
              <th class="px-4 py-3">Price</th>
              <th class="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in items"
              :key="item.id"
              class="itbms-row transition-colors duration-200"
              :class="theme === 'dark' ? 'bg-gray-900 text-gray-200 hover:bg-gray-800' : 'bg-white text-gray-800 hover:bg-gray-100'"
            >
              <td class="px-4 py-3">{{ item.id }}</td>
              <td class="px-4 py-3">{{ item.brandName }}</td>
              <td class="px-4 py-3">{{ item.model }}</td>
              <td class="px-4 py-3">{{ item.ramGb || '-'}}</td>
              <td class="px-4 py-3">{{ item.storageGb || '-' }}</td>
              <td class="px-4 py-3">{{ item.color || '-'}}</td>
              <td class="px-4 py-3">{{ item.price.toLocaleString() }}</td>
              <td class="px-4 py-3 space-x-2">
                <button
                  @click.stop="goToEditItem(item.id)"
                  class="itbms-edit-button font-semibold border-2 rounded-full px-4 py-1 transition-colors duration-300"
                  :class="theme === 'dark' ? 'bg-yellow-500 text-white border-yellow-500 hover:bg-transparent hover:text-yellow-500' : 'bg-yellow-500 text-white border-yellow-500 hover:bg-transparent hover:text-yellow-500'"
                >
                  Edit
                </button>
                <button
                  @click.stop="deleteproduct(item)"
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
        @click="() => { lastAction = 'first'; goToPage(0) }"
        :disabled="currentPage === 0"
        class="itbms-page-first rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === 0
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        &lt;&lt; First
      </button>

      <button
        @click="() => { lastAction = 'prev'; goToPage(currentPage - 1) }"
        :disabled="currentPage === 0"
        class="itbms-page-prev rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === 0
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        &lt; Prev
      </button>

      <button
        v-for="page in visiblePages"
        :key="'page-' + page"
        @click="() => { lastAction = ''; goToPage(page - 1) }"
        class="px-4 py-2 rounded-full text-sm font-semibold transition-all duration-300 shadow-sm"
        :class="[
          currentPage === page - 1
            ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-lg'
            : theme === 'dark'
            ? 'bg-gray-800 text-gray-300 hover:bg-gray-700'
            : 'bg-white text-gray-800 hover:bg-gray-100'
        ]"
      >
        {{ page }}
      </button>

      <button
        @click="() => { lastAction = 'next'; goToPage(currentPage + 1) }"
        :disabled="currentPage === totalPages - 1"
        class="itbms-page-next rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === totalPages - 1
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        Next &gt;
      </button>

      <button
        @click="() => { lastAction = 'last';goToPage(totalPages - 1)}"
        :disabled="currentPage === totalPages - 1"
        class="itbms-page-last rounded-full px-4 py-2 border-2 text-sm transition-colors duration-300 font-semibold"
        :class="[
          currentPage === totalPages - 1
            ? 'bg-gray-700 text-gray-400 border-gray-700 opacity-70 cursor-not-allowed'
            : 'bg-gradient-to-r from-orange-500 to-red-500 text-white border-transparent hover:from-orange-600 hover:to-red-600'
        ]"
      >
        Last &gt;&gt;
      </button>
    </div>

    <transition name="modal-fade">
      <div v-if="showBrandFilterModal" class="itbms-bg fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg w-full max-w-md mx-4" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h3 class="text-xl font-semibold mb-4">Select Brands</h3>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-x-4 gap-y-2 max-h-60 overflow-y-auto border rounded-xl p-2" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'">
            <div v-for="brand in availableBrands" :key="brand" class="flex items-center space-x-2 py-1">
              <input type="checkbox" :id="'brand-' + brand" :value="brand" v-model="selectedBrands" class="form-checkbox h-4 w-4 rounded" :class="theme === 'dark' ? 'text-orange-500 bg-gray-700 border-gray-600' : 'text-blue-600 bg-gray-100 border-gray-300'" />
              <label :for="'brand-' + brand" class="itbms-filter-item">{{ brand }}</label>
            </div>
            <p v-if="availableBrands.length === 0" class="text-center py-4 w-full col-span-full">No brands available.</p>
          </div>
          <div class="mt-6 flex justify-end space-x-2">
            <button @click="showBrandFilterModal = false" class="bg-gray-300 text-gray-800 px-6 py-2 rounded-full hover:bg-gray-400 transition-colors duration-300 font-semibold">Close</button>
            <button @click="clearBrandFilter()" class="bg-red-500 text-white px-6 py-2 rounded-full hover:bg-red-600 transition-colors duration-300 font-semibold">Clear All</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="modal-fade">
      <div v-if="showPriceFilterModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">

        <div class="p-6 rounded-3xl shadow-lg w-full max-w-md mx-4"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">

          <h3 class="text-xl font-semibold mb-4">Select Prices</h3>

          <!-- Predefined Price Options -->
          <div class="space-y-2 mb-4">
            <label v-for="(range, idx) in priceRanges" :key="idx" class="flex items-center space-x-2">
              <input type="checkbox" :checked="priceLower === range.min && priceUpper === range.max"
                @change="selectPriceRange(range)" class="form-checkbox h-4 w-4 rounded text-blue-600" />
              <span>{{ range.label }}</span>
            </label>
          </div>

          <!-- Custom Price Range -->
          <div class="flex items-center gap-2 border-t pt-4 border-gray-300">
            <input type="number" placeholder="Min" v-model.number="priceLower"
              class="border rounded px-3 py-1 w-full" />
            <span>-</span>
            <input type="number" placeholder="Max" v-model.number="priceUpper"
              class="border rounded px-3 py-1 w-full" />
            <button @click="savePriceRange" class="bg-blue-500 text-white px-4 py-1 rounded">Apply</button>
          </div>

          <!-- Buttons -->
          <div class="mt-6 flex justify-end space-x-2">
            <button @click="showPriceFilterModal = false"
              class="bg-gray-300 text-gray-800 px-6 py-2 rounded-full hover:bg-gray-400 font-semibold">
              Close
            </button>
            <button @click="clearPriceFilter"
              class="bg-red-500 text-white px-6 py-2 rounded-full hover:bg-red-600 font-semibold">
              Clear All
            </button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="modal-fade">
      <div v-if="showStorageFilterModal"
        class="itbms-bg fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg w-full max-w-md mx-4"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h3 class="text-xl font-semibold mb-4">Select Storages</h3>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-x-4 gap-y-2 max-h-60 overflow-y-auto border rounded-xl p-2"
            :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'">
            <div v-for="size in availableStorages" :key="size" class="flex items-center space-x-2 py-1">
              <input type="checkbox" :id="'brand-' + size" :value="size" v-model="selectedStorages"
                class="form-checkbox h-4 w-4 rounded"
                :class="theme === 'dark' ? 'text-orange-500 bg-gray-700 border-gray-600' : 'text-blue-600 bg-gray-100 border-gray-300'" />
              <label :for="'size-' + size" class="itbms-storage-item">{{ size }} GB</label>
            </div>
            <p v-if="availableStorages.length === 0" class="text-center py-4 w-full col-span-full">No Storages available.
            </p>
          </div>
          <div class="mt-6 flex justify-end space-x-2">
            <button @click="showStorageFilterModal = false"
              class="bg-gray-300 text-gray-800 px-6 py-2 rounded-full hover:bg-gray-400 transition-colors duration-300 font-semibold">Close</button>
            <button @click="clearStorageFilter()"
              class="bg-red-500 text-white px-6 py-2 rounded-full hover:bg-red-600 transition-colors duration-300 font-semibold">Clear
              All</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
    <div v-if="showDeleteConfirmationPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div class="rounded-3xl p-8 shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
        <h2 class="text-xl font-semibold mb-4">Confirm delete the product</h2>
        <p class="itbms-message mb-4">Do you want to delete this sale item?</p>
        <div class="flex justify-center gap-4">
          <button @click="confirmDelete" class="itbms-confirm-button bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Yes</button>
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
          <p class="itbms-message text-sm font-medium">Deleting product...</p>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showAddSuccessPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The sale item has been successfully added!</p>
          <button @click="closeSuccessPopup" class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showDeleteSuccessPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">The sale item has been successfully deleted!</p>
          <button @click="closeSuccessPopup" class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold">Done</button>
        </div>
      </div>
    </transition>

    <transition name="bounce-popup">
      <div v-if="showfailPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center" :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <h2 class="text-xl font-semibold mb-4">The sale item has been Fail added!</h2>
          <p class="itbms-message mb-4">Please try again later.</p>
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
  background-color: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
}

/* Modal Fade Transition */
.modal-fade-enter-active {
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}

.modal-fade-leave-active {
  transition: opacity 0.2s ease-in, transform 0.2s ease-in;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}


/* Other Popups - Kept as is */
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

/* Animation สำหรับ Fade In/Out ของพื้นหลัง */
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
