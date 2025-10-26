<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';
import { resetPasswordForgot,addItem,editItem ,editItemWithAuth} from '@/libs/fetchUtilsOur';

const route = useRoute();
const router = useRouter();

// --- State for UI ---
const token = ref(null);
const emailToReset = ref('mock-user@example.com'); // อีเมลจำลอง
const password = ref('');
const confirmPassword = ref('');
const isLoading = ref(true); // เริ่มต้นด้วยการโหลด (เพื่อ Verify token)
const isVerified = ref(false); // ควบคุมการแสดงฟอร์ม
const isSubmitting = ref(false); // ควบคุม Loading ตอน Submit

// --- State for Slide Popup Notification ---
const showNotification = ref(false);
const notificationMessage = ref("");
const notificationSuccess = ref(false);
let notificationTimeout = null;

// --- Theming (มาจาก Store) ---
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

// --- ✨ 1. Mock Function: Verify Token ---
const verifyToken = async () => {
  token.value = route.query.token; 
 // ล้าง query
router.replace({ query: {} });
  if (!token.value) {
    isLoading.value = false;
    isVerified.value = false;
    triggerNotification("No token provided.", false);
    return;
  }

  try {
    isLoading.value = true;
    
    const data = await addItem(`${import.meta.env.VITE_BACKEND}/v2/verify-password?token=${encodeURIComponent(token.value)}`);
console.log(data);
   if (data.status === 201){
    // data จะเป็น user object จาก backend
    emailToReset.value = data.data.email;
    isVerified.value = true;
    triggerNotification("Token verified. You can reset your password.", true);
  }
  } catch (err) {
    isVerified.value = false;
    triggerNotification("Invalid Token, Please request a new one to continue.", false);
  } finally {
    isLoading.value = false;
  }
};

// --- ✨ 2. Mock Function: Handle Submit ---
const handleSubmit = async () => {
  if (password.value !== confirmPassword.value) {
    triggerNotification("Passwords do not match.", false);
    return;
  }

const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).+$/

  if (password.value.length < 8) {
    triggerNotification("Password must be at least 8 characters.", false);
    return;
  } else if (password.value.length >14){
    triggerNotification("Password must be no more than 14 characters long.", false);
    return
  } else if(!passwordRegex.test(password.value)){
    triggerNotification("Password must include upper, lower, number, and special character.", false);
    return
  }

  if (!token.value || typeof token.value !== 'string') {
    triggerNotification("Invalid or missing token.", false);
    return;
  }

  isSubmitting.value = true;

  try {
    const response = await resetPasswordForgot(
      `${import.meta.env.VITE_BACKEND}/v2/reset-password-forgot`,
      token.value,   // ✅ ส่ง reset token ใน header
      { newPassword: password.value, confirmPassword: confirmPassword.value }
    );

    if (response.status === 200 || response.status === 204) {
      triggerNotification("Password reset successfully! Redirecting...", true);
      setTimeout(() => router.push('/signin'), 2000);
    } else {
      triggerNotification("Failed to reset password.", false);
    }

  } catch (error) {
    console.error('Error resetting password:', error);
    triggerNotification('A network error occurred. Please try again.', false);
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  verifyToken();
});
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
        
        <div v-if="isLoading" class="text-center py-10">
          <svg class="animate-spin h-12 w-12 text-indigo-500 mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <h2 class="mt-4 text-xl font-semibold">Verifying your link...</h2>
        </div>

        <div v-else-if="!isVerified" class="text-center">
            <div class="mx-auto mb-6 w-16 h-16 flex items-center justify-center rounded-full bg-red-500/10 text-red-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            </div>
            <h1 class="text-3xl font-extrabold tracking-tight mb-3" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Link Invalid</h1>
            <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
                This password reset link is either invalid or has expired. Please request a new one.
            </p>
        </div>

        <div v-else class="text-center">
          <h1 class="text-3xl font-extrabold tracking-tight mb-3" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Set New Password</h1>
          <p class="mb-8" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
            Resetting password for: <span class="font-bold text-indigo-500 dark:text-indigo-400">{{ emailToReset }}</span>
          </p>
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <div>
              <label for="password" class="block text-left font-semibold mb-2">New Password</label>
              <input 
                type="password" 
                id="password" 
                v-model="password"
                class="w-full p-3 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition"
                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
                placeholder="••••••••"
              />
            </div>
             <div>
              <label for="confirmPassword" class="block text-left font-semibold mb-2">Confirm New Password</label>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="confirmPassword"
                class="w-full p-3 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition"
                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
                placeholder="••••••••"
              />
            </div>
            <button 
              type="submit" 
              class="w-full py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1">
              Reset Password
            </button>
          </form>
        </div>

      </div>
    </div>

    <transition name="fade-background">
        <div v-if="isSubmitting" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
            <div class="p-8 rounded-2xl shadow-xl text-center" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-white'">
                <svg class="animate-spin h-8 w-8 text-indigo-500 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                <p class="text-lg font-medium">Resetting Password...</p>
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
