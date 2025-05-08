<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { addItem } from '@/libs/fetchUtilsOur'
import Footer from './Footer.vue'

const router = useRouter()

const product = ref({
  brandName: '',
  model: '',
  price: '',
  description: '',
  ramGb: '',
  screenSizeInch: '',
  storageGb: '',
  color: '',
  quantity: '',
})

const imageList = ref(['/phone/iPhone.jpg', '/phone/iPhone2.jpg','/phone/iPhone3.jpg','/phone/iPhone4.jpg'])
const mainImage = ref('/phone/iPhone.jpg')
const responseMessage = ref('')

const isValid = () => {
  return Object.values(product.value).every(val => String(val).trim() !== '')
}

const submitForm = async () => {
  if (!isValid()) {
    responseMessage.value = 'กรุณากรอกข้อมูลให้ครบถ้วน'
    return
  }

  const newProduct = {
    ...product.value,
    image: mainImage.value,
    price: parseFloat(product.value.price),
    ramGb: parseInt(product.value.ramGb),
    screenSizeInch: parseFloat(product.value.screenSizeInch),
    storageGb: parseInt(product.value.storageGb),
    quantity: parseInt(product.value.quantity),
  }

  try {
    await addItem('http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items', newProduct)
    responseMessage.value = 'เพิ่มสินค้าสำเร็จแล้ว!'
    router.push('/sale-items')
  } catch (err) {
    console.error(err)
    responseMessage.value = 'เกิดข้อผิดพลาดในการเพิ่มสินค้า'
  }
}
</script>

<template>
    <div class="p-4 w-full min-h-screen bg-white">
        <!-- Breadcrumb -->
    <nav class="text-sm text-gray-500 mb-4 max-w-6xl mx-auto">
      <router-link to="/sale-items"><span class="hover:underline cursor-pointer">Home</span></router-link> ›
      <span class="itbms-row text-gray-800 font-medium ml-1">
        {{ product?.model || '-' }} {{ product?.ramGb || '-' }}/GB {{ product?.color || '-' }}
      </span>
    </nav>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 bg-white p-6 rounded-lg shadow max-w-6xl mx-auto">
        
        <!-- 🔹 รูปภาพด้านซ้าย -->
        <div>
          <img
            :src="mainImage"
            alt="Main Image"
            class="rounded-lg w-full h-96 object-cover mb-4"
          />
          <div class="flex gap-2">
            <img
              v-for="(img, index) in imageList"
              :key="index"
              :src="img"
              @click="mainImage = img"
              class="w-16 h-16 object-cover rounded cursor-pointer border"
              :class="{ 'border-blue-500': img === mainImage }"
            />
          </div>
        </div>
  
        <!-- 🔸 ช่องกรอกข้อมูลด้านขวา -->
        <div class="space-y-3 text-sm text-black">
          <div class="grid grid-cols-2 gap-2 items-center">
            <label class="text-left font-medium">Brand:</label>
            <input v-model="product.brandName" type="text" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Model:</label>
            <input v-model="product.model" type="text" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Price (Baht):</label>
            <input v-model="product.price" type="number" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Ram (GB):</label>
            <input v-model="product.ramGb" type="number" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Screen Size (in):</label>
            <input v-model="product.screenSizeInch" type="number" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Storage (GB):</label>
            <input v-model="product.storageGb" type="number" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Color:</label>
            <input v-model="product.color" type="text" class="border p-2 rounded w-full" />
  
            <label class="text-left font-medium">Quantity:</label>
            <input v-model="product.quantity" type="number" class="border p-2 rounded w-full" />
          </div>
  
          <label class="block font-medium mt-4">Description:</label>
          <textarea v-model="product.description" rows="3" class="border p-2 rounded w-full"></textarea>
  
          <div class="flex gap-2 mt-4 justify-end">
            <button @click="submitForm" class="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded shadow">
              Save
            </button>
            <router-link to="/sale-items"><button @click="submitForm" class="bg-red-500 hover:bg-red-600 text-white px-6 py-2 rounded shadow">
              Cancel
            </button></router-link>
          </div>
        </div>
      </div>
    </div>
  </template>
  

<style scoped>
/* ปรับแต่งเพิ่มเติมได้ที่นี่ */
</style>
