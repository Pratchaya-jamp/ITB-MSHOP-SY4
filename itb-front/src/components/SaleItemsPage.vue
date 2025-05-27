<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems, deleteItemById } from '@/libs/fetchUtilsOur';
import Footer from './Footer.vue'

const router = useRouter()
const route = useRoute()


const showAddSuccessPopup = ref(false)
const showDeleteSuccessPopup = ref(false)
const showfailPopup = ref(false)
const isGridView = computed(() => route.path !== '/sale-items/list')

const showBrandFilterModal = ref(false)
const items = ref([])
const totalPages = ref(0)
const brandList = ref([])
const savedPageSize = sessionStorage.getItem('pageSize')
const savedPage = parseInt(sessionStorage.getItem('currentPage'))
const currentPage = ref(!isNaN(savedPage) ? savedPage : 0)
const pageSize = ref(savedPageSize ? parseInt(savedPageSize) : 10)

const searchQuery = ref(route.query.search || '')
const selectedBrands = ref(
  JSON.parse(sessionStorage.getItem('selectedBrands') || 'null') ??
  (route.query.filterBrands ? [].concat(route.query.filterBrands) : [])
)

const savedSort = sessionStorage.getItem('sortOrder')
const currentSortOrder = ref(savedSort || 'createdOn')

const availableBrands = computed(() => {
  return brandList.value
    .map(b => b.brandName)   // ดึงเฉพาะ brandName
    .filter(name => !!name) // กรอง null/undefined
});


// Query parameter sync
function updateQueryParams() {
  router.replace({
    path: route.path
  })
}


// Fetch
async function fetchItems() {
  try {
    const response = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/sale-items', {
      params: {
        filterBrands: selectedBrands.value.length ? selectedBrands.value : undefined,
        page: currentPage.value,
        size: pageSize.value,
        sortField: currentSortOrder.value === 'createdOn' ? 'id' : 'brand.name',
        sortDirection:
          currentSortOrder.value === 'brandAsc'
            ? 'asc'
            : currentSortOrder.value === 'brandDesc'
            ? 'desc'
            : 'asc', // default ถ้าเป็น createdOn ก็ให้ asc
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
  //if (a.brandName < b.brandName) {
    //return -1;
  //}
  //if (a.brandName > b.brandName) {
    //return 1;
  //}
  //return 0;
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

watch(
  [selectedBrands, searchQuery], // หรือ filters อื่นๆ ที่คุณใช้
  () => {
    currentPage.value = 0
    lastAction.value = '' // เพื่อให้ visiblePages รีคอมพิวต์ใหม่
 updateQueryParams()
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

const fixedStart = ref(0)           // ใช้เก็บจุดเริ่มกลุ่ม (0-based)
const lastAction =ref('')
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
  updateQueryParams()
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
  updateQueryParams()
}
function sortBrandDescending() {
  currentSortOrder.value = 'brandDesc'
  currentPage.value = 0
  updateQueryParams()
}
function clearBrandSorting() {
  currentSortOrder.value = 'createdOn'
  currentPage.value = 0
  updateQueryParams()
}

watch(pageSize, () => {
  currentPage.value = 0  // รีเซ็ตไปหน้าแรกเมื่อเปลี่ยนขนาดหน้า
  updateQueryParams()
  fetchItems()
  fixedStart.value = 0
})

// Filter
function toggleBrandFilterModal() {
  showBrandFilterModal.value = !showBrandFilterModal.value
}
function removeBrandFromFilter(brand) {
  selectedBrands.value = selectedBrands.value.filter(b => b !== brand)
  currentPage.value = 0
  updateQueryParams()
}
function clearAllBrandFilters() {
  selectedBrands.value = []
  currentPage.value = 0
  updateQueryParams()
}

// View toggle
function toggleViewMode() {
  isGridView.value = !isGridView.value
  updateQueryParams()
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
        // Refresh items after successful deletion
        router.push({ path: route.path, query: { deleteSuccess: 'true' } })
      }, 1000)
    }else if (statusCode === 404) {
      isDeleting.value = false;
      setTimeout(() => {
        router.push(route.path); // Stay on the current path, no query param needed
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
  router.replace({ path: route.path, query: {} }); // Clear query params
  await fetchItems(); // Re-fetch items to ensure data is up-to-date and triggers computed re-evaluation
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
  <div class="Itbms-sale-items-page bg-white min-h-screen">
    <div class="Itbms-header container mx-auto py-8 flex items-center justify-between">
      <div class="Itbms-logo font-bold text-3xl text-black">ITB MShop</div>
      <div class="flex-grow flex justify-center">
        <div class="Itbms-search-bar flex items-center rounded-md border border-gray-300 focus-within:border-blue-500 w-full max-w-sm">
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

    <div class="ml-[8%] flex flex-col md:flex-row items-start justify-between mb-4 gap-4">
      <button
        class="itbms-sale-item-add bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500"
        @click="addSaleItemButton"
      >
        + Add Sell Item
      </button>

      <div class="itbms-filter-container flex-grow flex justify-center w-full md:w-auto">
        <div class="itbms-brand-filter flex items-center rounded-md border border-gray-300 focus-within:border-blue-500 w-full max-w-sm">
          <div class="flex flex-grow flex-wrap items-center gap-1 p-2 min-h-[40px] max-h-[40px] overflow-y-auto">
            <span v-for="brand in selectedBrands" :key="brand" class="itbms-filter-item bg-blue-100 text-blue-800 text-sm font-medium px-2.5 py-0.5 rounded-full flex items-center whitespace-nowrap">
              {{ brand }}
              <button @click.stop="removeBrandFromFilter(brand)" class="itbms-filter-item-clear ml-1 text-blue-800 hover:text-blue-600 focus:outline-none">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </button>
            </span>
            <input
              type="text"
              :placeholder="selectedBrands.length === 0 ? 'Filter by brand(s)' : ''"
              readonly
              class="itbms-brand-filter-input py-2 px-3 flex-grow focus:outline-none rounded-l-md text-black bg-white cursor-pointer"
              @click="toggleBrandFilterModal"
            />
          </div>
          <button @click="toggleBrandFilterModal" class="itbms-brand-filter-button bg-gray-100 hover:bg-gray-200 p-2 rounded-r-none focus:outline-none">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V19l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
            </svg>
          </button>
          <button @click="clearAllBrandFilters()" class="itbms-brand-filter-clear bg-red-500 text-white hover:bg-transparent hover:text-red-500 border-2 border-red-500 p-2 rounded-r-md focus:outline-none transition-colors duration-300">
            Clear
          </button>
        </div>
      </div>


      <div class="flex items-center space-x-4 mr-[5%]">
        <button
          v-if="isGridView"
          @click="sortBrandAscending"
          class="itbms-brand-asc border-2 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 focus:outline-none"
          :class="currentSortOrder === 'brandAsc' ? 'bg-transparent text-blue-500 border-blue-500' : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500'"
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
          class="itbms-brand-desc border-2 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 focus:outline-none"
          :class="currentSortOrder === 'brandDesc' ? 'bg-transparent text-blue-500 border-blue-500' : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500'"
          title="Sort by Brand Descending"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 4.5h14.25M3 9h9.75M3 13.5h9.75m4.5 4.5V8.25m0 0 3 3m-3-3-3 3" />
          </svg>
          <span class="sr-only">Sort by Brand Descending</span>
        </button>

        <button
          v-if="isGridView"
          @click="clearBrandSorting"
          class="itbms-brand-none border-2 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 focus:outline-none"
          :class="currentSortOrder === 'createdOn' ? 'bg-transparent text-blue-500 border-blue-500' : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500'"
          title="Clear Sort (Default: Created On)"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5a.75.75 0 0 0-1.5 0v7.5a.75.75 0 0 0 .75.75h4.5a.75.75 0 0 0 0-1.5h-3.75V4.5Z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12c0 5.66 4.62 10.25 10.25 10.25S22.75 17.66 22.75 12A10.25 10.25 0 0 0 12 1.75 10.07 10.07 0 0 0 4.25 4.5" />
          </svg>
          <span class="sr-only">Clear Sort</span>
        </button>


        <button
          v-if="!isGridView"
          @click="goToManageBrand"
          class="itbms-manage-brand bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500"
        >
          Manage Brand
        </button>


      </div>

    </div>

    <div class="ml-[6%] mb-6 flex items-center gap-3">
          <label for="page-size" class="text-sm font-medium text-gray-700">
            Items per page:
          </label>
          <select
            id="page-size"
            v-model="pageSize"
            class="itbms-page-size border-2 border-blue-500 bg-white rounded-md px-3 py-2 text-sm font-semibold text-blue-700
              shadow-sm transition duration-200 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-500
              hover:border-blue-600 hover:bg-blue-50 cursor-pointer"
            >
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            </select>
        </div>


    <transition name="modal-fade">
      <div v-if="showBrandFilterModal" class="itbms-bg fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="bg-white p-6 rounded-lg shadow-lg w-full max-w-md mx-4">
          <h3 class="text-lg font-semibold mb-4 text-black">Select Brands</h3>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-x-4 gap-y-2 max-h-60 overflow-y-auto border border-gray-300 rounded-md p-2">
            <div v-for="brand in availableBrands" :key="brand" class="flex items-center space-x-2 py-1">
              <input
                type="checkbox"
                :id="'brand-' + brand"
                :value="brand"
                v-model="selectedBrands"
                class="form-checkbox h-4 w-4 text-blue-600 rounded"
              />
              <label :for="'brand-' + brand" class="itbms-filter-item text-gray-700">{{ brand }}</label>
            </div>
            <p v-if="availableBrands.length === 0" class="text-gray-500 text-center py-4 w-full col-span-full">No brands available.</p>
          </div>
          <div class="mt-4 flex justify-end space-x-2">
            <button @click="showBrandFilterModal = false" class="bg-gray-300 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-400 transition-colors duration-300">Close</button>
            <button @click="clearAllBrandFilters()" class="bg-red-500 text-white px-4 py-2 rounded-md hover:bg-red-600 transition-colors duration-300">Clear All</button>
          </div>
        </div>
      </div>
    </transition>

    <div v-if="isGridView" class="p-6">
      <div v-if="items && items.length === 0" class="text-gray-500 text-center">No sale items found.</div>
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        <div
          v-for="(item, index) in items"
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
      <div v-if="items && items.length === 0" class="text-gray-500 text-center">
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
            v-for="item in items"
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
    <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
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

        <!-- ✅ Pagination Navigation Bar -->
<div :class="totalPages === 1 ? 'invisible' : 'visible'" class="flex justify-center mt-6 flex-wrap gap-2">
    <!-- First -->
    <button
      @click="() => { lastAction = 'first'; goToPage(0) }"
      :disabled="currentPage === 0"
      :class="[
        'itbms-page-first rounded-md px-4 py-2 border-2 text-sm transition-colors duration-300',
        currentPage === 0
          ? 'bg-blue-300 text-white border-blue-300 opacity-90'
          : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500 cursor-pointer'
      ]"
    >
      ⏮ First
    </button>

    <!-- Prev -->
    <button
      @click="() => { lastAction = 'prev'; goToPage(currentPage - 1) }"
  :disabled="currentPage === 0"
      :class="[
        'itbms-page-prev rounded-md px-4 py-2 border-2 text-sm transition-colors duration-300',
        currentPage === 0
          ? 'bg-blue-300 text-white border-blue-300 opacity-90'
          : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500 cursor-pointer'
      ]"
    >
      ◀ Prev
    </button>

    <!-- Page Numbers -->
    <button
      v-for="page in visiblePages"
      :key="'page-' + page"
      @click="() => { lastAction = ''; goToPage(page - 1) }"
      :class="[
        `itbms-page-${page - 1}`,
        'px-4 py-2 border-2 rounded-md text-sm font-semibold transition shadow-sm',
        currentPage === page - 1
          ? 'bg-blue-600 text-white border-blue-700'
          : 'bg-white text-blue-600 border-blue-300 hover:bg-blue-100 hover:text-blue-700'
      ]"
    >
      {{ page }}
    </button>

    <!-- Next -->
    <button
      @click="() => { lastAction = 'next'; goToPage(currentPage + 1) }"
  :disabled="currentPage === totalPages  - 1"
      :class="[
        'itbms-page-next rounded-md px-4 py-2 border-2 text-sm transition-colors duration-300',
        currentPage === totalPages  - 1
          ? 'bg-blue-300 text-white border-blue-300 opacity-90'
          : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500 cursor-pointer'
      ]"
    >
      Next ▶
    </button>

    <!-- Last -->
    <button
      @click="() => { lastAction = 'last';goToPage(totalPages - 1)}"
      :disabled="currentPage === totalPages - 1"
      :class="[
        'itbms-page-last rounded-md px-4 py-2 border-2 text-sm transition-colors duration-300',
        currentPage === totalPages  - 1
          ? 'bg-blue-300 text-white border-blue-300 opacity-90'
          : 'bg-blue-500 text-white border-blue-500 hover:bg-transparent hover:text-blue-500 cursor-pointer'
      ]"
    >
      Last ⏭
    </button>
  </div>

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
