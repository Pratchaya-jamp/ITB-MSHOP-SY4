<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, watch, onMounted } from 'vue';
import { addItem } from '@/libs/fetchUtilsOur';

const router = useRouter();
const theme = ref(localStorage.getItem('theme') || 'dark');
// 2. ฟังก์ชันสำหรับสลับ Theme และอัปเดต localStorage
const toggleTheme = () => {
  const newTheme = theme.value === 'dark' ? 'light' : 'dark'
  theme.value = newTheme
  localStorage.setItem('theme', newTheme) // บันทึกค่าใหม่ลงใน localStorage
}

// 3. ตั้งค่า Event Listener เมื่อ component ถูก mount
onMounted(async () => {
  // ตั้งค่า theme ตาม localStorage
  const storedTheme = localStorage.getItem('theme');
  if (storedTheme) {
    theme.value = storedTheme;
  }
})

// 4. Watcher สำหรับอัปเดต class ของ body ตาม theme
watch(theme, (newTheme) => {
  document.body.className = newTheme === 'dark' ? 'dark-theme' : '';
});

// โค้ดส่วนอื่นๆ ที่เกี่ยวกับ Theme ยังคงเดิม
const themeClass = computed(() => (theme.value === 'dark' ? 'dark bg-gray-900 text-white' : 'light bg-white text-gray-950'))
const iconComponent = computed(() => {
  return theme.value === 'dark'
    ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>` // sun icon
    : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-800" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>` // moon icon
})
const showPassword = ref(false);

const email = ref('')
const password = ref('')

const loginError =ref('')

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};


const message = ref('');
const isLoading = ref(false);

const handleSubmit = async () => {
  isLoading.value = true;
  loginError.value = ''; // เพิ่มบรรทัดนี้เพื่อล้าง error เก่า
  
  try {
    const data = await addItem(
      'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/users/authentications',
      {
        email: email.value,
        password: password.value,
      }
    );
    
    if (data.status === 200) {
      localStorage.setItem('access_token', data.data?.access_token);
      localStorage.setItem('refresh_token', data.data?.refresh_token);
      message.value = 'Login successful!';
      setTimeout(() => {
        isLoading.value = false;
        router.push({ path: '/sale-items', query: { loginSuccess: 'true' } });
      }, 1000);
    } else {
      loginError.value = 'Invalid email or password'; // แก้ไข: ย้ายมาไว้ใน else
    }
  } catch (error) {
    loginError.value = 'Login failed: ' + (error.message || 'Unknown error');
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
        <input type="email" v-model="email" placeholder="Email" required
               class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" />
        
        <div class="relative">
          <input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="Password" required
                 class="w-full p-4 rounded-xl pr-12 placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" />
          <button type="button" @click="togglePasswordVisibility" class="absolute inset-y-0 right-0 pr-3 flex items-center text-sm leading-5">
            <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
              <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.958 10.958 0 0020 10c-1.274-4.057-5.064-7-9.542-7-1.254 0-2.457.246-3.564.733l-2.022-2.022zM8.68 5.61A6.98 6.98 0 0010 4a9 9 0 018.354 5.646l-2.493-2.493zM3.646 10c.937 2.062 2.825 3.513 4.965 4.067l-2.5-2.5a4 4 0 01-2.465-1.567zm12.923-3.61l-3.076 3.076a4 4 0 01-5.584 5.584L3.707 17.293a1 1 0 101.414 1.414l12-12a1 1 0 00-1.414-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
        <p v-if="loginError" class="itbms-message text-red-500 text-sm mt-1">{{ loginError }}</p>
        <button type="submit" class="w-full px-10 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105 hover:cursor-pointer">
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
</style>
