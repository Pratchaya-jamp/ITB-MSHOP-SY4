<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getItemById } from '@/libs/fetchUtilsOur';

const route = useRoute()

const product = ref({
  brand: '-',
  model: '-',
  price: 0,
  description: '-',
  ramGb: 0,
  screenSizeInch: 0,
  storageGb: 0,
  color: '-',
  quantity: 0
})

const imageList = ref([])
const mainImage = ref('')

// ดึงข้อมูลจาก backend
onMounted(async () => {
  const id = route.params.id
  try {
    product.value = await getItemById(`http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items`, id)


    imageList.value = data.imageList?.length
      ? data.imageList
      : [new URL('../../public/phone/iPhone.jpg', import.meta.url).href]

    mainImage.value = imageList.value[0]
  } catch (err) {
    console.error('Error fetching product:', err)
  }
})
</script>

<template>
  <div class="p-4 w-full min-h-screen bg-white">
    <!-- Breadcrumb -->
    <nav class="text-sm text-gray-500 mb-4 max-w-6xl mx-auto">
      <router-link to="/sale-items"><span class="hover:underline cursor-pointer">Home</span></router-link> ›
      <span class="text-gray-800 font-medium ml-1">
        {{ product.model }} {{ product.ramGb }}/GB {{ product.color }}
      </span>
    </nav>

    <!-- Main content -->
    <div class="ltbms-product-detail grid grid-cols-1 md:grid-cols-2 gap-6 bg-white p-6 rounded-lg shadow max-w-6xl mx-auto">
      <!-- Image -->
      <div>
        <img
          :src="'/phone/iPhone.jpg'"
          alt="Product Image"
          class="ltbms-product-image rounded-lg w-full h-96 object-cover mb-4"
        />
        <div class="flex gap-2">
          <img
            v-for="(img, index) in imageList"
            :key="index"
            :src="img"
            class="ltbms-product-thumbnail w-16 h-16 object-cover rounded cursor-pointer border"
            :class="{'border-blue-500': img === mainImage}"
            @click="mainImage = img"
          />
        </div>
      </div>

      <!-- Product details -->
      <div class="space-y-3 text-base text-black">
        <div><strong>Brand:</strong> <span class="ltbms-brand">{{ product.brand }}</span></div>
        <div><strong>Model:</strong> <span class="ltbms-model">{{ product.model }}</span></div>
        <div>
          <strong>Price:</strong>
          <span class="ltbms-price font-semibold">{{ product.price.toLocaleString() }}</span>
          <span class="ltbms-price-unit">Baht</span>
        </div>
        <div><strong>Description:</strong> <span class="ltbms-description">{{ product.description }}</span></div>
        <div>
          <strong>Ram:</strong>
          <span class="ltbms-ramGb">{{ product.ramGb }}</span>
          <span class="ltbms-ramGb-unit">GB</span>
        </div>
        <div>
          <strong>Screen Size:</strong>
          <span class="ltbms-screenSizeInch">{{ product.screenSizeInch }}</span>
          <span class="ltbms-screenSizeInch-unit">inch</span>
        </div>
        <div>
          <strong>Storage:</strong>
          <span class="ltbms-storageGb">{{ product.storageGb }}</span>
          <span class="ltbms-storageGb-unit">GB</span>
        </div>
        <div><strong>Color:</strong> <span class="ltbms-color">{{ product.color }}</span></div>
        <div>
          <strong>Available quantity:</strong>
          <span class="ltbms-quantity text-green-600 font-medium">{{ product.quantity }}</span>
          <span class="ltbms-quantity-unit">units</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ปรับแต่ง class เพิ่มเติมได้ที่นี่ */
</style>
