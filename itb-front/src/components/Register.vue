<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, onMounted, watch } from 'vue';

const router = useRouter();
const theme = ref(localStorage.getItem('theme') || 'dark');
const accountType = ref('Buyer'); // Default to Buyer
const showPassword = ref(false);
const showConfirmPassword = ref(false);

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

// form data
const nickname = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const fullname = ref('');
const mobileNumber = ref('');
const bankAccountNumber = ref('');
const bankName = ref('');
const nationalId = ref('');
const nationalIdPhotoFront = ref(null);
const nationalIdPhotoBack = ref(null);

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

const handleFileChange = (event, type) => {
  if (type === 'front') {
    nationalIdPhotoFront.value = event.target.files[0];
  } else if (type === 'back') {
    nationalIdPhotoBack.value = event.target.files[0];
  }
};

// const handleSubmit = async () => {
//   if (password.value !== confirmPassword.value) {
//     alert("Passwords do not match!");
//     return;
//   }

//   const formData = new FormData();
//   formData.append('nickname', nickname.value);
//   formData.append('email', email.value);
//   formData.append('password', password.value);
//   formData.append('fullname', fullname.value);
//   formData.append('accountType', accountType.value);
  
//   if (accountType.value === 'Seller') {
//     if (!mobileNumber.value || !bankAccountNumber.value || !bankName.value || !nationalId.value || !nationalIdPhotoFront.value || !nationalIdPhotoBack.value) {
//       alert("Please fill in all seller registration fields and upload both sides of your National ID photo.");
//       return;
//     }
//     formData.append('mobileNumber', mobileNumber.value);
//     formData.append('bankAccountNumber', bankAccountNumber.value);
//     formData.append('bankName', bankName.value);
//     formData.append('nationalId', nationalId.value);
//     formData.append('nationalIdPhotoFront', nationalIdPhotoFront.value);
//     formData.append('nationalIdPhotoBack', nationalIdPhotoBack.value);
//   }

//   console.log('Registering with:', Object.fromEntries(formData.entries()));

//   try {
//     const response = await fetch('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/register', {
//       method: 'POST',
//       body: formData,
//     });

//     if (response.ok) {
//       alert('Registration successful! Redirecting to login page.');
//       router.push('/login'); // Redirect to login page after successful registration
//     } else {
//       const errorData = await response.json();
//       alert(`Registration failed: ${errorData.message}`);
//     }
//   } catch (error) {
//     console.error('Error during registration:', error);
//     alert('An error occurred. Please try again later.');
//   }
// };

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
        <span class="text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-pink-500">Register</span>
      </h2>
      <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" class="text-center mb-8">Create a new ITB MSHOP account.</p>

      <div class="flex justify-center mb-6 space-x-4">
        <button @click="accountType = 'Buyer'"
                :class="{'bg-orange-500 text-white': accountType === 'Buyer', 'bg-gray-200 text-gray-800': accountType !== 'Buyer'}"
                class="itbms-account-type px-6 py-2 rounded-full font-semibold transition-colors duration-300">
          Buyer
        </button>
        <button @click="accountType = 'Seller'"
                :class="{'bg-orange-500 text-white': accountType === 'Seller', 'bg-gray-200 text-gray-800': accountType !== 'Seller'}"
                class="itbms-account-type px-6 py-2 rounded-full font-semibold transition-colors duration-300">
          Seller
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <input type="text" v-model="nickname" class="itbms-nickname w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Nickname" required />
        
        <input type="email" v-model="email" class="itbms-email w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Email" required />

        <div class="relative">
          <input :type="showPassword ? 'text' : 'password'" v-model="password" class="itbms-password w-full p-4 rounded-xl pr-12 placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Password" required />
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
        
        <div class="relative">
          <input :type="showConfirmPassword ? 'text' : 'password'" v-model="confirmPassword" class="w-full p-4 rounded-xl pr-12 placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Confirm Password" required />
          <button type="button" @click="toggleConfirmPasswordVisibility" class="absolute inset-y-0 right-0 pr-3 flex items-center text-sm leading-5">
            <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
              <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.958 10.958 0 0020 10c-1.274-4.057-5.064-7-9.542-7-1.254 0-2.457.246-3.564.733l-2.022-2.022zM8.68 5.61A6.98 6.98 0 0010 4a9 9 0 018.354 5.646l-2.493-2.493zM3.646 10c.937 2.062 2.825 3.513 4.965 4.067l-2.5-2.5a4 4 0 01-2.465-1.567zm12.923-3.61l-3.076 3.076a4 4 0 01-5.584 5.584L3.707 17.293a1 1 0 101.414 1.414l12-12a1 1 0 00-1.414-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
        
        <input type="text" v-model="fullname" class="itbms-fullname w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Fullname" required />

        <div v-if="accountType === 'Seller'" class="space-y-4 pt-4 border-t" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'">
          <h4 class="font-semibold text-xl">Seller Information</h4>
          <input type="text" v-model="mobileNumber" class="itbms-mobile w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Mobile Number" required />
          <input type="text" v-model="bankAccountNumber" class="itbms-bank-account-no w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Bank Account Number" required />
          <input type="text" v-model="bankName" class="itbms-bank-name w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Bank Name" required />
          <input type="text" v-model="nationalId" class="itbms-card-no w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="National Card Number" required />
          <div>
            <label for="nationalIdPhoto" class="block mb-2 font-semibold">National ID Photo (front & back)</label>
            <div class="flex space-x-4">
              <label class="itbms-card-photo-front block w-1/2 p-4 rounded-xl text-center cursor-pointer transition-colors duration-300"
                     :class="theme === 'dark' ? 'bg-gray-800 border-2 border-dashed border-gray-700 hover:bg-gray-700' : 'bg-gray-100 border-2 border-dashed border-gray-300 hover:bg-gray-200'">
                {{ nationalIdPhotoFront ? nationalIdPhotoFront.name : 'Front side' }}
                <input type="file" @change="handleFileChange($event, 'front')" accept="image/*" required class="hidden" />
              </label>
              <label class="itbms-card-photo-back block w-1/2 p-4 rounded-xl text-center cursor-pointer transition-colors duration-300"
                     :class="theme === 'dark' ? 'bg-gray-800 border-2 border-dashed border-gray-700 hover:bg-gray-700' : 'bg-gray-100 border-2 border-dashed border-gray-300 hover:bg-gray-200'">
                {{ nationalIdPhotoBack ? nationalIdPhotoBack.name : 'Back side' }}
                <input type="file" @change="handleFileChange($event, 'back')" accept="image/*" required class="hidden" />
              </label>
            </div>
          </div>
        </div>

        <button type="submit" class="itbms-submit-button w-full px-10 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105">
          Submit
        </button>
        <button type="button" @click="router.push('/login')" class="itbms-cancel-button w-full px-10 py-3 font-semibold rounded-full transition-all duration-300 transform hover:bg-white/10 hover:border-white/50" :class="theme === 'dark' ? 'border-2 border-white/20 text-white hover:bg-white/10 hover:border-white/50' : 'border-2 border-gray-400 text-gray-800 hover:bg-gray-200/50 hover:border-gray-500'">
          Cancel
        </button>
      </form>

      <div class="text-center mt-6">
        <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
          Already have an account?
          <router-link to="/login" class="font-semibold text-orange-500 hover:underline ml-1">
            Login
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