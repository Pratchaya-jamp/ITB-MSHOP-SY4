<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { getItemById,deleteItemById } from '@/libs/fetchUtilsOur';


const route = useRoute()
const router = useRouter()

const product = ref(null)
const id = route.params.id

const imageList = ref(['/sy4/phone/iPhone.jpg','/sy4/phone/iPhone2.jpg','/sy4/phone/iPhone3.jpg','/sy4/phone/iPhone4.jpg'])
const mainImage = ref('/sy4/phone/iPhone.jpg')
const showNotFoundPopup = ref(false)
const isDeleting = ref(false)
const showDeleteConfirmationPopup = ref(false)
const deleteResponseMessage = ref('')
const countdown = ref(3)
const startCountdown = () => {
  if (countdown.value > 0) {
    setTimeout(() => {
      countdown.value--
      startCountdown() // เรียกตัวเองซ้ำ
    }, 1000)
  }
}
const showEditSuccessPopup = ref(false)
const showEditFallPopup = ref(false)

const closeSuccessPopup = () => {
  showEditSuccessPopup.value = false
showEditFallPopup.value = false
}

// ดึงข้อมูลจาก backend
onMounted(async () => {
  try {
    const data = await getItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items', id)
    if (!data || data?.status === 404) {
      showNotFoundPopup.value = true
      startCountdown()

      setTimeout(() => {
        router.push('/sale-items')
      }, 3000)
      //alert('The requested sale item does not exist.')
      return
    }
    product.value = data;
  } catch (error) {
    console.error('Failed to fetch product:', error);
    showNotFoundPopup.value = true
    startCountdown()

    setTimeout(() => {
      router.push('/sale-items')
    }, 3000)
  }
})

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
	    showEditFallPopup.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

const deleteproduct = async () => {
  showDeleteConfirmationPopup.value = true
}

const confirmDelete = async () => {
  showDeleteConfirmationPopup.value = false
  isDeleting.value = true
  try {
    const statusCode = await deleteItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items', id);
    if (statusCode === 204) {
      setTimeout(() => {
        isDeleting.value = false
        router.push({ path: '/sale-items', query: { deleteSuccess: 'true' } })
      }, 1000)
    }else if (statusCode === 404) {
      // กรณีได้รับ 404 ตอนลบ แสดงว่าข้อมูลไม่มีแล้ว
      isDeleting.value = false;
      showNotFoundPopup.value = true;
      startCountdown();
      setTimeout(() => {
        router.push('/sale-items');
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
</script>

<template>
  <div class="p-4 w-full min-h-screen bg-white">
    <!-- Breadcrumb -->
    <nav class="text-sm text-gray-500 mb-4 max-w-6xl mx-auto">
      <router-link to="/sale-items"><span class="itbms-home-button hover:underline cursor-pointer">Home</span></router-link> ›
      <span class="itbms-row text-gray-800 font-medium ml-1">
        {{ product?.model || '-' }} {{ product?.ramGb || '-' }}/{{ product?.storageGb || '-' }}GB {{ product?.color || '-' }}
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
      <div class="itbms-row space-y-3 text-base text-black">
        <div class="itbms-brand">
          <strong>Brand: </strong>
          <span :class="{ 'text-gray-400': !product?.brandName }">{{ product?.brandName || '-' }}</span>
        </div>
        <div class="itbms-model">
          <strong>Model: </strong>
          <span :class="{ 'text-gray-400': !product?.model }">{{ product?.model || '-' }}</span>
        </div>
        <div class="itbms-price">
          <strong>Price: </strong>
          <span
            class="font-semibold"
            :class="{ 'text-gray-400': product?.price === null || product?.price === undefined }"
          >
            {{ product?.price !== null && product?.price !== undefined ? product?.price.toLocaleString() : '-' }}
          </span>
          <span class="itbms-price-unit"> Baht</span>
        </div>
        <div class="itbms-description">
          <strong>Description: </strong>
          <span :class="{ 'text-gray-400': !product?.description }">{{ product?.description || '-' }}</span>
        </div>
        <div class="itbms-ramGb">
          <strong>Ram: </strong>
          <span :class="{ 'text-gray-400': !product?.ramGb }">{{ product?.ramGb || '-' }}</span>
          <span class="itbms-ramGb-unit"> GB</span>
        </div>
        <div class="itbms-screenSizeInch">
          <strong>Screen Size: </strong>
          <span :class="{ 'text-gray-400': !product?.screenSizeInch }">{{ product?.screenSizeInch || '-' }}</span>
          <span class="itbms-screenSizeInch-unit"> Inches</span>
        </div>
        <div class="itbms-storageGb">
          <strong>Storage: </strong>
          <span :class="{ 'text-gray-400': !product?.storageGb }">{{ product?.storageGb || '-' }}</span>
          <span class="itbms-storageGb-unit"> GB</span>
        </div>
        <div class="itbms-color">
          <strong>Color: </strong>
          <span :class="{ 'text-gray-400': !product?.color }">{{ product?.color || '-' }}</span>
        </div>
        <div class="itbms-quantity">
          <strong>Available quantity: </strong>
          <span
            class="text-green-600 font-medium"
            :class="{ 'text-gray-400 font-normal': product?.quantity === null || product?.quantity === undefined }"
          >
            {{ product?.quantity !== null && product?.quantity !== undefined ? product?.quantity : '-' }}
          </span>
          <span class="itbms-quantity-unit"> Units</span>
        </div>
        <div class="flex gap-2 mt-3">
        <button
        @click="router.push(`/sale-items/${product.id}/edit`)"
        class="itbms-edit-button bg-yellow-500 text-white border-2 border-yellow-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-yellow-500"
        >
          Edit
        </button>
        <button
        @click="deleteproduct"
        class="itbms-delete-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500"
        >
          Delete
        </button>
      </div>
      </div>
    </div>
    <transition name="bounce-popup">
  <div
    v-if="showDeleteConfirmationPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Confirm delete the product</h2>
      <p class="itbms-message mb-4">Do you want to delete this sale item?</p>
      <div class="flex justify-center gap-4">
        <button @click="confirmDelete" class="itbms-confirm-button bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
        <button @click="cancelDeleteItem" class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>     
      </div>
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
      <p class="itbms-message mb-2">The requested sale item does not exist.</p>
      <p class="text-sm text-gray-500">Bring You Back in {{ countdown }} second<span v-if="countdown > 1">s</span>...</p>
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
          <p class="itbms-message text-sm font-medium">Deleting product...</p>
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
      <p class="itbms-message mb-4">The sale item has been successfully updated!</p>
      <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
    </div>
  </div>
</transition>
 
<transition name="bounce-popup">
  <div
    v-if="showEditFallPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Error 500!</h2>
      <p class="itbms-message mb-4">The sale item has been fail to updated!</p>
      <button @click="closeSuccessPopup" class="bg-blue-500 text-white border-2 border-blue-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-blue-500">Done</button>
    </div>
  </div>
</transition>
 </div>


</template>


<style scoped>
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

/* Animation สำหรับ Fade In/Out ของพื้นหลัง Loading */
.fade-background-enter-active,
.fade-background-leave-active {
  transition: opacity 0.3s ease; /* เปลี่ยนเป็น opacity */
}

.fade-background-enter-from {
  opacity: 0; /* เริ่มจากโปร่งใส */
}

.fade-background-leave-to {
  opacity: 0; /* จบที่โปร่งใส */
}

/* สไตล์สำหรับพื้นหลัง Loading ที่จางลง */
.fixed.bg-black.loading-overlay {
  background-color: rgba(0, 0, 0, 0.2); /* ปรับ opacity เป็น 0.2 (จางลง) */
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

span {
  white-space: normal;
  word-break: break-word; 
}

</style>
