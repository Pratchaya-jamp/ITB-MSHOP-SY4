<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { addItem, editItem, getItemById } from '@/libs/fetchUtilsOur'


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

// เริ่มต้น theme ด้วยค่าจาก localStorage หรือใช้ 'dark' เป็นค่าเริ่มต้น
const theme = ref(localStorage.getItem('theme') || 'dark');

const toggleTheme = () => {
    theme.value = theme.value === 'dark' ? 'light' : 'dark';
};

// ใช้ watch เพื่อบันทึก theme ลง localStorage ทุกครั้งที่ theme.value เปลี่ยน
watch(theme, (newTheme) => {
    localStorage.setItem('theme', newTheme);
});

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
});

const iconComponent = computed(() => {
    return theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
});

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
});

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

const isValid = computed(() => {
    return (
        iscountryOfOriginValid.value === true &&
        isWebsiteUrlValid.value === true &&
        isNameValid.value === true
    )
})

const submitForm = async () => {
    if (!isFormTouched.value) {
        responseMessage.value = 'กรุณากรอกข้อมูลอย่างน้อยหนึ่งช่อง'
        return
    }

    if (!isValid.value) {
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
    showConfirmationAddPopup.value = false;
    showConfirmationEditPopup.value = false;
    isLoading.value = true;

    const newbrand = {
        name: brand.value.name.trim(),
        websiteUrl: brand.value.websiteUrl?.trim() || null,
        isActive: brand.value.isActive, // ส่งค่า boolean โดยตรง
        countryOfOrigin: brand.value.countryOfOrigin?.trim() || null,
    };

    if (isEditMode.value) {
        try {
            const result = await editItem(
                'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands', id,
                newbrand
            );

            if (result.status !== 200 || !result.data?.id) {
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

            if (result.status !== 201 || !result.data?.id) {
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
};

const cancelAddItem = () => {
    showConfirmationAddPopup.value = false // ปิด Pop-up ยืนยัน
    showConfirmationEditPopup.value = false
}
</script>

<template>
    <div :class="themeClass" class="min-h-screen px-4 py-8 font-sans transition-colors duration-500">
        <div
            class="max-w-6xl mx-auto text-sm mb-6 transition-colors duration-500"
            :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'"
        >
            <router-link to="/sale-items">
                <span class="itbms-item-list hover:underline cursor-pointer">Home</span>
            </router-link>
            <span class="mx-2" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">/</span>
            <router-link to="/brands">
                <span class="itbms-manage-brand hover:underline cursor-pointer">Brand List</span>
            </router-link>
            <span class="mx-2" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">/</span>
            <span v-if="brand?.name"
                class="itbms-row font-medium transition-colors duration-500"
                :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'"
            >
                {{ brand?.name || '-' }}
            </span>
            <span v-else
                class="font-medium transition-colors duration-500"
                :class="theme === 'dark' ? 'text-blue-400' : 'text-indigo-600'"
            >
                New Brand
            </span>
        </div>

        <div
            class="rounded-2xl shadow-xl w-full max-w-6xl mx-auto p-8 transition-colors duration-500"
            :class="theme === 'dark' ? 'bg-gray-900' : 'bg-white'"
        >
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div
                    class="flex flex-col items-center justify-center border rounded-2xl p-4 transition-colors duration-500"
                    :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'"
                >
                    <img :src="brandLogo" alt="Brand Logo" class="w-48 h-48 object-contain mb-4" />
                </div>

                <div class="space-y-4">
                    <div>
                        <label
                            class="block font-medium mb-1 transition-colors duration-500"
                            :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'"
                        >
                            Name:<span class="text-red-500">*</span>
                        </label>
                        <input
                            v-model="brand.name"
                            type="text"
                            class="itbms-name w-full border rounded-lg px-4 py-2 transition-colors duration-300"
                            :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'"
                        />
                        <p v-if="nameError" class="itbms-message text-red-500 text-sm mt-1">{{ nameError }}</p>
                    </div>

                    <div>
                        <label
                            class="block font-medium mb-1 transition-colors duration-500"
                            :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'"
                        >
                            Website URL:
                        </label>
                        <input
                            v-model="brand.websiteUrl"
                            type="url"
                            class="itbms-websiteUrl w-full border rounded-lg px-4 py-2 transition-colors duration-300"
                            :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'"
                        />
                        <p v-if="websiteUrlError" class="itbms-message text-red-500 text-sm mt-1">{{ websiteUrlError }}</p>
                    </div>

                    <div class="flex items-center">
                        <label class="block font-medium mr-4 transition-colors duration-500" :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
                            isActive:
                        </label>
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input
                                v-model="brand.isActive"
                                type="checkbox"
                                id="isActiveSwitch"
                                class="sr-only peer"
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

                    <div>
                        <label
                            class="block font-medium mb-1 transition-colors duration-500"
                            :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'"
                        >
                            Country of Origin:
                        </label>
                        <input
                            v-model="brand.countryOfOrigin"
                            type="text"
                            class="itbms-countryOfOrigin w-full border rounded-lg px-4 py-2 transition-colors duration-300"
                            :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'"
                        />
                        <p v-if="countryOfOriginError" class="itbms-message text-red-500 text-sm mt-1">{{ countryOfOriginError }}</p>
                    </div>

                    <div class="flex flex-col sm:flex-row gap-4 pt-4">
                        <button
                            @click="submitForm"
                            :disabled="!isFormTouched || !isValid || (isEditMode && !isModified)"
                            :class="[
                                'itbms-save-button w-full font-semibold border-2 rounded-xl px-6 py-3 transition-all duration-300 transform active:scale-95 shadow-md',
                                isFormTouched && isValid && (!isEditMode || isModified)
                                    ? 'bg-green-500 text-white border-green-500 hover:bg-green-600'
                                    : 'bg-gray-300 text-gray-500 border-gray-300 cursor-not-allowed'
                            ]"
                        >
                            Save
                        </button>
                        <router-link to="/brands" class="w-full">
                            <button
                                class="itbms-cancel-button w-full font-semibold border-2 rounded-xl px-6 py-3 transition-all duration-300 transform active:scale-95 shadow-md"
                                :class="theme === 'dark'
                                    ? 'bg-red-500 text-white border-red-500 hover:bg-red-600'
                                    : 'bg-red-500 text-white border-red-500 hover:bg-red-600'"
                            >
                                Cancel
                            </button>
                        </router-link>
                    </div>
                </div>
            </div>
        </div>
        
        <transition name="bounce-popup">
            <div v-if="showConfirmationAddPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm adding the brand</h2>
                    <p class="itbms-message mb-6 text-lg">Do you want to add this brand?</p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmAddItem"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95">Yes</button>
                        <button @click="cancelAddItem"
                            class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95">No</button>
                    </div>
                </div>
            </div>
        </transition>
        <transition name="bounce-popup">
            <div v-if="showConfirmationEditPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm editing the brand</h2>
                    <p class="itbms-message mb-6 text-lg">Do you want to save changes to this brand?</p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmAddItem"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95">Yes</button>
                        <button @click="cancelAddItem"
                            class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95">No</button>
                    </div>
                </div>
            </div>
        </transition>
        <transition name="fade-background">
            <div v-if="isLoading" class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
                <div
                    class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
                    </svg>
                    <p class="itbms-message text-lg font-medium">Saving brand...</p>
                </div>
            </div>
        </transition>
        <transition name="bounce-popup">
            <div v-if="showNotFoundPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center max-w-sm w-full transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">⚠️ Item not found.</h2>
                    <p class="itbms-message mb-4 text-lg">The brand does not exist.</p>
                    <p class="text-sm transition-colors duration-500"
                        :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                        Redirecting in {{ countdown }} second<span v-if="countdown > 1">s</span>...
                    </p>
                </div>
            </div>
        </transition>

        <button @click="toggleTheme"
            class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-md shadow-lg transition-all duration-300 z-50 hover:shadow-2xl"
            :class="theme === 'dark' ? 'bg-gray-700/80 text-white' : 'bg-gray-200/80 text-black'"
            v-html="iconComponent">
        </button>
    </div>
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

/* .itbms-isActive {
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
    width: 44px;
    height: 24px;
    background-color: #e5e7eb;
    border-radius: 12px;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
}

input[type="checkbox"].itbms-checkbox:checked + div {
    background-color: #6366f1;
}

input[type="checkbox"].itbms-checkbox + div + span {
    position: absolute;
    top: 2px;
    left: 2px;
    width: 20px;
    height: 20px;
    background-color: white;
    border-radius: 50%;
    transition: transform 0.2s ease-in-out;
}

input[type="checkbox"].itbms-checkbox:checked + div + span {
    transform: translateX(20px);
} */

/* ส่วนนี้สามารถเพิ่มในไฟล์ CSS ของคุณ หรือใช้ Tailwind ได้เลย */
.itbms-bg {
    background-color: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(4px);
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
    transition: opacity 0.3s ease;
}
.fade-background-enter-from,
.fade-background-leave-to {
    opacity: 0;
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
