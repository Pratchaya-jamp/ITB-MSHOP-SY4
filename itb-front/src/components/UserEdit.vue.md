<script setup>
import { useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import { editItemWithAuth } from '@/libs/fetchUtilsOur';
import Cookies from 'js-cookie'

const router = useRouter()
const theme = ref(localStorage.getItem('theme') || 'dark')
const userPicture = ref({ image: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' })
const userProfile = ref(null)
const originalProfile = ref(null)
const confirmUpdateProfile = ref(false)
const isLoading = ref(false)

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

const iconComponent = computed(() => {
    return theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
})

const applyTheme = (newTheme) => {
    document.body.className = newTheme === 'dark' ? 'dark-theme' : ''
    localStorage.setItem('theme', newTheme)
    theme.value = newTheme
}

const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(newTheme)
}

const cardClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-900 shadow-xl border border-gray-800'
        : 'bg-gray-100 shadow-xl border border-gray-200'
})

const isSeller = computed(() => userProfile.value?.userType === 'Seller')
const isBuyer = computed(() => userProfile.value?.userType === 'Buyer')

const isFormModified = computed(() => {
    if (!userProfile.value || !originalProfile.value) return false;
    
    // Check for changes in common editable fields
    const commonFieldsChanged = 
        userProfile.value.nickname !== originalProfile.value.nickname ||
        userProfile.value.fullname !== originalProfile.value.fullname ||
        userProfile.value.email !== originalProfile.value.email;

    // Check for changes in fields editable only by Seller
    const sellerFieldsChanged = isSeller.value && userProfile.value.password !== originalProfile.value.password;

    return commonFieldsChanged || sellerFieldsChanged;
})

const isSaveEnabled = computed(() => {
    // ตรวจสอบว่า userProfile และ originalProfile มีข้อมูลครบถ้วน
    if (!userProfile.value || !originalProfile.value) {
        return false;
    }

    // ตรวจสอบการเปลี่ยนแปลงของ nickname และ fullname
    const hasChanges =
        userProfile.value.nickname !== originalProfile.value.nickname ||
        userProfile.value.fullname !== originalProfile.value.fullname;

    // คืนค่า true ถ้ามีการเปลี่ยนแปลง
    return hasChanges;
});

const showConfirmation = () => {
    if (isSaveEnabled.value) {
        confirmUpdateProfile.value = true;
    }
}

const navigateToProfile = () => {
    router.push('/profile')
}

// **Function to handle saving changes to the backend (PUT/PATCH request)**
const confirmSave = async () => {
    confirmUpdateProfile.value = false;
    isLoading.value = true;
    const token = Cookies.get('access_token');

    try {
        const result = await editItemWithAuth(
            'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/users',
            userProfile.value.id,
            {
                nickname: userProfile.value.nickname,
                fullname: userProfile.value.fullname
            },
            token
        );

        if (result.status !== 200 || !result.data?.id) {
            throw new Error('Edit failed');
        }

        setTimeout(() => {
            isLoading.value = false;
            router.push({ path: '/profile', query: { updateSuccess: 'true' } });
        }, 1000);
        
    } catch (err) {
        console.error(err);
        isLoading.value = false; // หากเกิดข้อผิดพลาด ให้ปิดหน้าโหลด
        // responseMessage.value = 'เกิดข้อผิดพลาดในการแก้ไขสินค้า' // หากมีตัวแปรนี้
    }
}



const decodeJwtToken = (token) => {
    try {
        const payload = token.split('.')[1];
        const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decodedPayload);
    } catch (error) {
        console.error('Failed to decode JWT token:', error);
        return null;
    }
};

// **Function to fetch user data for pre-filling the form**
const fetchUserProfileForEdit = async () => {
    const token = Cookies.get('access_token');

    if (!token) {
        console.error('Authentication error: No access token found in storage.');
        router.push('/login'); 
        return;
    }

    const decodedToken = decodeJwtToken(token);

    if (!decodedToken || !decodedToken.id) {
        console.error('Invalid token payload: Could not find user ID.');
        router.push('/login');
        return;
    }

    const userId = decodedToken.id;
    const API_URL = `http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/users/${userId}`;

    try {
        const response = await fetch(API_URL, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.status === 401 || response.status === 403) {
            console.error('Authentication error: Unauthorized or Forbidden');
            router.push('/login');
            return;
        }
        
        if (!response.ok) {
            throw new Error(`Failed to fetch user profile: ${response.statusText}`);
        }

        const data = await response.json();
        data.userType = decodedToken.role === 'SELLER' ? 'Seller' : 'Buyer';
        userProfile.value = data;
        originalProfile.value = { ...data }; // Store original data for comparison
    } catch (error) {
        console.error('Error fetching user profile:', error);
    }
};

onMounted(() => {
    fetchUserProfileForEdit();
    applyTheme(theme.value);
});

// แก้ไขในส่วนนี้
const maskedMobileNumber = computed(() => {
    const mobile = userProfile.value?.mobile;
    if (!mobile || mobile.length <= 4) return mobile;
    const unmaskedPart = mobile.slice(-4, -1);
    const maskedPart = 'x'.repeat(mobile.length - 4);
    return `${maskedPart}${unmaskedPart}x`;
})

// แก้ไขในส่วนนี้
const maskedBankNumber = computed(() => {
    const bankNumber = userProfile.value?.bankNumber;
    if (!bankNumber || bankNumber.length <= 4) return bankNumber;
    const unmaskedPart = bankNumber.slice(-4, -1);
    const maskedPart = 'x'.repeat(bankNumber.length - 4);
    return `${maskedPart}${unmaskedPart}x`;
})

const cancelSave = () => {
    confirmUpdateProfile.value = false;
}
</script>

<template>
    <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden p-6 md:p-20 flex items-center justify-center">
        <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
            <div class="absolute w-96 h-96 bg-gradient-to-r from-orange-500 to-pink-500 rounded-full blur-3xl opacity-50 top-1/4 left-1/4 animate-blob"></div>
            <div class="absolute w-80 h-80 bg-gradient-to-r from-teal-400 to-blue-500 rounded-full blur-3xl opacity-50 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
        </div>
        
        <div class="relative z-10 w-full max-w-2xl animate-fade-in-up">
            <div v-if="userProfile" :class="cardClass" class="p-8 md:p-12 rounded-[2rem] space-y-6">
                <div class="flex justify-center mb-6">
                    <img :src="userPicture.image" alt="User Profile Picture" 
                            class="w-32 h-32 rounded-full border-4 border-orange-500 object-cover shadow-lg transform hover:scale-110 transition-transform duration-300" />
                </div>
                
                <h2 class="text-4xl font-bold text-center mb-6">Edit Profile</h2>

                <div class="space-y-4">
                    <div class="flex flex-col gap-2">
                        <label for="nickname" class="text-sm opacity-75">Nickname</label>
                        <input type="text" id="nickname" v-model="userProfile.nickname"
                                class="itbms-nickname w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                                :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" />
                    </div>

                    <div class="flex flex-col gap-2">
                        <label for="fullname" class="text-sm opacity-75">Fullname</label>
                        <input type="text" id="fullname" v-model="userProfile.fullname"
                                class="itbms-fullname w-full p-4 rounded-xl placeholder-gray-500 transition-all focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                                :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" />
                    </div>

                    <div class="flex flex-col gap-2">
                        <label for="email" class="text-sm opacity-75">Email</label>
                        <input type="email" id="email" v-model="userProfile.email" disabled
                                class="itbms-email w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed"
                                        :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                    </div>

                    <template v-if="isSeller">
                        <div class="flex flex-col gap-2">
                            <label class="text-sm opacity-75">Mobile Number</label>
                            <input type="text" :value="maskedMobileNumber" disabled
                                class="itbms-mobile w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed"
                                :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                        </div>
                        <div class="flex flex-col gap-2">
                            <label class="text-sm opacity-75">Bank Name</label>
                            <input type="text" :value="userProfile.bankName" disabled
                                class="itbms-bankName w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed text-orange-500 font-semibold"
                                :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                        </div>
                        
                        <div class="flex flex-col gap-2">
                            <label class="text-sm opacity-75">Bank Account Number</label>
                            <input type="text" :value="maskedBankNumber" disabled
                                class="itbms-bankAccount w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed"
                                :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                        </div>
                    </template>
                </div>

                <div class="flex flex-col sm:flex-row gap-4 pt-4">
                    <button @click="showConfirmation" 
                        :disabled="!isSaveEnabled"
                        class="itbms-save-button w-full sm:w-1/2 px-10 py-3 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
                        :class="isSaveEnabled ? 'bg-gradient-to-r from-orange-500 to-red-500 hover:-translate-y-1' : 'bg-gray-500 cursor-not-allowed'">
                        Save
                    </button>
                    <button @click="navigateToProfile"
                        class="itbms-cancel-button w-full sm:w-1/2 px-10 py-3 font-semibold rounded-full transition-all duration-300 transform hover:bg-white/10 hover:border-white/50"
                        :class="theme === 'dark' ? 'border-2 border-white/20 text-white hover:bg-white/10 hover:border-white/50' : 'border-2 border-gray-400 text-gray-800 hover:bg-gray-200/50 hover:border-gray-500'">
                        Cancel
                    </button>
                </div>
            </div>
            <div v-else :class="cardClass" class="p-8 md:p-12 rounded-[2rem] text-center">
                <p>Failed to load user profile for editing. Please try again.</p>
            </div>
        </div>
        <transition name="bounce-popup">
    <div v-if="confirmUpdateProfile"
        class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
            :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
            <h2 class="text-2xl font-bold mb-4">Confirm editing profile</h2>
            <p class="itbms-message mb-6 text-lg">Do you want to save changes to this profile?</p>
            <div class="flex justify-center gap-4">
                <button @click="confirmSave"
                    class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
                <button @click="cancelSave"
                    class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">No</button>
            </div>
        </div>
    </div>
</transition>

<transition name="fade-background">
    <div v-if="isLoading"
        class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
        <div class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
            :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
            <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
            </svg>
            <p class="itbms-message text-lg font-medium">Saving profile...</p>
        </div>
    </div>
</transition>
        <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
    </button>
    </div>
</template>

<style scoped>
@keyframes fade-in-up {
    from { opacity: 0; transform: translateY(40px); }
    to { opacity: 1; transform: translateY(0); }
}
@keyframes blob {
    0% { transform: scale(1) translate(0px, 0px); }
    33% { transform: scale(1.1) translate(30px, -50px); }
    66% { transform: scale(0.9) translate(-20px, 20px); }
    100% { transform: scale(1) translate(0px, 0px); }
}
.animate-fade-in-up { animation: fade-in-up 1s ease-out forwards; }
.animate-blob { animation: blob 7s infinite ease-in-out; }
.animation-delay-2000 { animation-delay: 2s; }

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
    transition: background-color 0.3s ease;
}

.fade-background-enter-from {
    background-color: rgba(0, 0, 0, 0);
}

.fade-background-leave-to {
    background-color: rgba(0, 0, 0, 0);
}

.fade-in-out-enter-active,
.fade-in-out-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-in-out-enter-from {
    opacity: 0;
    transform: scale(0.95);
}

.fade-in-out-leave-to {
    opacity: 0;
    transform: scale(1.05);
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

.fixed.bg-black {
    background-color: rgba(0, 0, 0, 0.5);
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.animate-spin {
    animation: spin 1s linear infinite;
}

span {
    white-space: normal;
    word-break: break-word;
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
