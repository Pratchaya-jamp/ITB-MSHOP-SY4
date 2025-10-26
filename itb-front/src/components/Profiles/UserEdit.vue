<script setup>
import { useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import { editItemWithAuth } from '@/libs/fetchUtilsOur';
import Cookies from 'js-cookie'
import { theme } from '@/stores/themeStore.js' 

const router = useRouter()
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

const isSeller = computed(() => userProfile.value?.userType === 'Seller')

const isSaveEnabled = computed(() => {
    if (!userProfile.value || !originalProfile.value) {
        return false;
    }
    return userProfile.value.nickname !== originalProfile.value.nickname ||
           userProfile.value.fullname !== originalProfile.value.fullname;
});

const showConfirmation = () => {
    if (isSaveEnabled.value) {
        confirmUpdateProfile.value = true;
    }
}

const navigateToProfile = () => {
    router.push('/profile')
}

const navigateToChangePassword = () => {
    router.push('/change-password')
}

const confirmSave = async () => {
    confirmUpdateProfile.value = false;
    isLoading.value = true;
    const token = Cookies.get('access_token');

    try {
        const result = await editItemWithAuth(
            `${import.meta.env.VITE_BACKEND}/v2/users`,
            userProfile.value.id,
            {
                nickname: userProfile.value.nickname,
                fullname: userProfile.value.fullname
            },
            token
        );

        if (result.status !== 200) {
            throw new Error('Edit failed');
        }

        setTimeout(() => {
            isLoading.value = false;
            router.push({ path: '/profile', query: { updateSuccess: 'true' } });
        }, 1000);
        
    } catch (err) {
        console.error(err);
        isLoading.value = false;
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

const fetchUserProfileForEdit = async () => {
    const token = Cookies.get('access_token');
    if (!token) {
        router.push('/signin'); 
        return;
    }
    const decodedToken = decodeJwtToken(token);
    if (!decodedToken || !decodedToken.id) {
        router.push('/signin');
        return;
    }
    const userId = decodedToken.id;
    try {
        const response = await getItemByIdWithAuth(`${import.meta.env.VITE_BACKEND}/v2/users/`,userId,token)

        if (!response.ok) {
            throw new Error(`Failed to fetch user profile`);
        }
        const data = await response.json();
        data.userType = decodedToken.role === 'SELLER' ? 'Seller' : 'Buyer';
        userProfile.value = data;
        originalProfile.value = { ...data };
    } catch (error) {
        console.error('Error fetching user profile:', error);
    }
};

const maskedMobileNumber = computed(() => {
    const mobile = userProfile.value?.mobile;
    if (!mobile || mobile.length <= 4) return mobile;
    const unmaskedPart = mobile.slice(-4, -1);
    const maskedPart = 'x'.repeat(mobile.length - 4);
    return `${maskedPart}${unmaskedPart}x`;
})

const maskedBankNumber = computed(() => {
    const bankNumber = userProfile.value?.bankNumber;
    if (!bankNumber || bankNumber.length <= 4) return bankNumber;
    const unmaskedPart = bankNumber.slice(-4, -1);
    const maskedPart = 'x'.repeat(bankNumber.length - 4);
    return `${maskedPart}${unmaskedPart}x`;
})

onMounted(() => {
    fetchUserProfileForEdit();
});

const cancelSave = () => {
    confirmUpdateProfile.value = false;
}
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans transition-colors duration-500">
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="lg:grid lg:grid-cols-12 lg:gap-x-12">
        
        <aside class="lg:col-span-3 mb-8 lg:mb-0 animate-fade-in-up">
          <div class="p-4 rounded-2xl" :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'">
            <div v-if="userProfile" class="flex items-center gap-4 p-2">
                <img :src="userPicture.image" alt="User Profile" class="w-12 h-12 rounded-full object-cover">
                <div>
                    <h2 class="font-bold text-lg">{{ userProfile.nickname }}</h2>
                </div>
            </div>
            <nav class="mt-4 space-y-1">
              <a href="#" class="group flex items-center px-3 py-2 text-sm font-semibold rounded-lg" :class="theme === 'dark' ? 'bg-indigo-500/10 text-indigo-400' : 'bg-indigo-100 text-indigo-700'">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                Edit Profile
              </a>
              
              <a href="#" @click.prevent="navigateToChangePassword" class="group flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors" :class="theme === 'dark' ? 'hover:bg-gray-700/50 text-slate-300' : 'hover:bg-slate-100 text-slate-700'">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
                Change Password
              </a>

              <a href="#" @click.prevent="navigateToProfile" class="group flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors" :class="theme === 'dark' ? 'hover:bg-gray-700/50 text-slate-300' : 'hover:bg-slate-100 text-slate-700'">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" /></svg>
                Back to Profile
              </a>
            </nav>
          </div>
        </aside>

        <div v-if="userProfile" class="lg:col-span-9 animate-fade-in-up" style="animation-delay: 0.1s;">
          <div class="p-8 rounded-2xl" :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'">
            
            <div class="mb-8">
              <h1 class="text-2xl font-bold">Personal Information</h1>
              <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">Update your personal details here.</p>
            </div>

            <form @submit.prevent="showConfirmation" class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label for="nickname" class="block text-sm font-medium mb-1.5">Nickname</label>
                  <input type="text" id="nickname" v-model="userProfile.nickname" class="w-full p-3 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition" :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'">
                </div>
                <div>
                  <label for="fullname" class="block text-sm font-medium mb-1.5">Full Name</label>
                  <input type="text" id="fullname" v-model="userProfile.fullname" class="w-full p-3 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition" :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'">
                </div>
              </div>
              
              <div>
                <label for="email" class="block text-sm font-medium mb-1.5">Email Address</label>
                <input type="email" id="email" :value="userProfile.email" disabled class="w-full p-3 rounded-lg border-0 outline-none cursor-not-allowed" :class="theme === 'dark' ? 'bg-gray-800/50 text-slate-400' : 'bg-slate-200 text-slate-500'">
              </div>
              <template v-if="isSeller">
                <div class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                    <h2 class="text-xl font-bold mb-6">Seller Information (Read-only)</h2>
                    <div class="space-y-6">
                        <div>
                            <label class="block text-sm font-medium mb-1.5">Mobile Number</label>
                            <input type="text" :value="maskedMobileNumber" disabled class="w-full p-3 rounded-lg border-0 outline-none cursor-not-allowed" :class="theme === 'dark' ? 'bg-gray-800/50 text-slate-400' : 'bg-slate-200 text-slate-500'">
                        </div>
                        <div>
                            <label class="block text-sm font-medium mb-1.5">Bank Name</label>
                            <input type="text" :value="userProfile.bankName" disabled class="w-full p-3 rounded-lg border-0 outline-none cursor-not-allowed" :class="theme === 'dark' ? 'bg-gray-800/50 text-slate-400' : 'bg-slate-200 text-slate-500'">
                        </div>
                        <div>
                            <label class="block text-sm font-medium mb-1.5">Bank Account Number</label>
                            <input type="text" :value="maskedBankNumber" disabled class="w-full p-3 rounded-lg border-0 outline-none cursor-not-allowed" :class="theme === 'dark' ? 'bg-gray-800/50 text-slate-400' : 'bg-slate-200 text-slate-500'">
                        </div>
                    </div>
                </div>
              </template>

              <div class="flex justify-end items-center pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
                <div class="flex gap-4">
                  <button type="button" @click="navigateToProfile" class="px-6 py-2.5 font-semibold rounded-full transition-colors" :class="theme === 'dark' ? 'hover:bg-gray-700' : 'hover:bg-slate-100'">
                    Cancel
                  </button>
                  <button type="submit" :disabled="!isSaveEnabled" class="px-6 py-2.5 text-white font-semibold rounded-full shadow-lg transition-all transform" 
                          :class="isSaveEnabled ? 'bg-indigo-600 hover:bg-indigo-700 hover:-translate-y-1' : 'bg-slate-400 dark:bg-gray-600 opacity-50 cursor-not-allowed'">
                    Save Changes
                  </button>
                </div>
              </div>
            </form>

          </div>
        </div>

        <div v-else class="lg:col-span-9 text-center py-10">
            <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Loading editor...</p>
        </div>
      </div>
    </main>

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
            <svg class="animate-spin h-8 w-8 text-indigo-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
            </svg>
            <p class="itbms-message text-lg font-medium">Saving profile...</p>
        </div>
    </div>
</transition>
  </div>
</template>

<style scoped>
@keyframes fade-in-up {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  animation: fade-in-up 0.5s ease-out forwards;
}

/* Popup transitions */
.bounce-popup-enter-active,
.bounce-popup-leave-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.bounce-popup-enter-from, .bounce-popup-leave-to {
  transform: scale(0.9);
  opacity: 0;
}

.fade-background-enter-active,
.fade-background-leave-active {
  transition: all 0.3s ease;
}
.fade-background-enter-from, .fade-background-leave-to {
  opacity: 0;
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
</style>