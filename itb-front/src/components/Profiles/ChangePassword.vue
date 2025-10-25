<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { jwtDecode } from 'jwt-decode';
import Cookies from 'js-cookie';
import { theme } from '@/stores/themeStore.js';
import { editItemWithAuth } from '@/libs/fetchUtilsOur'; // Import utility

const router = useRouter();

// --- State for UI ---
const userEmail = ref('');
const userId = ref(null);
const oldPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const isLoading = ref(false); // Loading ตอน Submit

// State สำหรับการแสดงผลรหัสผ่าน
const showOldPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

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

// --- Validation ---
const passwordError = ref('');
const confirmPasswordError = ref('');

const isFormValid = computed(() => {
  // ตรวจสอบว่ากรอกครบ
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    return false;
  }
  // ตรวจสอบว่ารหัสใหม่ตรงกัน
  if (newPassword.value !== confirmPassword.value) {
    confirmPasswordError.value = "Passwords do not match.";
    return false;
  }
  // ตรวจสอบความยาวรหัสใหม่ (ตัวอย่าง)
  if (newPassword.value.length < 8) {
    passwordError.value = "New password must be at least 8 characters.";
    return false;
  }
  
  // ถ้าผ่านหมด
  passwordError.value = '';
  confirmPasswordError.value = '';
  return true;
});

// --- Functions ---
const triggerNotification = (message, isSuccess) => {
  if (notificationTimeout) clearTimeout(notificationTimeout);
  notificationMessage.value = message;
  notificationSuccess.value = isSuccess;
  showNotification.value = true;
  notificationTimeout = setTimeout(() => {
    showNotification.value = false;
  }, 3000);
};

// --- ✨ Logic การ Submit ---
const handleSubmit = async () => {
  if (!isFormValid.value) {
    triggerNotification("Please check your inputs.", false);
    return;
  }

  isLoading.value = true;

  // Controller ที่คุณให้มา: PUT /v2/user/{uid}/reset-password-change
  // เราจะใช้ editItemWithAuth(url, id, body, token)
  const url = `${import.meta.env.VITE_BACKEND}/v2/user`;
  const idForPath = `${userId.value}/reset-password-change`;
  const token = Cookies.get('access_token');
  
  const body = {
    oldPassword: oldPassword.value,
    newPassword: newPassword.value,
    confirmPassword: confirmPassword.value
  };

  try {
    const result = await editItemWithAuth(url, idForPath, body, token, false); // false = ไม่ใช่ multipart

    if (result.status === 200) {
      triggerNotification("Password changed successfully! Redirecting...", true);
      setTimeout(() => {
        router.push('/profile'); // กลับไปหน้า Profile
      }, 2000);
    } else {
      // 401 (รหัสเก่าผิด) หรือ 400 (Bad Request)
      const errorMsg = result.data?.message || "Failed to change password. Please check your old password.";
      triggerNotification(errorMsg, false);
    }
  } catch (error) {
    console.error("Error changing password:", error);
    triggerNotification("An network error occurred. Please try again.", false);
  } finally {
    isLoading.value = false;
  }
};

// --- ✨ Logic การตรวจสอบสิทธิ์ ---
onMounted(() => {
  const token = Cookies.get('access_token');
  if (!token) {
    router.push('/signin'); // ดีดไปหน้า Sign In ถ้าไม่ Login
    return;
  }

  try {
    const decodedToken = jwtDecode(token);
    userEmail.value = decodedToken.email; // ดึง Email มาแสดง
    userId.value = decodedToken.id;       // เก็บ ID ไว้ใช้ยิง API
  } catch (error) {
    console.error("Invalid token:", error);
    router.push('/signin'); // ถ้า Token มั่ว ก็ดีดไป
  }
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
        
        <div class="text-center">
          <h1 class="text-3xl font-extrabold tracking-tight mb-3" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">Change Password</h1>
          <p class="mb-8" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
            Updating password for: <span class="font-bold text-indigo-500 dark:text-indigo-400">{{ userEmail }}</span>
          </p>
        </div>
          
        <form @submit.prevent="handleSubmit" class="space-y-5">
            <div>
              <label for="oldPassword" class="block text-left font-semibold mb-2">Old Password</label>
              <div class="relative">
                <input 
                  :type="showOldPassword ? 'text' : 'password'" 
                  id="oldPassword" 
                  v-model="oldPassword"
                  class="w-full p-3 pr-12 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition"
                  :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
                  placeholder="••••••••"
                />
                <button type="button" @click="showOldPassword = !showOldPassword" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
                  <svg v-if="showOldPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
                </button>
              </div>
            </div>
            
            <div>
              <label for="newPassword" class="block text-left font-semibold mb-2">New Password</label>
               <div class="relative">
                <input 
                  :type="showNewPassword ? 'text' : 'password'" 
                  id="newPassword" 
                  v-model="newPassword"
                  class="w-full p-3 pr-12 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition"
                  :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
                  placeholder="••••••••"
                />
                <button type="button" @click="showNewPassword = !showNewPassword" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
                  <svg v-if="showNewPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
                </button>
              </div>
              <p v-if="passwordError" class="text-red-500 text-xs mt-1 px-2">{{ passwordError }}</p>
            </div>

            <div>
              <label for="confirmPassword" class="block text-left font-semibold mb-2">Confirm New Password</label>
              <div class="relative">
                <input 
                  :type="showConfirmPassword ? 'text' : 'password'" 
                  id="confirmPassword" 
                  v-model="confirmPassword"
                  class="w-full p-3 pr-12 rounded-lg border-0 outline-none focus:ring-2 focus:ring-indigo-500 transition"
                  :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
                  placeholder="••••••••"
                />
                <button type="button" @click="showConfirmPassword = !showConfirmPassword" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
                  <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
                </button>
              </div>
              <p v-if="confirmPasswordError" class="text-red-500 text-xs mt-1 px-2">{{ confirmPasswordError }}</p>
            </div>

            <div class="flex flex-col sm:flex-row gap-4 pt-4">
              <button 
                type="submit" 
                :disabled="!isFormValid || isLoading"
                class="w-full py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1 disabled:bg-slate-400 disabled:shadow-none disabled:transform-none dark:disabled:bg-gray-600">
                {{ isLoading ? 'Saving...' : 'Save Password' }}
              </button>
              <button 
                type="button" 
                @click="router.push('/profile/edit')"
                class="w-full py-3 font-semibold rounded-full transition-all"
                :class="theme === 'dark' ? 'hover:bg-gray-700' : 'hover:bg-slate-100'">
                Cancel
              </button>
            </div>
          </form>
      </div>
    </div>

    <transition name="fade-background">
        <div v-if="isLoading" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
            <div class="p-8 rounded-2xl shadow-xl text-center" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-white'">
                <svg class="animate-spin h-8 w-8 text-indigo-500 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                <p class="text-lg font-medium">Saving Changes...</p>
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
