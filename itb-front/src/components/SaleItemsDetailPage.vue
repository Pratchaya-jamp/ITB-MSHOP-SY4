<script setup>
import { ref, onMounted } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { getItemById } from '@/libs/fetchUtilsOur';

const route = useRoute()
const router = useRouter()

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

const imageList = ref(['/phone/iPhone.jpg','/phone/iPhone2.jpg','/phone/iPhone3.jpg','/phone/iPhone4.jpg'])
const mainImage = ref('/phone/iPhone.jpg')

// ดึงข้อมูลจาก backend
onMounted(async () => {
  const id = route.params.id
  try {
    product.value = await getItemById(`http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items`, id)


    // imageList.value = data.imageList?.length
    //   ? data.imageList
    //   : [new URL('/phone/iPhone.jpg', import.meta.url).href]

    // mainImage.value = imageList.value[0]
  } catch (err) {
    if (err.response && err.response.status === 404) {
      alert("The requested sale item does not exist or has been removed")
      router.push('/sale-items') 
    } else {
      console.error('Error fetching product:', err)
    }
  }
})

</script>

<template>
  <div class="p-4 w-full min-h-screen bg-white">
    <!-- Breadcrumb -->
    <nav class="text-sm text-gray-500 mb-4 max-w-6xl mx-auto">
      <router-link to="/sale-items"><span class="hover:underline cursor-pointer">Home</span></router-link> ›
      <span class="text-gray-800 font-medium ml-1">
        {{ product.model || '-' }} {{ product.ramGb || '-' }}/GB {{ product.color || '-' }}
      </span>
    </nav>

    <!-- Main content -->
    <div class="ltbms-product-detail grid grid-cols-1 md:grid-cols-2 gap-6 bg-white p-6 rounded-lg shadow max-w-6xl mx-auto">
      <!-- Image -->
      <div>
        <img
          :src="mainImage"
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
        <div>
          <strong>Brand:</strong>
          <span :class="{ 'text-gray-400': !product.brand }">{{ product.brand || ' - ' }}</span>
        </div>
        <div>
          <strong>Model:</strong>
          <span :class="{ 'text-gray-400': !product.model }">{{ product.model || ' - ' }}</span>
        </div>
        <div>
          <strong>Price:</strong>
          <span
            class="font-semibold"
            :class="{ 'text-gray-400': product.price === null || product.price === undefined }"
          >
            {{ product.price !== null && product.price !== undefined ? product.price.toLocaleString() : ' - ' }}
          </span>
          <span>Baht</span>
        </div>
        <div>
          <strong>Description:</strong>
          <span :class="{ 'text-gray-400': !product.description }">{{ product.description || ' - ' }}</span>
        </div>
        <div>
          <strong>Ram:</strong>
          <span :class="{ 'text-gray-400': !product.ramGb }">{{ product.ramGb || ' - ' }}</span>
          <span>GB</span>
        </div>
        <div>
          <strong>Screen Size:</strong>
          <span :class="{ 'text-gray-400': !product.screenSizeInch }">{{ product.screenSizeInch || ' - ' }}</span>
          <span>inch</span>
        </div>
        <div>
          <strong>Storage:</strong>
          <span :class="{ 'text-gray-400': !product.storageGb }">{{ product.storageGb || ' - ' }}</span>
          <span>GB</span>
        </div>
        <div>
          <strong>Color:</strong>
          <span :class="{ 'text-gray-400': !product.color }">{{ product.color || ' - ' }}</span>
        </div>
        <div>
          <strong>Available quantity:</strong>
          <span
            class="text-green-600 font-medium"
            :class="{ 'text-gray-400 font-normal': product.quantity === null || product.quantity === undefined }"
          >
            {{ product.quantity !== null && product.quantity !== undefined ? product.quantity : ' - ' }}
          </span>
          <span>units</span>
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
/* ปรับแต่ง class เพิ่มเติมได้ที่นี่ */
</style>
