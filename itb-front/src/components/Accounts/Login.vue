<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { addItem } from '@/libs/fetchUtilsOur';
import { DotLottieVue } from '@lottiefiles/dotlottie-vue';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';
import { theme } from '@/stores/themeStore.js'; 

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
const isPasswordValid = ref(false) 
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

// --- State สำหรับ OTP ---
const currentStage = ref('login'); // 'login' | 'otp'
const otpDigits = ref(new Array(6).fill('')); 
const otp = computed(() => otpDigits.value.join('')); 
const rememberMe = ref(false);
const otpMessage = ref('');
const otpError = ref('');
const resendCooldown = ref(0);
const resendTimer = ref(null);
const isResending = ref(false);

// --- State สำหรับ Slide Notification ---
const showNotification = ref(false);
const notificationMessage = ref("");
const notificationSuccess = ref(false);
let notificationTimeout = null;

const triggerNotification = (message, isSuccess) => {
  if (notificationTimeout) {
    clearTimeout(notificationTimeout);
  }
  notificationMessage.value = message;
  notificationSuccess.value = isSuccess;
  showNotification.value = true;
  notificationTimeout = setTimeout(() => {
    showNotification.value = false;
  }, 3000); // 3 วินาที
};

const startResendTimer = () => {
    if (resendTimer.value) clearInterval(resendTimer.value);
    resendCooldown.value = 60;
    resendTimer.value = setInterval(() => {
        resendCooldown.value--;
        if (resendCooldown.value <= 0) {
            clearInterval(resendTimer.value);
        }
    }, 1000);
};

// --- ฟังก์ชันสำหรับจัดการ 6-Digit OTP Input ---
const handleOtpInput = (e, index) => {
  const input = e.target;
  const value = input.value;
  
  otpDigits.value[index] = value.replace(/[^0-9]/g, '');

  if (value && index < 5) {
    document.getElementById(`otp-input-${index + 1}`).focus();
  }
};

const handleOtpKeydown = (e, index) => {
  if (e.key === 'Backspace' && !otpDigits.value[index] && index > 0) {
    document.getElementById(`otp-input-${index - 1}`).focus();
  }
};

const handleOtpPaste = (e) => {
  const pasteData = e.clipboardData.getData('text').replace(/[^0-9]/g, '').slice(0, 6);
  if (pasteData.length === 6) {
    otpDigits.value = pasteData.split('');
    document.getElementById('otp-input-5').focus();
  }
};

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
    isEmailValid.value = false
  } else if (!emailRegex.test(val)) {
    EmailError.value = 'Invalid email format.'
    isEmailValid.value = false
  } else {
    EmailError.value = ''
    isEmailValid.value = true
  }
})

watch(password, (val) => {
  if (!val.trim()) {
    PasswordError.value = 'Password is required.'
    isPasswordValid.value = false
  } else if (val.length > 14) {
    PasswordError.value = 'Password must not exceed 14 characters.'
    isPasswordValid.value = false
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
  emailactivate.value = false;

  try {
    const response = await addItem(
      `${import.meta.env.VITE_BACKEND}/v2/auth/login`,
      {
        email: email.value,
        password: password.value,
      }
    );

    if (response.status === 201) {
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
      
    } else if (response.status === 202) {
      otpMessage.value = response.data?.message || `We've sent a 6-digit code to ${email.value}.`;
      currentStage.value = 'otp';
      startResendTimer();
      isLoading.value = false;

    } else {
      loginError.value = 'Invalid email or password';
      isLoading.value = false;
    }
  } catch (error) {
    if (error.message && error.message.includes('403')) {
      emailactivate.value = true;
    } else {
      loginError.value = 'Email or Password is incorrect.';
    }
    isLoading.value = false;
  }
};

// --- ✨ FIX: แก้ไข handleOtpSubmit ทั้งหมด ---
const handleOtpSubmit = async () => {
    isLoading.value = true; // ปุ่มขึ้น "Verifying..." และค้างไว้
    otpError.value = '';

    try {
        const response = await addItem(
            `${import.meta.env.VITE_BACKEND}/v2/auth/verify-otp`,
            { 
              email: email.value, 
              otp: otp.value, 
              rememberMe: rememberMe.value 
            }
        );

        if (response.status === 201) { // 201 CREATED (สำเร็จ)
            const accessToken = response.data?.access_token;
            Cookies.set('access_token', accessToken);
            let userRole = null;
            try {
                const decodedToken = jwtDecode(accessToken);
                userRole = decodedToken.role; 
            } catch (decodeError) {
                console.error("Failed to decode JWT:", decodeError);
            }
            
            // 1. ขึ้น Notification สำเร็จ และเริ่มนับถอยหลัง 3 วิ
            triggerNotification("Verification Successful! Redirecting...", true);

            // 2. ปุ่มยังคงเป็น "Verifying..."
            // 3. หลังจาก 3 วินาที ค่อย Redirect
            setTimeout(() => {
                if (userRole === 'SELLER') {
                    router.push({ path: '/sale-items/list', query: { loginSuccess: 'true' } });
                } else {
                    router.push({ path: '/sale-items', query: { loginSuccess: 'true' } });
                }
                isLoading.value = false; // คืนค่าปุ่ม (เผื่อกดย้อนกลับมา)
            }, 3000); // 3 วินาที

        } else if (response.status === 401) {
            // 401 (OTP ผิด)
            otpError.value = 'Invalid or expired OTP.';
            triggerNotification("Invalid or expired OTP.", false);
            isLoading.value = false; // คืนค่าปุ่ม
        } else {
            // 400 (OTP ถูกใช้ไปแล้ว ฯลฯ)
            otpError.value = 'OTP has already been used or is invalid.';
            triggerNotification("OTP has already been used or is invalid.", false);
            isLoading.value = false; // คืนค่าปุ่ม
        }
    } catch (error) {
        otpError.value = 'Failed to verify OTP. Please try again.';
        triggerNotification("Failed to verify OTP. Please try again.", false);
        isLoading.value = false; // คืนค่าปุ่ม
    }
};
// --- (จบส่วน FIX) ---

const handleResendOtp = async () => {
    isResending.value = true;
    loginError.value = '';
    
    try {
        const response = await addItem(
            `${import.meta.env.VITE_BACKEND}/v2/auth/login`,
            { email: email.value, password: password.value }
        );

        if (response.status === 202) {
            otpMessage.value = response.data?.message || `A new 6-digit code has been resent to ${email.value}.`;
            startResendTimer();
            triggerNotification("A new OTP has been sent!", true);
        } else {
            triggerNotification("Failed to resend code. Please try again later.", false);
        }
    } catch (error) {
        triggerNotification("An error occurred while resending.", false);
    } finally {
        isResending.value = false;
    }
};

onUnmounted(() => {
    if (resendTimer.value) clearInterval(resendTimer.value);
    if (notificationTimeout) clearTimeout(notificationTimeout);
});

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans flex items-center justify-center p-6 transition-colors duration-300">
    
    <transition name="slide-down">
        <div v-if="showNotification" 
             class="fixed top-20 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 p-4 rounded-lg shadow-lg"
             :class="notificationSuccess ? 'bg-green-500 text-white' : 'bg-red-500 text-white'">
            <svg v-if="notificationSuccess" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span class="font-semibold">{{ notificationMessage }}</span>
        </div>
    </transition>
    
    <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
      <div class="absolute w-96 h-96 bg-indigo-500 rounded-full blur-3xl opacity-30 top-1/4 left-1/4 animate-blob"></div>
      <div class="absolute w-80 h-80 bg-sky-500 rounded-full blur-3xl opacity-30 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>
    
    <div :class="theme === 'dark' ? 'bg-gray-800/50 ring-1 ring-white/10' : 'bg-white ring-1 ring-black/5'"
         class="relative z-10 w-full max-w-md mx-auto p-8 md:p-10 rounded-3xl shadow-2xl backdrop-blur-xl animate-fade-in-up">
      
      <div v-if="currentStage === 'login'">
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
          
          <div>
            <div class="relative">
              <input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="Password" maxlength="14"
                     class="itbms-password w-full p-4 rounded-lg pr-12 placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                     :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" />
              <button type="button" @click="togglePasswordVisibility" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
                <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
              </button>
            </div>
            <p v-if="PasswordError" class="itbms-message text-red-500 text-xs mt-1 px-2">{{ PasswordError }}</p>
          </div>

          <div class="flex justify-end mt-1 px-2">
            <router-link to="/forgot-password" class="text-sm font-semibold transition" :class="theme === 'dark' ? 'text-indigo-400 hover:text-indigo-300' : 'text-indigo-600 hover:text-indigo-700'">
                Forgot Password?
            </router-link>
          </div>
          
          <p v-if="loginError" class="itbms-message text-red-500 text-sm text-center pt-2">{{ loginError }}</p>
          
          <div class="pt-4">
            <button type="button" @click="handleSubmit"
              class="itbms-signin-button w-full px-10 py-4 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1"
              :class="{
                'opacity-50 cursor-not-allowed': !isFormValid,
                'opacity-50 pointer-events-none': isLoading
              }"
              :disabled="!isFormValid || isLoading">
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

      <div v-else-if="currentStage === 'otp'">
        <h2 class="text-3xl font-extrabold text-center mb-2" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
          Enter Verification Code
        </h2>
        <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'" class="text-center mb-6">
          We've sent a 6-digit code to <span class="font-bold text-indigo-500 dark:text-indigo-400">{{ email }}</span>
        </p>
        
        <form @submit.prevent="handleOtpSubmit" class="space-y-5">
          <div>
            <label for="otp" class="block text-left font-semibold mb-2">6-Digit Code</label>
            
            <div class="flex items-center justify-between gap-2">
              <input
                v-for="i in 6"
                :key="i"
                type="tel" 
                maxlength="1"
                v-model="otpDigits[i - 1]"
                @input="handleOtpInput($event, i - 1)"
                @keydown.backspace="handleOtpKeydown($event, i - 1)"
                @paste.prevent="handleOtpPaste($event)"
                :id="'otp-input-' + (i - 1)"
                class="otp-box w-full h-14 text-2xl text-center rounded-lg border-0 outline-none transition-all no-spin"
                :class="[
                  theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100',
                  otpError ? 'ring-2 ring-red-500' : 'focus:ring-2 focus:ring-indigo-500'
                ]"
              />
            </div>
            <p v-if="otpError" class="itbms-message text-red-500 text-xs mt-2 px-2 text-center">{{ otpError }}</p>
          </div>

          <div class="flex items-center justify-between px-1">
              <label class="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" v-model="rememberMe" class="form-checkbox h-4 w-4 rounded-sm" />
                  <span class="text-sm font-medium" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">Remember Me in 30 Days</span>
              </label>
          </div>
          
          <div class="pt-4 space-y-4">
            <button type="submit"
              class="itbms-verify-button w-full px-10 py-4 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1"
              :class="{'opacity-50 pointer-events-none': isLoading}"
              :disabled="isLoading || otp.length < 6">
              {{ isLoading ? 'Verifying...' : 'Verify & Sign In' }}
            </button>
            <button 
                type="button" 
                @click="handleResendOtp"
                :disabled="resendCooldown > 0 || isResending"
                class="itbms-resend-button w-full font-semibold transition-colors"
                :class="[
                    theme === 'dark' ? 'text-indigo-400 hover:text-indigo-300' : 'text-indigo-600 hover:text-indigo-700',
                    (resendCooldown > 0 || isResending) ? 'opacity-50 cursor-not-allowed' : ''
                ]">
                {{ isResending ? 'Sending...' : (resendCooldown > 0 ? `Resend in (${resendCooldown}s)` : 'Resend Code') }}
            </button>
          </div>
        </form>
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
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}
.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-150%);
  opacity: 0;
}

.no-spin::-webkit-outer-spin-button,
.no-spin::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.no-spin {
  -moz-appearance: textfield;
}

.form-checkbox {
    appearance: none;
    background-color: transparent;
    border-radius: 0.25rem;
    border-width: 2px;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
}
.dark .form-checkbox { border-color: #4b5563; }
.light .form-checkbox { border-color: #cbd5e1; }
.form-checkbox:checked {
    background-color: #6366f1;
    border-color: #6366f1;
    background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
}

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