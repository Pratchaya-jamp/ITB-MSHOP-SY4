<script setup>
import { ref, computed } from 'vue';
import { theme } from '@/stores/themeStore.js';
import { addItem } from "@/libs/fetchUtilsOur";

// --- State for UI ---
const email = ref('');
const isLoading = ref(false);
const formSubmitted = ref(false);
const submissionSuccess = ref(false);

// --- State for Slide Popup Notification ---
const showNotification = ref(false);
const notificationMessage = ref("");
const notificationSuccess = ref(false);
let notificationTimeout = null;

// --- Theming ---
const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

// --- Functions ---
const triggerNotification = (message, isSuccess) => {
  if (notificationTimeout) {
    clearTimeout(notificationTimeout);
  }
  notificationMessage.value = message;
  notificationSuccess.value = isSuccess;
  showNotification.value = true;
  notificationTimeout = setTimeout(() => {
    showNotification.value = false;
  }, 3000);
};

const handleSubmit = async () => {
  if (!email.value) {
    triggerNotification("Please enter your email address.", false);
    return;
  }

  isLoading.value = true;
  try {

    // --- เรียกใช้ฟังก์ชัน addItem() ---
    const data  = await addItem(`${import.meta.env.VITE_BACKEND}/v2/send-email?email=${encodeURIComponent(email.value)}`); // ไม่มี body ก็ส่ง {} ได้

    isLoading.value = false;
    formSubmitted.value = true;

    if (data.status === 201) {
      submissionSuccess.value = true;
      triggerNotification("Password reset link sent!", true);
      console.log("✅ Response:", data);
    } else {
      submissionSuccess.value = false;
      triggerNotification(data?.message || "Failed to send link. Please try again.", false);
      console.warn("⚠️ Backend response:", data);
    }

  } catch (error) {
    isLoading.value = false;
    formSubmitted.value = true;
    submissionSuccess.value = false;
    triggerNotification("E-mail not found. Please try again.", false);
    console.error("❌ Error:", error);
  }
};
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans flex items-center justify-center p-6">
    
    <transition name="slide-down">
        <div v-if="showNotification" 
             class="fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 p-4 rounded-lg shadow-lg"
             :class="notificationSuccess ? 'bg-green-500 text-white' : 'bg-red-500 text-white'">
            <svg v-if="notificationSuccess" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span class="font-semibold">{{ notificationMessage }}</span>
        </div>
    </transition>

    <div class="w-full max-w-md animate-fade-in-up">
      <div class="p-8 rounded-2xl" :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm'">
        
        <div v-if="!formSubmitted" class="text-center">
          <h1 class="text-3xl font-extrabold tracking-tight mb-3" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Forgot Password?</h1>
          <p class="mb-8" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
            No worries, we'll send you reset instructions.
          </p>
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <div>
              <label for="email" class="block text-left font-semibold mb-2">Email Address</label>
              <input 
                type="email" 
                id="email" 
                v-model="email"
                class="w-full p-3 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition"
                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
                placeholder="you@example.com"
              />
            </div>
            <button 
              type="submit" 
              class="w-full py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1">
              Send Reset Link
            </button>
          </form>
        </div>

        <div v-else class="text-center">
            <div v-if="submissionSuccess">
                <div class="mx-auto mb-6 w-16 h-16 flex items-center justify-center rounded-full bg-green-500/10 text-green-500">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 19v-8.93a2 2 0 01.89-1.664l7-4.666a2 2 0 012.22 0l7 4.666A2 2 0 0121 10.07V19M3 19a2 2 0 002 2h14a2 2 0 002-2M3 19l6.75-4.5M21 19l-6.75-4.5M3 10l6.75 4.5M21 10l-6.75 4.5m0 0l-1.14.76a2 2 0 01-2.22 0l-1.14-.76" /></svg>
                </div>
                <h1 class="text-3xl font-extrabold tracking-tight mb-3" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Check your email</h1>
                <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                    We've sent a password reset link to <span class="font-bold text-indigo-500 dark:text-indigo-400">{{ email }}</span>.
                </p>
            </div>
            <div v-else>
                <div class="mx-auto mb-6 w-16 h-16 flex items-center justify-center rounded-full bg-red-500/10 text-red-500">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                </div>
                 <h1 class="text-3xl font-extrabold tracking-tight mb-3" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Something Went Wrong</h1>
                <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                    We couldn't send a reset link. Please check your email address and try again.
                </p>
            </div>
        </div>

      </div>
    </div>

    <transition name="fade-background">
        <div v-if="isLoading" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
            <div class="p-8 rounded-2xl shadow-xl text-center" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-white'">
                <svg class="animate-spin h-8 w-8 text-indigo-500 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                <p class="text-lg font-medium">Sending...</p>
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
  animation: fade-in-up 0.8s ease-out forwards;
}


.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}
.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-150%);
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
</style>
