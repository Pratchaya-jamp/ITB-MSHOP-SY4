<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'; 
import { useRoute, useRouter } from 'vue-router';
import { DotLottieVue } from '@lottiefiles/dotlottie-vue';
import { addItem } from '@/libs/fetchUtilsOur';
import { theme } from '@/stores/themeStore.js';

const route = useRoute();
const router = useRouter();
const verificationStatus = ref('verifying'); // ✨ 1. เริ่มต้นที่ State 'verifying'
const message = ref('');

const showNotification = ref(false);
const notificationMessage = ref("");
const notificationSuccess = ref(false);
let notificationTimeout = null;
let countdownInterval = null;

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
});

const triggerNotification = (message, isSuccess, duration = null) => {
  if (notificationTimeout) {
    clearTimeout(notificationTimeout);
  }
  notificationMessage.value = message;
  notificationSuccess.value = isSuccess;
  showNotification.value = true;
  
  if (duration) {
    notificationTimeout = setTimeout(() => {
      showNotification.value = false;
    }, duration);
  }
};

const startRedirectCountdown = (seconds) => {
    let remaining = seconds;
    const updateNotification = () => {
        triggerNotification(`Redirecting to Sign In in ${remaining}...`, true, null);
    };
    updateNotification(); 
    countdownInterval = setInterval(() => {
        remaining--;
        if (remaining > 0) {
            updateNotification();
        } else {
            clearInterval(countdownInterval);
            triggerNotification("Redirecting...", true, 1000); 
            router.push('/signin');
        }
    }, 1000);
};

const verifyEmail = async () => {
    const token = route.query.token;

    if (!token) {
        verificationStatus.value = 'error';
        message.value = 'Verification token is missing. Please check the link or request a new one.';
        return;
    }
    router.replace({ query: {} });
    
    // ✨ 2. หน่วงเวลา 2 วินาที (เพื่อให้เห็น Lottie 'verifying')
    setTimeout(async () => {
        try {
            // 3. ยิง API หลังจาก 2 วินาทีผ่านไป
            const response = await addItem(
                `${import.meta.env.VITE_BACKEND}/v2/auth/verify-email?token=${token}`
            );

            if (response.status === 200) {
                verificationStatus.value = 'success';
                message.value = 'Your account has been successfully activated.';
                
                // 4. ถ้าสำเร็จ ค่อยเริ่มนับถอยหลัง 3 วินาที
                startRedirectCountdown(3); 

            } else if (response.status === 409) {
                verificationStatus.value = 'warning';
                message.value = 'This token has already been used. Your account is already active.';
            } else {
                const errorData = response.data; // ใช้ .data จาก fetchUtils
                verificationStatus.value = 'error';
                message.value = errorData.message || 'An error occurred, or the verification link has expired.';
            }
        } catch (error) {
            console.error('Error during email verification:', error);
            verificationStatus.value = 'error';
            message.value = 'A network error occurred. Please check your connection and try again.';
        }
    }, 2000); // 2 วินาที
};

onMounted(() => {
    verifyEmail();
});

onUnmounted(() => {
    if (countdownInterval) {
        clearInterval(countdownInterval);
    }
    if (notificationTimeout) {
        clearTimeout(notificationTimeout);
    }
});
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans overflow-hidden flex items-center justify-center text-center p-4">
    
    <transition name="slide-down">
        <div v-if="showNotification" 
             class="fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 p-4 rounded-lg shadow-lg"
             :class="notificationSuccess ? 'bg-green-500 text-white' : 'bg-red-500 text-white'">
            <svg v-if="notificationSuccess" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span class="font-semibold">{{ notificationMessage }}</span>
        </div>
    </transition>

    <div class="absolute inset-0 w-full h-full opacity-20 pointer-events-none">
        <div class="absolute w-96 h-96 bg-blue-500 rounded-full blur-[100px] opacity-40 top-1/4 left-1/4 animate-blob"></div>
        <div class="absolute w-80 h-80 bg-purple-500 rounded-full blur-[100px] opacity-40 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>

    <div class="relative z-10 p-8 md:p-12 rounded-3xl max-w-lg w-full space-y-4 animate-fade-in-up" 
         :class="theme === 'dark' ? 'bg-gray-950/70 backdrop-blur-xl border border-white/10' : 'bg-white/70 backdrop-blur-xl border border-black/10'">
      
      <div v-if="verificationStatus === 'verifying'">
        <DotLottieVue src="/sy4/animation/loading_fingerprint.lottie" autoplay loop class="w-48 h-48 mx-auto mb-4"/>
        <h1 class="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">
          Verifying Email...
        </h1>
        <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
          Please wait a moment while we activate your account.
        </p>
      </div>
      
      <div v-if="verificationStatus === 'success'">
        <DotLottieVue src="/sy4/animation/Loading_success.lottie" autoplay class="w-48 h-48 mx-auto mb-4"/>
        <h1 class="text-3xl font-bold" :class="theme === 'dark' ? 'text-green-400' : 'text-green-600'">
          Verification Successful!
        </h1>
        <p class="text-lg" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
          {{ message }}
        </p>
        <p class="text-sm" :class="theme === 'dark' ? 'text-gray-500' : 'text-gray-400'">
            Preparing your account...
        </p>
      </div>
      
      <div v-if="verificationStatus === 'error'">
        <DotLottieVue src="/sy4/animation/Loading_failed.lottie" autoplay class="w-48 h-48 mx-auto mb-4"/>
        <h1 class="text-3xl font-bold" :class="theme === 'dark' ? 'text-red-400' : 'text-red-600'">
          Verification Failed
        </h1>
        <p class="text-lg" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
          {{ message }}
        </p>
         <button @click="router.push('/')" class="mt-6 px-8 py-3 font-semibold rounded-full transition-all" :class="theme === 'dark' ? 'bg-white/10 text-white hover:bg-white/20' : 'bg-black/5 text-black hover:bg-black/10'">
            Go to Homepage
        </button>
      </div>

      <div v-if="verificationStatus === 'warning'">
        <DotLottieVue src="/sy4/animation/warning.lottie" autoplay class="w-48 h-48 mx-auto mb-4"/>
        <h1 class="text-3xl font-bold" :class="theme === 'dark' ? 'text-yellow-400' : 'text-yellow-600'">
          Already Verified!
        </h1>
        <p class="text-lg" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
          {{ message }}
        </p>
        <button @click="router.push('/signin')" class="mt-6 px-8 py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full shadow-lg transition-all transform hover:scale-105">
            Go to Login
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Slide Notification */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}
.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-150%);
  opacity: 0;
}

/* Card Animation */
@keyframes fade-in-up {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
    animation: fade-in-up 0.6s ease-out forwards;
}

/* Background Animation */
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
</style>