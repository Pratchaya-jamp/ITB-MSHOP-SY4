<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, watch, onMounted } from 'vue';
import { addItem } from '@/libs/fetchUtilsOur';
import { DotLottieVue } from '@lottiefiles/dotlottie-vue';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';

const router = useRouter();
const theme = ref(localStorage.getItem('theme') || 'dark');

const toggleTheme = () => {
  const newTheme = theme.value === 'dark' ? 'light' : 'dark'
  theme.value = newTheme
  localStorage.setItem('theme', newTheme)
}

onMounted(async () => {
  const storedTheme = localStorage.getItem('theme');
  if (storedTheme) {
    theme.value = storedTheme;
  }
})

watch(theme, (newTheme) => {
  document.body.className = newTheme === 'dark' ? 'dark-theme' : '';
});

const themeClass = computed(() => (theme.value === 'dark' ? 'dark bg-gray-900 text-white' : 'light bg-white text-gray-950'))
const iconComponent = computed(() => {
  return theme.value === 'dark'
    ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
    : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-800" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
})

const showPassword = ref(false);
const email = ref('')
const password = ref('')
const loginError = ref('')
const emailactivate = ref(false)
const Pleaseactivate = () => {
  emailactivate.value = false
}

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const message = ref('');
const isLoading = ref(false);

const EmailError = ref('')
const PasswordError = ref('')
const isEmailValid = ref(false)
const isPasswordValid = ref(true)

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

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
  isLoading.value = true;
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
        isLoading.value = false;
        if (userRole === 'SELLER') {
          router.push({ path: '/sale-items/list', query: { loginSuccess: 'true' } });
        } else {
          router.push({ path: '/sale-items', query: { loginSuccess: 'true' } });
        }
      }, 100);
      
    } else if (response.status === 403) {
      emailactivate.value = true
    } else {
      loginError.value = 'Invalid email or password';
    }
  } catch (error) {
    loginError.value = 'Email or Pasword is incorrect.';
  } finally {
    if (!router.currentRoute.value.path.includes('sale-items')) {
      isLoading.value = false;
    }
  }
};

const cardClass = computed(() => {
  return theme.value === 'dark'
    ? 'bg-gray-900 shadow-xl border border-gray-800'
    : 'bg-gray-100 shadow-xl border border-gray-200';
});
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans flex items-center justify-center p-4">
    <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
      <div class="absolute w-96 h-96 bg-gradient-to-r from-orange-500 to-pink-500 rounded-full blur-3xl opacity-50 top-1/4 left-1/4 animate-blob"></div>
      <div class="absolute w-80 h-80 bg-gradient-to-r from-teal-400 to-blue-500 rounded-full blur-3xl opacity-50 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>
    <div :class="cardClass" class="relative z-10 w-full max-w-xl mx-auto p-8 md:p-12 rounded-[2rem] animate-fade-in-up">
      <h2 class="text-4xl font-extrabold text-center mb-2">
        <span class="text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-pink-500">Login</span>
      </h2>
      <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" class="text-center mb-8">Access your ITB MSHOP account.</p>
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <p v-if="EmailError" class="itbms-message text-red-500 text-sm mb-1">{{ EmailError }}</p>
        <input type="email" v-model="email" placeholder="Email" maxlength="50"
               class="itbms-email w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" />
        <div class="relative">
          <p v-if="PasswordError" class="itbms-message text-red-500 text-sm mb-1">{{ PasswordError }}</p>
          <input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="Password" maxlength="14"
                 class="itbms-password w-full p-4 rounded-xl pr-12 placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" />
          <button type="button" @click="togglePasswordVisibility" class="absolute inset-y-0 right-0 pr-3 flex items-center text-sm leading-5">
            <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/>
            </svg>
          </button>
        </div>
        <p v-if="loginError" class="itbms-message text-red-500 text-sm mt-1">{{ loginError }}</p>
        <button type="button" @click="handleSubmit"
          class="itbms-signin-button w-full px-10 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
          :class="{'opacity-50 cursor-not-allowed': !isFormValid}"
          :disabled="!isFormValid">
          Login
        </button>
      </form>
      <div class="text-center mt-6">
        <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
          Don't have an account?
          <router-link to="/registers" class="font-semibold text-orange-500 hover:underline ml-1">
            Sign Up here
          </router-link>
        </p>
      </div>
    </div>
  </div>
  <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
  </button>
  <transition name="bounce-popup">
    <div v-if="emailactivate"
      class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div class="p-6 rounded-3xl shadow-lg text-center"
        :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
        <DotLottieVue src="/sy4/animation/warning.lottie" autoplay class="w-24 h-24 mx-auto mb-4" />
        <h2 class="text-xl font-semibold mb-4">Login Failed</h2>
        <p class="itbms-message mb-4">Please check your email to complete the activation process.</p>
        <button @click="Pleaseactivate"
          class="bg-yellow-500 text-white border-2 border-yellow-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-yellow-500 font-semibold hover:cursor-pointer">Done</button>
      </div>
    </div>
  </transition>
</template>

<style scoped>
/* Animations */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up { animation: fade-in-up 1s ease-out forwards; }
@keyframes blob {
  0% { transform: scale(1) translate(0px, 0px); }
  33% { transform: scale(1.1) translate(30px, -50px); }
  66% { transform: scale(0.9) translate(-20px, 20px); }
  100% { transform: scale(1) translate(0px, 0px); }
}
.animate-blob { animation: blob 7s infinite ease-in-out; }
.animation-delay-2000 { animation-delay: 2s; }
.itbms-bg {
  background-color: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
}
.modal-fade-enter-active {
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}
.modal-fade-leave-active {
  transition: opacity 0.2s ease-in, transform 0.2s ease-in;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
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
</style>