<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, watch, onMounted } from 'vue';
import { addItem } from '@/libs/fetchUtilsOur';
import { DotLottieVue } from '@lottiefiles/dotlottie-vue';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';
// 1. Import 'theme' จาก Store ส่วนกลาง
import { theme } from '@/stores/themeStore.js'; // <-- ตรวจสอบว่า Path ไปยังไฟล์ store ถูกต้อง

const router = useRouter();
const showPassword = ref(false);
const email = ref('')
const password = ref('')
const loginError = ref('')
const emailactivate = ref(false)
const isLoading = ref(false);
const message = ref('');
const EmailError = ref('')
const PasswordError = ref('')
const isEmailValid = ref(false)
const isPasswordValid = ref(true)
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

// --- โค้ดส่วน Logic การทำงานทั้งหมดเหมือนเดิม ---

const Pleaseactivate = () => {
  emailactivate.value = false
}

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

watch(email, (val) => {
  if (!val.trim()) {
    EmailError.value = 'Email is required.'
    isEmailValid.value = false
  } else if (val.length > 50) {
    EmailError.value = 'Email must not exceed 50 characters.'
  } else if (!emailRegex.test(val)) {
    EmailError.value = 'Invalid email format.'
  } else {
    EmailError.value = ''
    isEmailValid.value = true
  }
})

watch(password, (val) => {
  if (val.length > 14) {
    PasswordError.value = 'Password must not exceed 14 characters.'
  } else {
    PasswordError.value = ''
    isPasswordValid.value = true
  }
})

const isFormValid = computed(() => {
  return (
    isPasswordValid.value &&
    isEmailValid.value
  )
})

const handleSubmit = async () => {
  isLoading.value = true; // <-- ปุ่มเปลี่ยนเป็น "Signing In..."
  loginError.value = '';

  try {
    const response = await addItem(
      `${import.meta.env.VITE_BACKEND}/v2/auth/login`,
      {
        email: email.value,
        password: password.value,
      }
    );

    if (response.status === 200) {
      const accessToken = response.data?.access_token;
      Cookies.set('access_token', accessToken);

      let userRole = null;
      try {
        const decodedToken = jwtDecode(accessToken);
        userRole = decodedToken.role; 
      } catch (decodeError) {
        console.error("Failed to decode JWT:", decodeError);
      }
      
      message.value = 'Login successful!';

      setTimeout(() => {
        if (userRole === 'SELLER') {
          router.push({ path: '/sale-items/list', query: { loginSuccess: 'true' } });
        } else {
          router.push({ path: '/sale-items', query: { loginSuccess: 'true' } });
        }
      }, 1000);
      
    } else if (response.status === 403) {
      emailactivate.value = true
      isLoading.value = false;
    } else {
      loginError.value = 'Invalid email or password';
      isLoading.value = false;
    }
  } catch (error) {
    loginError.value = 'Email or Pasword is incorrect.';
    isLoading.value = false;
  }
};

const themeClass = computed(() => (theme.value === 'dark' ? 'dark bg-gray-900 text-slate-200' : 'bg-slate-50 text-slate-800'));
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans flex items-center justify-center p-6 transition-colors duration-300">
    <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
      <div class="absolute w-96 h-96 bg-indigo-500 rounded-full blur-3xl opacity-30 top-1/4 left-1/4 animate-blob"></div>
      <div class="absolute w-80 h-80 bg-sky-500 rounded-full blur-3xl opacity-30 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>
    
    <div :class="theme === 'dark' ? 'bg-gray-800/50 ring-1 ring-white/10' : 'bg-white ring-1 ring-black/5'"
         class="relative z-10 w-full max-w-md mx-auto p-8 md:p-10 rounded-3xl shadow-2xl backdrop-blur-xl animate-fade-in-up">
      <h2 class="text-4xl font-extrabold text-center mb-2" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
        Welcome Back
      </h2>
      <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'" class="text-center mb-8">Access your ITB MSHOP account.</p>
      
      <form @submit.prevent="handleSubmit" class="space-y-5">
        <div>
          <input type="email" v-model="email" placeholder="Email Address" maxlength="50"
                 class="itbms-email w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                 :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" />
          <p v-if="EmailError" class="itbms-message text-red-500 text-xs mt-1 px-2">{{ EmailError }}</p>
        </div>
        
        <div class="relative">
          <input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="Password" maxlength="14"
                 class="itbms-password w-full p-4 rounded-lg pr-12 placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                 :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" />
          <button type="button" @click="togglePasswordVisibility" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
            <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
          </button>
        </div>
        <p v-if="PasswordError" class="itbms-message text-red-500 text-xs -mt-2 px-2">{{ PasswordError }}</p>
        
        <p v-if="loginError" class="itbms-message text-red-500 text-sm text-center pt-2">{{ loginError }}</p>
        
        <div class="pt-4">
          <button type="button" @click="handleSubmit"
            class="itbms-signin-button w-full px-10 py-4 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1"
            :class="{
              'opacity-50 cursor-not-allowed': !isFormValid,
              'opacity-50 pointer-events-none': isLoading
            }"
            :disabled="!isFormValid">
            {{ isLoading ? 'Signing In...' : 'Sign In' }}
          </button>
        </div>
      </form>
      
      <div class="text-center mt-6 text-sm">
        <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Don't have an account?
          <router-link to="/registers" class="font-semibold transition" :class="theme === 'dark' ? 'text-indigo-400 hover:text-indigo-300' : 'text-indigo-600 hover:text-indigo-700'">
            Sign up here
          </router-link>
        </p>
      </div>
    </div>
  </div>

  <transition name="bounce-popup">
    <div v-if="emailactivate"
      class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div class="p-8 rounded-3xl shadow-xl text-center"
        :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
        <DotLottieVue src="/sy4/animation/warning.lottie" autoplay class="w-24 h-24 mx-auto" />
        <h2 class="text-2xl font-bold mt-4 mb-2">Activation Required</h2>
        <p class="itbms-message mb-6" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">Please check your email to activate your account.</p>
        <button @click="Pleaseactivate"
          class="bg-indigo-600 text-white rounded-full px-8 py-3 transition-all duration-300 hover:bg-indigo-700 font-semibold hover:cursor-pointer">
          Done
        </button>
      </div>
    </div>
  </transition>
</template>

<style scoped>
/* Animations and other styles remain unchanged */
.animate-fade-in-up { 
    animation: fade-in-up 0.8s ease-out forwards; 
}
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(30px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
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
@keyframes blob {
  0% { transform: scale(1) translate(0px, 0px); }
  33% { transform: scale(1.1) translate(30px, -50px); }
  66% { transform: scale(0.9) translate(-20px, 20px); }
  100% { transform: scale(1) translate(0px, 0px); }
}
.animate-blob { animation: blob 7s infinite ease-in-out; }
.animation-delay-2000 { animation-delay: 2s; }
</style>