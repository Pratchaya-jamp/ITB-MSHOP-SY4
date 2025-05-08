<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getItems } from '@/libs/fetchUtilsOur';
import Footer from './Footer.vue'

const router = useRouter()
const items = ref([])
const searchQuery = ref('')
const filterBy = ref('')

const goTophoneDetails = (id) => {
  router.push(`/sale-items/${id}`)
}

onMounted(async () => {
  try {
    const data = await getItems('http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items')
    items.value = data
  } catch (err) {
    console.error('Error loading items:', err)
  }
})

const filteredAndSortedItems = computed(() => {
  let result = [...items.value]

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item =>
      item.brandName.toLowerCase().includes(query) ||
      item.model.toLowerCase().includes(query)
    )
  }

  if (filterBy.value === 'cheapest') {
    result.sort((a, b) => a.price - b.price)
  } else if (filterBy.value === 'expensive') {
    result.sort((a, b) => b.price - a.price)
  }

  return result
})
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
      <div class="Itbms-icons flex items-center space-x-4">
        <!-- Icons -->
        <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-black cursor-pointer" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
        <svg version="1.1" xmlns="http://www.w3.org/2000/svg" x="0" y="0" class="h-8 w-8 text-black cursor-pointer" viewBox="0 0 128 128">
          <g><path d="M125.1 43.6h-20.4V17.5H84.4v-2.9H46.5v2.9H26.2v26.2H2.9C1.3 43.7 0 45 0 46.6v8.7c0 1.6 1.3 2.9 2.9 2.9h122.2c1.6 0 2.9-1.3 2.9-2.9v-8.7c0-1.7-1.3-3-2.9-3zm-26.2 0H32V23.3h14.5v2.9h37.8v-2.9h14.5v20.3zm-78.5 64c0 3.2 2.6 5.8 5.8 5.8h72.7c3.2 0 5.8-2.6 5.8-5.8l14.5-46.5H8.7l11.7 46.5zm61.1-36.3c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3zm-23.3 0c0-5 8.7-5 8.7 0v29.1c0 5-8.7 5-8.7 0V71.3z"/>
          </g>
        </svg>
      </div>
    </div>
    <div class="ml-[8%]">
    <button
      class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg shadow"
      @click="router.push('/sale-items/add')"
    >
      + Add Sell Item
    </button>
  </div>
  <!-- Product list -->
    <div class="p-6">
      <div v-if="items.length === 0" class="text-gray-500 text-center">No sale items found.</div>

      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        <div
          v-for="item in filteredAndSortedItems"
          :key="item.id"
          class="itbms-row border rounded-lg p-4 shadow hover:shadow-lg text-black cursor-pointer"
	  :style="{ animationDelay: (index * 50) + 'ms' }"
          @click="goTophoneDetails(item.id)"
        >
          <img
            :src="'/phone/iPhone.jpg'"
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
  </div>
  
  <Footer />
</template>

<style scoped>
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
</style>

