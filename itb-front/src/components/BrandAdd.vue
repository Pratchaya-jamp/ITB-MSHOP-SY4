<script setup>
import { ref,  onMounted, computed,watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { addItem,editItem,  getItemById } from '@/libs/fetchUtilsOur'


const route = useRoute();
const router = useRouter();


const brandLogo = ref('/sy4/logobrands/1.png');

// State สำหรับควบคุมการแสดง Pop-up
const originalBrand = ref(null)
const showConfirmationAddPopup = ref(false)
const showConfirmationEditPopup = ref(false)
const id = route.params.id
const isEditMode = ref(false)
const countdown = ref(3)
const isLoading = ref(false)
const responseMessage = ref('')

const startCountdown = () => {
  if (countdown.value > 0) {
    setTimeout(() => {
      countdown.value--
      startCountdown() // เรียกตัวเองซ้ำ
    }, 1000)
  }
}
const showNotFoundPopup = ref(false)

const brand = ref({
  name: '',
  websiteUrl: '',
  isActive: false, // เปลี่ยนค่าเริ่มต้นเป็น false
  countryOfOrigin: '',
});


 onMounted(async () => {
    if (id) {
    isEditMode.value = true
    const data = await getItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands', id)

    if (data) {
      const formattedBrand = {
        name: data.name,
        websiteUrl: data.websiteUrl,
        isActive: data.isActive,
        countryOfOrigin: data.countryOfOrigin,
      }
      brand.value = { ...formattedBrand }
      originalBrand.value = { ...formattedBrand }

    } else {
      if (!data || data?.status === 404) {
        showNotFoundPopup.value = true
        startCountdown()

        setTimeout(() => {
          router.push('/brands')
        }, 3000)
      }
    }
}
})

const isNameValid = ref(false)
const isWebsiteUrlValid = ref(true)
const iscountryOfOriginValid = ref(true)


// ติดตาม Valid แบบ real-time
const nameError = ref('')
const websiteUrlError = ref('')
const countryOfOriginError = ref('')

// --- Brand Name ---
watch(() => brand.value.name, (newVal) => {
  const name = newVal.trim()
  if (!name || name.length > 30) {
    nameError.value = 'Brand name must be 1-30 characters long.'
    isNameValid.value = false
  } else {
    nameError.value = ''
    isNameValid.value = true
  }
})

// --- Website URL ---
watch(() => brand.value.websiteUrl, (newVal) => {
  const url = newVal.trim()
  const pattern = /^(https?:\/\/)?([\w\-]+\.)+[\w\-]+(\/[\w\-._~:/?#[\]@!$&'()*+,;=]*)?$/

  if (!url) {
    websiteUrlError.value = ''
    isWebsiteUrlValid.value = true
  } else if (url.length > 80 || !pattern.test(url)) {
    websiteUrlError.value = 'Brand URL must be a valid URL or not specified.'
    isWebsiteUrlValid.value = false
  } else {
    websiteUrlError.value = ''
    isWebsiteUrlValid.value = true
  }
})

// --- Country of Origin ---
watch(() => brand.value.countryOfOrigin, (newVal) => {
  const country = newVal.trim()
  if (country.length > 80) {
    countryOfOriginError.value = 'Brand country of origin must be 1-80 characters long or not specified.'
    iscountryOfOriginValid.value = false
  } else {
    countryOfOriginError.value = ''
    iscountryOfOriginValid.value = true
  }
})


const isModified = computed(() => {
  if (!originalBrand.value) return true // ในกรณีเพิ่มสินค้าใหม่
  return Object.keys(brand.value).some(key => String(brand.value[key]) !== String(originalBrand.value[key]))
})

const isFormTouched = computed(() => {
  return Object.values(brand.value).some(val => String(val).trim() !== '')
})

const isValid = () => {
  return (
  iscountryOfOriginValid.value === true &&
  isWebsiteUrlValid.value === true &&
  isNameValid.value === true 
)
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

  const newbrand = {
    name: brand.value.name.trim(),
  websiteUrl: brand.value.websiteUrl?.trim() || null,
  isActive: brand.value.isActive, // ส่งค่า boolean โดยตรง
  countryOfOrigin: brand.value.countryOfOrigin?.trim() || null,
  }

if (isEditMode.value) {
  try {
    const result = await editItem(
      'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands', id,
      newbrand
    );

    if (!result || result.status === 'error' || !result.id) {
      throw new Error('Edit failed or invalid data returned');
    }

    setTimeout(() => {
      isLoading.value = false;
      router.push({
        path: '/brands',
        query: { editSuccess: 'true' },
      });
    }, 1000);
  } catch (err) {
    console.error(err);
    responseMessage.value = 'เกิดข้อผิดพลาดในการแก้ไขสินค้า';
    isLoading.value = false;
    router.push({
      path: '/brands',
      query: { editFail: 'true' },
    });
  }
} else {
  try {
    const result = await addItem(
      'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands',
      newbrand
    );

    if (!result || result.status === 'error' || !result.id) {
      throw new Error('Add failed or invalid data returned');
    }

    setTimeout(() => {
      isLoading.value = false;
      router.push({
        path: '/brands',
        query: { addSuccess: 'true' },
      });
    }, 1000);
  } catch (err) {
    console.error(err);
    responseMessage.value = 'เกิดข้อผิดพลาดในการเพิ่มสินค้า';
    isLoading.value = false;
    router.push({
      path: '/brands',
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
  <div class="min-h-screen bg-white px-4 py-8">
    <div class="max-w-6xl mx-auto text-gray-600 text-sm mb-4">
      <router-link to="/sale-items"><span class="itbms-item-list hover:underline cursor-pointer">Home</span></router-link> ›
      <router-link to="/brands"><span class="itbms-manage-brand hover:underline cursor-pointer">BrandList</span></router-link> ›
      <span v-if="brand?.name" class="itbms-row text-gray-800 font-medium ml-1">
        {{ brand?.name || '-' }}
        </span>
      <span v-else
      class="text-indigo-600 font-medium">New Brand</span>
    </div>

    <div class="bg-white rounded-lg shadow-lg w-full max-w-6xl mx-auto p-8">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

        <div class="flex flex-col items-center justify-center border border-gray-300 rounded-lg p-4">
          <img :src="brandLogo" alt="Brand Logo" class="w-48 h-48 object-contain mb-4" />
        </div>

        <div>
          <div class="mb-4">
            <label class="block text-gray-800 font-medium mb-1">Name:<span class="text-red-500">*</span></label>
            <input v-model="brand.name" type="text"
              class="itbms-name w-full border border-gray-300 rounded px-4 py-2 text-gray-900 placeholder-gray-400" />
            
          <p v-if="nameError" class="itbms-message text-red-500 text-sm">{{ nameError }}</p>
          </div>

          <div class="mb-4">
            <label class="block text-gray-800 font-medium mb-1">WebsiteUrl:</label>
            <input v-model="brand.websiteUrl" type="url"
              class="itbms-websiteUrl w-full border border-gray-300 rounded px-4 py-2 text-gray-900 placeholder-gray-400" />
              <p v-if="websiteUrlError" class="itbms-message text-red-500 text-sm">{{ websiteUrlError }}</p>
          </div>
 
<div class="mb-4 flex items-center">
  <label class="block text-gray-800 font-medium mr-4">isActive:</label>
  <label class="relative inline-flex items-center cursor-pointer w-11 h-6">
    <input
      v-model="brand.isActive"
      type="checkbox"
      id="isActiveSwitch"
      class="itbms-isActive itbms-checkbox"
    />
    <div
      class="w-11 h-6 bg-gray-200 rounded-full peer dark:bg-gray-700 peer-checked:bg-indigo-600 transition-colors duration-200 ease-in-out"
    ></div>
    <span
      aria-hidden="true"
      class="absolute top-[2px] left-[2px] h-5 w-5 bg-white border border-gray-300 rounded-full peer-checked:translate-x-5 transition-transform duration-200 ease-in-out"
    ></span>
  </label>
</div>

          <div class="mb-6">
            <label class="block text-gray-800 font-medium mb-1">countryOfOrigin:</label>
            <input v-model="brand.countryOfOrigin" type="text"
              class="itbms-countryOfOrigin w-full border border-gray-300 rounded px-4 py-2 text-gray-900 placeholder-gray-400" />
              <p v-if="countryOfOriginError" class="itbms-message text-red-500 text-sm">{{ countryOfOriginError }}</p>
          </div>

          <div class="flex gap-2 mt-4 justify-end">
          <button
            @click="submitForm"
            :disabled="!isFormTouched || !isValid() || (isEditMode && !isModified)"
            :class="[
              'itbms-save-button rounded-md px-4 py-2 transition-colors duration-300',
              isFormTouched && isValid() && (!isEditMode || isModified)
              ? 'bg-green-500 text-white border-2 border-green-500 cursor-pointer hover:bg-transparent hover:text-green-500'
              : 'bg-gray-300 text-gray-500 border-2 border-gray-300'
            ]"
          >
            Save
          </button>
          <router-link to="/brands">
            <button
              class="itbms-cancel-button bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500"
            >
              Cancel
            </button>
          </router-link>
          </div>
        </div>

      </div>
    </div>
  </div>
  <transition name="bounce-popup">
  <div
    v-if="showConfirmationAddPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Confirm adding the brand</h2>
      <p class="itbms-message mb-4">Do you want to add this brand?</p>
      <div class="flex justify-center gap-4">
          <button @click="confirmAddItem" class="bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
         <button @click="cancelAddItem" class="bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>
      </div>
    </div>
  </div>
</transition>

<transition name="bounce-popup">
  <div
    v-if="showConfirmationEditPopup"
    class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white text-black  rounded-lg p-6 shadow-lg text-center">
      <h2 class="text-xl font-semibold mb-4">Confirm editing the brand</h2>
      <p class="itbms-message mb-4">Do you want to save changes to this brand?</p>
      <div class="flex justify-center gap-4">
        <button @click="confirmAddItem" class="bg-green-500 text-white border-2 border-green-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-green-500">Yes</button>
        <button @click="cancelAddItem" class="bg-red-500 text-white border-2 border-red-500 rounded-md px-4 py-2 cursor-pointer transition-colors duration-300 hover:bg-transparent hover:text-red-500">No</button>
      </div>
    </div>
  </div>
</transition>

<transition name="fade-background">
      <div v-if="isLoading" class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white text-black p-6 rounded-lg shadow-lg text-center">
          <svg class="animate-spin h-8 w-8 text-blue-600 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z"/>
          </svg>
          <p class="itbms-message text-sm font-medium">Saving brand...</p>
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
      <p class="itbms-message mb-2">The brand does not exist.</p>
      <p class="text-sm text-gray-500">Bring You Back in {{ countdown }} second<span v-if="countdown > 1">s</span>...</p>
    </div>
  </div>
</transition>
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

.itbms-isActive {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 10;
  opacity: 0;
  appearance: none;
  cursor: pointer;
}


input[type="checkbox"].itbms-checkbox + div {
  width: 44px; /* ปรับขนาดตามต้องการ */
  height: 24px; /* ปรับขนาดตามต้องการ */
  background-color: #e5e7eb; /* สีพื้นหลังเมื่อไม่ Checked */
  border-radius: 12px; /* ทำให้ขอบมน */
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

input[type="checkbox"].itbms-checkbox:checked + div {
  background-color: #6366f1; /* สีพื้นหลังเมื่อ Checked */
}

input[type="checkbox"].itbms-checkbox + div + span {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px; /* ปรับขนาด Thumb ตามต้องการ */
  height: 20px; /* ปรับขนาด Thumb ตามต้องการ */
  background-color: white;
  border-radius: 50%; /* ทำให้เป็นวงกลม */
  transition: transform 0.2s ease-in-out;
}

input[type="checkbox"].itbms-checkbox:checked + div + span {
  transform: translateX(20px); /* ระยะเลื่อนเมื่อ Checked */
}



</style>
