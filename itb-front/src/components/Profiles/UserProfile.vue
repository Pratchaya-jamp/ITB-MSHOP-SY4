<script setup>
import { useRouter, useRoute } from 'vue-router'
import { computed, ref, onMounted, watch } from 'vue'
import { getItemByIdWithAuth } from '@/libs/fetchUtilsOur';
import Cookies from 'js-cookie'
import { theme } from '@/stores/themeStore.js' 

const router = useRouter()
const route = useRoute()

const userPicture = ref({ image: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' })
const userProfile = ref(null) 
const showUpdateSuccess = ref(false) 
const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

const isSeller = computed(() => userProfile.value?.userType === 'Seller')

const navigateToEdit = () => {
    router.push('/profile/edit')
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

const fetchUserProfile = async () => {
    const token = Cookies.get('access_token');

    if (!token) {
        console.error('Authentication error: No access token found in storage.');
        router.push('/signin'); 
        return;
    }

    const decodedToken = decodeJwtToken(token);

    if (!decodedToken || !decodedToken.id) {
        console.error('Invalid token payload: Could not find user ID.');
        router.push('/signin');
        return;
    }

    const userId = decodedToken.id;

    try {
        const response = await getItemByIdWithAuth(
            `${import.meta.env.VITE_BACKEND}/v2/users`,
            userId,
            token
        );
        
        if (response.status === 401 || response.status === 403) {
            console.error('Authentication error: Unauthorized or Forbidden');
            router.push('/signin');
            return;
        }
        
        if (!response.ok) {
            throw new Error(`Failed to fetch user profile: ${response.statusText}`);
        }

        const data = await response.json();
        data.userType = decodedToken.role === 'SELLER' ? 'Seller' : 'Buyer';
        userProfile.value = data;
    } catch (error) {
        console.error('Error fetching user profile:', error);
    }
};

const closeSuccessPopup = async () => {
  showUpdateSuccess.value = false
  await fetchUserProfile();
  router.replace({ path: route.path, query: {} });
}

watch(
  () => route.query.updateSuccess,
  (updateSuccess) => {
    if (updateSuccess === 'true') {
      setTimeout(() => {
        showUpdateSuccess.value = true
      }, 200)
    }
  },
  { immediate: true }
)

onMounted(() => {
    fetchUserProfile();
});

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
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden p-6 md:py-16 flex items-center justify-center transition-colors duration-500">
    <div class="absolute inset-0 w-full h-full opacity-20 pointer-events-none">
        <div class="absolute w-96 h-96 bg-blue-500 rounded-full blur-[100px] opacity-40 top-1/4 left-1/4 animate-blob"></div>
        <div class="absolute w-80 h-80 bg-purple-500 rounded-full blur-[100px] opacity-40 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>
    
    <div v-if="userProfile" class="relative z-10 w-full max-w-2xl animate-fade-in-up">
      <div class="p-8 rounded-3xl space-y-8" :class="theme === 'dark' ? 'bg-gray-950/70 backdrop-blur-xl border border-white/10' : 'bg-white/70 backdrop-blur-xl border border-black/10'">
        
        <div class="text-center">
          <div class="relative inline-block mb-4">
            <img :src="userPicture.image" alt="User Profile Picture" 
                 class="w-28 h-28 rounded-full object-cover shadow-lg" />
            <div class="absolute inset-0 rounded-full ring-4 ring-offset-4 transition-all" 
                :class="theme === 'dark' ? 'ring-blue-500/50 ring-offset-gray-950' : 'ring-blue-500/50 ring-offset-white'">
            </div>
          </div>
          <h1 class="text-3xl font-bold">{{ userProfile.nickname }}</h1>
          <p class="font-semibold mt-1 px-3 py-1 inline-block rounded-full text-sm"
             :class="isSeller ? (theme === 'dark' ? 'bg-purple-500/20 text-purple-300' : 'bg-purple-100 text-purple-700') : (theme === 'dark' ? 'bg-blue-500/20 text-blue-300' : 'bg-blue-100 text-blue-700')">
            {{ userProfile.userType }}
          </p>
        </div>

        <div class="space-y-4">
          <h2 class="text-lg font-semibold border-b pb-2" :class="theme === 'dark' ? 'border-white/10' : 'border-black/10'">Account Details</h2>
          <div class="p-4 rounded-xl flex items-start gap-4" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mt-1 flex-shrink-0" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
            <div>
              <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Full Name</p>
              <p class="font-semibold text-base">{{ userProfile.fullname }}</p>
            </div>
          </div>
          <div class="p-4 rounded-xl flex items-start gap-4" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mt-1 flex-shrink-0" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
            <div>
              <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Email</p>
              <p class="font-semibold text-base">{{ userProfile.email }}</p>
            </div>
          </div>
        </div>

        <div v-if="isSeller" class="space-y-4">
          <h2 class="text-lg font-semibold border-b pb-2" :class="theme === 'dark' ? 'border-white/10' : 'border-black/10'">Seller Information</h2>
          <div class="p-4 rounded-xl flex items-start gap-4" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mt-1 flex-shrink-0" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
            <div>
              <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Mobile</p>
              <p class="font-semibold text-base">{{ maskedMobileNumber }}</p>
            </div>
          </div>
          <div class="grid sm:grid-cols-2 gap-4">
            <div class="p-4 rounded-xl flex items-start gap-4" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mt-1 flex-shrink-0" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
              <div>
                <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Bank Name</p>
                <p class="font-semibold text-base text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">{{ userProfile.bankName }}</p>
              </div>
            </div>
            <div class="p-4 rounded-xl flex items-start gap-4" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mt-1 flex-shrink-0" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
              <div>
                <p class="text-sm" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Bank Account</p>
                <p class="font-semibold text-base">{{ maskedBankNumber }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <div class="pt-4">
          <button @click="navigateToEdit" class="w-full px-10 py-4 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:scale-105">
            Edit Profile
          </button>
        </div>
      </div>
    </div>
    
    <div v-else class="relative z-10 text-center">
      <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Loading profile...</p>
    </div>

    <transition name="bounce-popup">
      <div v-if="showUpdateSuccess"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <DotLottieVue src="/sy4/animation/Success.lottie" autoplay class="w-24 h-24 mx-auto mb-4" />
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">Profile data is updated successfully!</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
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
@keyframes blob {
    0% { transform: scale(1) translate(0px, 0px); }
    33% { transform: scale(1.1) translate(30px, -50px); }
    66% { transform: scale(0.9) translate(-20px, 20px); }
    100% { transform: scale(1) translate(0px, 0px); }
}
.animate-fade-in-up { animation: fade-in-up 0.6s ease-out forwards; }
.animate-blob { animation: blob 7s infinite ease-in-out; }
.animation-delay-2000 { animation-delay: 2s; }

/* Popup transitions */
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