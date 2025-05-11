<script setup>
import { ref, computed,onMounted } from 'vue'
import { useRouter,useRoute } from 'vue-router'
import { addItem,editItem, getItems, getItemById } from '@/libs/fetchUtilsOur'
import Footer from './Footer.vue'

const router = useRouter()
const route = useRoute()

const product = ref({
  id: '',
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
const originalProduct = ref(null)
const addnewitemMessage = ref('New Sale ltem')

// State สำหรับควบคุมการแสดง Pop-up
const showConfirmationAddPopup = ref(false)
const showConfirmationEditPopup = ref(false)
const isLoading = ref(false)
const brandList = ref([])
const id = route.params.id
const isEditMode = ref(false)
const isAdding = computed(() => !isEditMode.value)
const countdown = ref(3)
const startCountdown = () => {
  if (countdown.value > 0) {
    setTimeout(() => {
      countdown.value--
      startCountdown() // เรียกตัวเองซ้ำ
    }, 1000)
  }
}
const showNotFoundPopup = ref(false)

onMounted(async () => {
  try {
    const data = await getItems('http://ip24sy4.sit.kmutt.ac.th:8080/v1/brands')
    brandList.value = data.sort((a, b) => {
  if (a.brandName < b.brandName) {
    return -1;
  }
  if (a.brandName > b.brandName) {
    return 1;
  }
  return 0;
});
  } catch (err) {
    console.error('Error loading items:', err)
  }
  if (id) {
    isEditMode.value = true
    const data = await getItemById('http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items', id)
    if (data) {
      const formattedProduct = {
        id: data.id,
        brandName: data.brandName,
        model: data.model,
        price: data.price,
        description: data.description,
        ramGb: data.ramGb,
        screenSizeInch: data.screenSizeInch,
        storageGb: data.storageGb,
        color: data.color,
        quantity: data.quantity,
      }
      product.value = { ...formattedProduct }
      originalProduct.value = { ...formattedProduct }
    } else {
      if (!data || data?.status === 404) {
      showNotFoundPopup.value = true
      startCountdown()

      setTimeout(() => {
        router.push('/sale-items')
      }, 3000)
      //alert('The requested sale item does not exist.')
      return
    }
    }
  }
})

const isModified = computed(() => {
  if (!originalProduct.value) return true // ในกรณีเพิ่มสินค้าใหม่
  return Object.keys(product.value).some(key => String(product.value[key]) !== String(originalProduct.value[key]))
})

const isFormTouched = computed(() => {
  return Object.values(product.value).some(val => String(val).trim() !== '')
})

const isValid = () => {
  const { brandName, model, price, description, quantity } = product.value
  return [brandName, model, price, description, quantity].every(val => String(val).trim() !== '')
}

const submitForm = async () => {
  if (!isFormTouched.value) {
    responseMessage.value = 'กรุณากรอกข้อมูลอย่างน้อยหนึ่งช่อง'
    return
  }

  if (!isValid()) {
    responseMessage.value = 'กรุณากรอกข้อมูลให้ครบถ้วน'
    return
  }

  if (isEditMode.value) {
    showConfirmationEditPopup.value = true
  } else {
    showConfirmationAddPopup.value = true
  }

}

const confirmAddItem = async () => {
  const isAdding = !isEditMode.value
  if (isAdding) {
    showConfirmationAddPopup.value = true
  } else {
    showConfirmationAddPopup.value = false
  }
  isLoading.value = true

  const newProduct = {
    //id:product.value.id,
    brand: {
      brandName: product.value.brandName
    },
    model: product.value.model.trim(),
    description: product.value.description.trim(),
    image: mainImage.value,
    price: parseFloat(product.value.price),
    ramGb: product.value.ramGb ? parseInt(product.value.ramGb) : null,
    screenSizeInch: parseFloat(product.value.screenSizeInch),
    storageGb: product.value.storageGb ? parseInt(product.value.storageGb) : null,
    quantity: parseInt(product.value.quantity),
    color: product.value.color.trim() || null,
  }
if (isEditMode.value) {
  try {
    const result = await editItem(
      'http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items',
      id,
      newProduct
    );

    if (!result || result.status === 'error' || !result.id) {
      throw new Error('Edit failed or invalid data returned');
    }

    setTimeout(() => {
      isLoading.value = false;
      router.push({
        path: `/sale-items/${id}`,
        query: { editSuccess: 'true' },
      });
    }, 1000);
  } catch (err) {
    console.error(err);
    responseMessage.value = 'เกิดข้อผิดพลาดในการแก้ไขสินค้า';
    isLoading.value = false;
    router.push({
      path: `/sale-items/${id}`,
      query: { editFail: 'true' },
    });
  }
} else {
  try {
    const result = await addItem(
      'http://ip24sy4.sit.kmutt.ac.th:8080/v1/sale-items',
      newProduct
    );

    if (!result || result.status === 'error' || !result.id) {
      throw new Error('Add failed or invalid data returned');
    }

    setTimeout(() => {
      isLoading.value = false;
      router.push({
        path: '/sale-items',
        query: { addSuccess: 'true' },
      });
    }, 1000);
  } catch (err) {
    console.error(err);
    responseMessage.value = 'เกิดข้อผิดพลาดในการเพิ่มสินค้า';
    isLoading.value = false;
    router.push({
      path: '/sale-items',
      query: { addFail: 'true' },
    });
  }
}

}
const cancelAddItem = () => {
  showConfirmationAddPopup.value = false // ปิด Pop-up ยืนยัน
  showConfirmationEditPopup.value = false
}
</script>

<template>
  <div class="p-4 w-full min-h-screen bg-white">
    <nav class="text-sm text-gray-500 mb-4 max-w-6xl mx-auto">
      <router-link to="/sale-items"><span class="itbms-home-button hover:underline cursor-pointer">Home</span></router-link> ›
      <span v-if="product?.id" class="itbms-row text-gray-800 font-medium ml-1">
      <router-link :to="{ path: `/sale-items/${product.id}` }" class="itbms-back-button hover:underline cursor-pointer">
        {{ product?.model || '-' }} {{ product?.ramGb || '-' }}/{{ product?.storageGb || '-' }}GB {{ product?.color || '-' }}
      </router-link>
    </span>
    <span v-else class="itbms-row text-gray-800 font-medium ml-1">
      {{ addnewitemMessage }}
    </span>
    </nav>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 bg-white p-6 rounded-lg shadow max-w-6xl mx-auto">

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

      <div class="space-y-3 text-sm text-black">
        <div class="grid grid-cols-2 gap-2 items-center">
          <!-- <label class="text-left font-medium">Product ID:</label> -->
          <!-- <input v-model="product.id" type="number" class="border p-2 rounded w-full" /> -->
          
          <label class="itbms-brand text-left font-medium">Brand:</label>
          <select v-if="brandList.length > 0" v-model="product.brandName" class="itbms-brand border p-2 rounded w-full">
          <option value="" disabled selected> Select Brand</option>
          <option v-for="brand in brandList" :key="brand.id" :value="brand.brandName">
          {{ brand.brandName }}
          </option>
          </select>
          <div v-else class="itbms-brand border p-2 rounded w-full text-gray-500 bg-gray-50">No brand found.</div>

          <label class="itbms-model text-left font-medium">Model:</label>
          <input v-model="product.model" type="text" class="itbms-model border p-2 rounded w-full" />

          <label class="itbms-price text-left font-medium">Price (Baht):</label>
          <input v-model="product.price" type="number" class="itbms-price border p-2 rounded w-full" />

          <label class="itbms-ramGb text-left font-medium">Ram (GB):</label>
          <input v-model="product.ramGb" type="number" class="itbms-ramGb border p-2 rounded w-full" />

          <label class="itbms-screenSizeInch text-left font-medium">Screen Size (in):</label>
          <input v-model="product.screenSizeInch" type="number" class="itbms-screenSizeInch border p-2 rounded w-full" />

          <label class="itbms-storageGb text-left font-medium">Storage (GB):</label>
          <input v-model="product.storageGb" type="number" class="itbms-storageGb border p-2 rounded w-full" />

          <label class="itbms-color text-left font-medium">Color:</label>
          <input v-model="product.color" type="text" class="itbms-color border p-2 rounded w-full" />

          <label class="itbms-quantity text-left font-medium">Quantity:</label>
          <input v-model="product.quantity" type="number" class="itbms-quantity border p-2 rounded w-full" />
        </div>

        <label class="itbms-description block font-medium mt-4">Description:</label>
        <textarea v-model="product.description" rows="3" class="itbms-description border p-2 rounded w-full"></textarea>

        <div class="flex gap-2 mt-4 justify-end">
          <button
           @click="submitForm"
           :disabled="!isFormTouched || !isValid() || (isEditMode && !isModified)"
           :class="[
             'itbms-save-button rounded-md px-4 py-2 transition-colors duration-300',
             isFormTouched && isValid() && (!isEditMode || isModified)
             ? 'bg-green-500 text-white border-2 border-green-500 cursor-pointer hover:bg-transparent hover:text-green-500'
             : 'bg-gray-300 text-gray-500 border-2 border-gray-300 cursor-not-allowed'
           ]"
         >
           Save
         </button>
          <router-link :to="isEditMode ? `/sale-items/${product.id}` : '/sale-items'">
            <button
              class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500"
            >
              Cancel
            </button>
          </router-link>
        </div>
      </div>
    </div>

    <transition name="bounce-popup">
  <div
    v-if="showConfirmationAddPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="itbms-message bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="itbms-message text-xl font-semibold mb-4">Confirm adding the product</h2>
      <p class="itbms-message mb-4">Do you want to add this product?</p>
      <div class="flex justify-center gap-4">
          <button @click="confirmAddItem" class="bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
         <button @click="cancelAddItem" class="bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>  
      </div>
    </div>
  </div>
</transition>

<!-- Edit popup -->
<transition name="bounce-popup">
  <div
    v-if="showConfirmationEditPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="itbms-message bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="itbms-message text-xl font-semibold mb-4">Confirm editing the product</h2>
      <p class="itbms-message mb-4">Do you want to save changes to this product?</p>
      <div class="flex justify-center gap-4">
        <button @click="confirmAddItem" class="bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
        <button @click="cancelAddItem" class="bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>     
      </div>
    </div>
  </div>
</transition>

<transition name="fade-background">
      <div v-if="isLoading" class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="itbms-message bg-white text-black p-6 rounded-lg shadow-lg text-center">
          <svg class="animate-spin h-8 w-8 text-blue-600 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z"/>
          </svg>
          <p class="itbms-message text-sm font-medium">Saving product...</p>
        </div>
      </div>
    </transition>
    <transition name="bounce-popup">
  <div
    v-if="showNotFoundPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50"
  >
    <div class="itbms-message bg-white text-black rounded-lg p-6 shadow-lg text-center max-w-sm w-full">
      <h2 class="itbms-message text-xl font-semibold mb-4">⚠️ Item not found.</h2>
      <p class="itbms-message mb-2">The requested sale item does not exist.</p>
      <p class="itbms-message text-sm text-gray-500">Bring You Back in {{ countdown }} second<span v-if="countdown > 1">s</span>...</p>
    </div>
  </div>
</transition>
</div>
  <Footer />
</template>

<style scoped>
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

/* Animation สำหรับ Fade In/Out ของเนื้อหา Pop-up (ถ้าต้องการ) */
.fade-in-out-enter-active,
.fade-in-out-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease; /* เพิ่ม transform */
}

.fade-in-out-enter-from {
  opacity: 0;
  transform: scale(0.95); /* เริ่มจากขนาดเล็กลงเล็กน้อย */
}

.fade-in-out-leave-to {
  opacity: 0;
  transform: scale(1.05); /* จบที่ขนาดใหญ่ขึ้นเล็กน้อย */
}

/* Animation สำหรับ Slide Up ของเนื้อหา Pop-up (ถ้าต้องการ) */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* สไตล์คงที่สำหรับพื้นหลัง (เพื่อให้ transition ทำงานได้) */
.fixed.bg-black {
  background-color: rgba(0, 0, 0, 0.5); /* กำหนด Opacity เริ่มต้น */
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
