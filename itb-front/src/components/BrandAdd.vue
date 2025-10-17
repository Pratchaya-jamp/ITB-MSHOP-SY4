<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { addItem, editItem, getItemById } from '@/libs/fetchUtilsOur'
// ✨ 1. ปรับปรุง: Import theme จาก store โดยตรง
import { theme, toggleTheme } from '@/stores/themeStore.js';

const route = useRoute();
const router = useRouter();

const brandLogo = ref('/sy4/logobrands/1.png');

// --- State ---
const originalBrand = ref(null)
const showConfirmationAddPopup = ref(false)
const showConfirmationEditPopup = ref(false)
const id = route.params.id
const isEditMode = ref(!!id); // ใช้ !!id เพื่อให้เป็น boolean ชัดเจน
const countdown = ref(3)
const isLoading = ref(false)
const responseMessage = ref('')
const showNotFoundPopup = ref(false)

const brand = ref({
    name: '',
    websiteUrl: '',
    isActive: true, // ตั้งค่าเริ่มต้นเป็น true เพื่อ UX ที่ดีกว่า
    countryOfOrigin: '',
});

// --- Theming (Receiver from Store) ---
const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'dark bg-gray-950 text-slate-200'
        : 'light bg-slate-50 text-slate-800';
});

// --- Countdown Logic ---
const startCountdown = () => {
    if (countdown.value > 0) {
        setTimeout(() => {
            countdown.value--
            startCountdown()
        }, 1000)
    }
}

// --- Validation State ---
const isNameValid = ref(false);
const isWebsiteUrlValid = ref(true);
const isCountryOfOriginValid = ref(true);
const nameError = ref('');
const websiteUrlError = ref('');
const countryOfOriginError = ref('');


// --- Lifecycle ---
onMounted(async () => {
    if (id) {
        const data = await getItemById(`${import.meta.env.VITE_BACKEND}/v1/brands`, id)
        if (data && data.id) {
            const formattedBrand = {
                name: data.name,
                websiteUrl: data.websiteUrl,
                isActive: data.isActive,
                countryOfOrigin: data.countryOfOrigin,
            }
            brand.value = { ...formattedBrand }
            originalBrand.value = { ...formattedBrand }

            // ตั้งค่า validation flags ตามข้อมูลที่โหลดมา
            isNameValid.value = !!data.name && data.name.length <= 30;

        } else if (!data || data?.status === 404) {
            showNotFoundPopup.value = true
            startCountdown()
            setTimeout(() => {
                router.push('/brands')
            }, 3000)
        }
    }
});

// --- Watchers for Validation ---
// ✨ FIX: เอา { immediate: true } ออก เพื่อไม่ให้ validation ทำงานตอนโหลดหน้า Add
watch(() => brand.value.name, (newVal) => {
    const name = newVal?.trim() ?? ''
    if (!name || name.length > 30) {
        nameError.value = 'Brand name must be 1-30 characters long.'
        isNameValid.value = false
    } else {
        nameError.value = ''
        isNameValid.value = true
    }
});

watch(() => brand.value.websiteUrl, (newVal) => {
    const url = newVal?.trim() ?? ''
    const pattern = /^(https?:\/\/)?([\w\-]+\.)+[\w\-]+(\/[\w\-._~:/?#[\]@!$&'()*+,;=]*)?$/

    if (!url) {
        websiteUrlError.value = ''
        isWebsiteUrlValid.value = true
    } else if (url.length > 80 || !pattern.test(url)) {
        websiteUrlError.value = 'Please enter a valid URL (e.g., example.com).'
        isWebsiteUrlValid.value = false
    } else {
        websiteUrlError.value = ''
        isWebsiteUrlValid.value = true
    }
});

watch(() => brand.value.countryOfOrigin, (newVal) => {
    const country = newVal?.trim() ?? ''
    if (country.length > 80) {
        countryOfOriginError.value = 'Country must be 1-80 characters long.'
        isCountryOfOriginValid.value = false
    } else {
        countryOfOriginError.value = ''
        isCountryOfOriginValid.value = true
    }
});

// --- Computed Properties for Form State ---
const isModified = computed(() => {
    if (!originalBrand.value) return true; // Always true for new items
    return Object.keys(brand.value).some(key => String(brand.value[key]) !== String(originalBrand.value[key]))
});

const isFormValid = computed(() => {
    return isNameValid.value && isWebsiteUrlValid.value && isCountryOfOriginValid.value;
});

// --- Form Submission ---
const submitForm = async () => {
    // Trigger validation for untouched fields
    if (brand.value.name === '') {
        nameError.value = 'Brand name must be 1-30 characters long.';
        isNameValid.value = false;
    }

    if (!isFormValid.value) {
        responseMessage.value = 'Please correct the errors before submitting.';
        return;
    }

    if (isEditMode.value) {
        showConfirmationEditPopup.value = true;
    } else {
        showConfirmationAddPopup.value = true;
    }
};

const confirmAddItem = async () => {
    isLoading.value = true;
    showConfirmationAddPopup.value = false;

    const newBrand = {
        name: brand.value.name.trim(),
        websiteUrl: brand.value.websiteUrl?.trim() || null,
        isActive: brand.value.isActive,
        countryOfOrigin: brand.value.countryOfOrigin?.trim() || null,
    };

    try {
        const result = await addItem(`${import.meta.env.VITE_BACKEND}/v1/brands`, newBrand);
        if (result.status !== 201) throw new Error('Add failed');
        
        setTimeout(() => {
            isLoading.value = false;
            router.push({ path: '/brands', query: { addSuccess: 'true' } });
        }, 500);
    } catch (err) {
        console.error(err);
        isLoading.value = false;
        router.push({ path: '/brands', query: { addFail: 'true' } });
    }
};

const confirmEditItem = async () => {
    isLoading.value = true;
    showConfirmationEditPopup.value = false;

     const editedBrand = {
        name: brand.value.name.trim(),
        websiteUrl: brand.value.websiteUrl?.trim() || null,
        isActive: brand.value.isActive,
        countryOfOrigin: brand.value.countryOfOrigin?.trim() || null,
    };

    try {
        const result = await editItem(`${import.meta.env.VITE_BACKEND}/v1/brands`, id, editedBrand);
        if (result.status !== 200) throw new Error('Edit failed');

        setTimeout(() => {
            isLoading.value = false;
            router.push({ path: '/brands', query: { editSuccess: 'true' } });
        }, 500);
    } catch (err) {
        console.error(err);
        isLoading.value = false;
        router.push({ path: '/brands', query: { editFail: 'true' } });
    }
};

const cancelAction = () => {
    showConfirmationAddPopup.value = false;
    showConfirmationEditPopup.value = false;
};
</script>

<template>
    <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden transition-colors duration-500">
        <div class="absolute inset-0 w-full h-full opacity-20 pointer-events-none">
            <div class="absolute w-96 h-96 bg-blue-500 rounded-full blur-[120px] opacity-30 top-10 left-10 animate-blob"></div>
            <div class="absolute w-80 h-80 bg-purple-500 rounded-full blur-[120px] opacity-30 bottom-10 right-10 animate-blob animation-delay-2000"></div>
        </div>

        <main class="relative z-10 p-4 sm:p-8">
            <nav class="mb-6 max-w-2xl mx-auto text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                <router-link to="/sale-items" class="hover:text-blue-400 transition-colors duration-300">Home</router-link>
                <span class="mx-2">/</span>
                <router-link to="/brands" class="hover:text-blue-400 transition-colors duration-300">Brand List</router-link>
                <span class="mx-2">/</span>
                <span class="font-medium" :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">
                    {{ isEditMode ? 'Edit Brand' : 'New Brand' }}
                </span>
            </nav>

            <div class="max-w-2xl mx-auto rounded-3xl p-6 sm:p-10" :class="theme === 'dark' ? 'bg-black/20 backdrop-blur-xl border border-white/10' : 'bg-white/50 backdrop-blur-xl border'">
                <div class="text-center mb-8">
                     <img :src="brandLogo" alt="Brand Logo" class="w-24 h-24 object-contain mb-4 mx-auto" />
                     <h2 class="text-3xl font-bold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">
                        {{ isEditMode ? `Edit ${originalBrand?.name || 'Brand'}` : 'Create a New Brand' }}
                    </h2>
                </div>
               
                <div class="space-y-6">
                    <div>
                        <label class="block text-sm font-semibold mb-2" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Brand Name <span class="text-red-500">*</span></label>
                        <input v-model="brand.name" type="text" placeholder="e.g., Apple" class="itbms-name w-full p-3 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" :class="theme === 'dark' ? 'bg-white/5 border border-white/10' : 'bg-black/5 border border-black/10'" />
                        <p v-if="nameError" class="itbms-message text-red-500 text-xs mt-1">{{ nameError }}</p>
                    </div>
                    <div>
                        <label class="block text-sm font-semibold mb-2" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Website URL</label>
                        <input v-model="brand.websiteUrl" type="url" placeholder="e.g., https://www.apple.com" class="itbms-websiteUrl w-full p-3 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" :class="theme === 'dark' ? 'bg-white/5 border border-white/10' : 'bg-black/5 border border-black/10'" />
                        <p v-if="websiteUrlError" class="itbms-message text-red-500 text-xs mt-1">{{ websiteUrlError }}</p>
                    </div>
                     <div>
                        <label class="block text-sm font-semibold mb-2" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Country of Origin</label>
                        <input v-model="brand.countryOfOrigin" type="text" placeholder="e.g., USA" class="itbms-countryOfOrigin w-full p-3 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" :class="theme === 'dark' ? 'bg-white/5 border border-white/10' : 'bg-black/5 border border-black/10'" />
                        <p v-if="countryOfOriginError" class="itbms-message text-red-500 text-xs mt-1">{{ countryOfOriginError }}</p>
                    </div>
                    <div class="flex items-center justify-between pt-2">
                         <label class="text-sm font-semibold" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Brand Status</label>
                         <label for="isActiveSwitch" class="relative inline-flex items-center cursor-pointer">
                            <input v-model="brand.isActive" type="checkbox" id="isActiveSwitch" class="sr-only peer" />
                            <div class="w-11 h-6 bg-gray-200 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
                            <span class="ml-3 text-sm font-medium" :class="brand.isActive ? 'text-blue-500' : (theme === 'dark' ? 'text-gray-400' : 'text-gray-500')">{{ brand.isActive ? 'Active' : 'Inactive' }}</span>
                        </label>
                    </div>

                    <div class="flex flex-col sm:flex-row gap-4 pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200/80'">
                        <button @click="submitForm" :disabled="!isFormValid || (isEditMode && !isModified)" class="itbms-save-button w-full px-8 py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:scale-105 disabled:from-gray-500 disabled:to-gray-600 disabled:opacity-50 disabled:cursor-not-allowed disabled:scale-100">
                            {{ isEditMode ? 'Save Changes' : 'Create Brand' }}
                        </button>
                        <router-link to="/brands" class="w-full">
                            <button class="itbms-cancel-button w-full px-8 py-3 font-semibold rounded-full transition-all duration-300 transform hover:scale-105" :class="theme === 'dark' ? 'bg-white/10 text-white hover:bg-white/20' : 'bg-black/5 text-black hover:bg-black/10'">
                                Cancel
                            </button>
                        </router-link>
                    </div>
                </div>
            </div>
        </main>
        
        <transition name="bounce-popup">
            <div v-if="showConfirmationAddPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm adding the brand</h2>
                    <p class="itbms-message mb-6 text-lg">Do you want to add this brand?</p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmAddItem"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95">Yes</button>
                        <button @click="cancelAction"
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
                        <button @click="confirmEditItem"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95">Yes</button>
                        <button @click="cancelAction"
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
    </div>
</template>

<style scoped>
/* --- New styles from LandingPage for background animation --- */
@keyframes blob {
  0% { transform: scale(1) translate(0px, 0px); }
  33% { transform: scale(1.1) translate(30px, -50px); }
  66% { transform: scale(0.9) translate(-20px, 20px); }
  100% { transform: scale(1) translate(0px, 0px); }
}
.animate-blob {
  animation: blob 7s infinite ease-in-out;
}
.animation-delay-2000 {
  animation-delay: 2s;
}

/* --- Transitions for Popups --- */
.bounce-popup-enter-active,
.bounce-popup-leave-active {
    transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.bounce-popup-enter-from,
.bounce-popup-leave-to {
    transform: scale(0.8);
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
    to { transform: rotate(360deg); }
}
.animate-spin {
    animation: spin 1s linear infinite;
}

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