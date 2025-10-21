<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, onMounted, watch } from 'vue';
import { addItem } from '@/libs/fetchUtilsOur';
// 1. Import 'theme' จาก Store ส่วนกลาง
import { theme } from '@/stores/themeStore.js'; // <-- ตรวจสอบว่า Path ไปยังไฟล์ store ถูกต้อง

const router = useRouter();
const accountType = ref('Buyer'); // Default to Buyer
const showPassword = ref(false);
const showConfirmPassword = ref(false);

// --- โค้ดส่วนที่เหลือทั้งหมดเหมือนเดิมทุกประการ ไม่มีการแก้ไข Logic ---

const uploadedPhotos = ref([]);

// ฟังก์ชันสำหรับจัดการการเลือกไฟล์ภาพ
const handlePhotoUpload = (event) => {
    const files = event.target.files;
    if (files) {
        for (const file of files) {
            const imageUrl = URL.createObjectURL(file);
            uploadedPhotos.value.push({ file, url: imageUrl });
        }
    }
};

const removePhoto = (index) => {
    // ใช้ splice เพื่อลบภาพออกจาก array ตาม index
    uploadedPhotos.value.splice(index, 1);
};

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

const isPasswordValid = ref(false)
const isnicknameValid = ref(false)
const isemailValid = ref(false)
const isfullnameValid = ref(false)

const PasswordError =ref('')
const nicknameError = ref('')
const emailError = ref('')
const fullnameError = ref('')
const SellerError =ref('')
const SubmitError = ref ('')
const emailApiError = ref('')
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).+$/

const showConfirmationSignUp = ref(false)
const isLoading = ref(false)

const nationalIdPhotoFrontUrl = computed(() => nationalIdPhotoFront.value ? URL.createObjectURL(nationalIdPhotoFront.value) : null);
const nationalIdPhotoBackUrl = computed(() => nationalIdPhotoBack.value ? URL.createObjectURL(nationalIdPhotoBack.value) : null);


const handleFileChange = (event, side) => {
    const file = event.target.files[0];
    if (side === 'front') {
        nationalIdPhotoFront.value = file;
    } else if (side === 'back') {
        nationalIdPhotoBack.value = file;
    }
};

const removeNationalIdPhotoFront = () => {
    nationalIdPhotoFront.value = null;
};

const removeNationalIdPhotoBack = () => {
    nationalIdPhotoBack.value = null;
};

// --- Watch validate แบบ realtime ---
watch(nickname, (val) => {
  if (!val.trim()) {
    nicknameError.value = 'Nickname is required.'
    isnicknameValid.value = false
  } else {
    nicknameError.value = ''
    isnicknameValid.value = true
  }
})

watch(email, (val) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!val.trim()) {
    emailError.value = 'Email is required.'
    isemailValid.value = false
  } else if (!emailRegex.test(val)) {
    emailError.value = 'Invalid email format.'
    isemailValid.value = false
  } else {
    emailError.value = ''
    isemailValid.value = true
  }
})

watch(password, (val) => {
  if (!val) {
    PasswordError.value = 'Password is required.'
    isPasswordValid.value = false
  } else if (val.length < 8) {
    PasswordError.value = 'Password must be at least 8 characters.'
    isPasswordValid.value = false
  } else if (val.length >14) {
    PasswordError.value = 'Password must be no more than 14 characters long.'
    isPasswordValid.value = false
  }else if (!passwordRegex.test(val)) {
    PasswordError.value = 'Password must include upper, lower, number, and special character.'
    isPasswordValid.value = false
  } else {
    PasswordError.value = ''
    isPasswordValid.value = true
  }
})

watch(fullname, (val) => {
  if (!val.trim()) {
    fullnameError.value = 'Fullname is required.'
    isfullnameValid.value = false
  } else if (val.length < 4 || val.length > 40) {
    fullnameError.value = 'Fullname must be 4–40 characters long.'
    isfullnameValid.value = false
  } else {
    fullnameError.value = ''
    isfullnameValid.value = true
  }
})

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

const isFormValid = computed(() => {
    const basicFieldsValid = nickname.value.trim() !== '' &&
                                 email.value.trim() !== '' &&
                                 password.value.trim() !== '' &&
                                 confirmPassword.value.trim() !== '' &&
                                 fullname.value.trim() !== '' &&
                                 isPasswordValid.value &&
                                 isnicknameValid.value &&
                                 isemailValid.value &&
                                 isfullnameValid.value &&
                                 password.value === confirmPassword.value;

    if (accountType.value === 'Seller') {
        return basicFieldsValid &&
               mobileNumber.value.trim() !== '' &&
               bankAccountNumber.value.trim() !== '' &&
               bankName.value.trim() !== '' &&
               nationalId.value.trim() !== '' &&
               nationalIdPhotoFront.value !== null &&
               nationalIdPhotoBack.value !== null;
    }

    return basicFieldsValid;
});

const handleSubmit = async () => {
    SubmitError.value = '';
    SellerError.value = '';
    PasswordError.value = '';

    if (password.value !== confirmPassword.value) {
        PasswordError.value = 'Passwords do not match!';
        return;
    }

    if (isFormValid.value) {
        // ✅ เมื่อฟอร์มถูกต้อง ให้แสดง Pop-up ยืนยัน
        showConfirmationSignUp.value = true;
    } else {
        SubmitError.value = 'Please fill out the information completely.';
    }
};

const confirmSignUp = async () => {
    // ✅ ซ่อน Pop-up ยืนยัน
    showConfirmationSignUp.value = false;

    // ✅ แสดง Pop-up Loading
    isLoading.value = true;

    const formData = new FormData();
    formData.append('nickname', nickname.value.trim());
    formData.append('email', email.value.trim());
    formData.append('fullname', fullname.value.trim());
    formData.append('password', password.value.trim());
    formData.append('userType', accountType.value.trim());

    if (accountType.value === 'Seller') {
        formData.append('mobile', mobileNumber.value.trim());
        formData.append('bankNumber', bankAccountNumber.value.trim());
        formData.append('bankName', bankName.value.trim());
        formData.append('nationalId', nationalId.value.trim());
        formData.append('idCardImageFront', nationalIdPhotoFront.value);
        formData.append('idCardImageBack', nationalIdPhotoBack.value);
    }

    try {
        const result = await addItem(`${import.meta.env.VITE_BACKEND}/v2/auth/register`, formData, true);

        if (result.status === 409) {
            let msg = result.data.message || 'Email already exists';
            const match = msg.match(/"(.*)"/);
            if (match && match[1]) {
                msg = match[1];
            }
            emailApiError.value = msg;
        }

        if (result.status !== 201 || !result.data?.id) {
            throw new Error('Registration failed or invalid data returned');
        }

        setTimeout(() => {
            isLoading.value = false;
            router.push({ path: '/sale-items', query: { regisSuccess: 'true' } });
        }, 1000);
    } catch (error) {
        console.error('Error:', error);
        isLoading.value = false;
    }
};

const cancelSignUp = () => {
    showConfirmationSignUp.value = false;
};

const formatMobileNumber = () => {
  mobileNumber.value = mobileNumber.value.replace(/[^0-9]/g, '');
};

// 2. Computed property ที่ใช้ theme จาก store ในการเปลี่ยนสี
const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans flex items-center justify-center p-6 transition-colors duration-300">
    <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
      <div class="absolute w-96 h-96 bg-indigo-500 rounded-full blur-3xl opacity-30 top-1/4 left-1/4 animate-blob"></div>
      <div class="absolute w-80 h-80 bg-sky-500 rounded-full blur-3xl opacity-30 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>

    <div :class="theme === 'dark' ? 'bg-gray-800/50 ring-1 ring-white/10' : 'bg-white ring-1 ring-black/5'" 
         class="relative z-10 w-full max-w-2xl mx-auto p-8 md:p-10 rounded-3xl shadow-2xl backdrop-blur-xl animate-fade-in-up">
      <h2 class="text-4xl font-extrabold text-center mb-2" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
        Create Account
      </h2>
      <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'" class="text-center mb-8">Join the ITB MSHOP community.</p>

      <div class="flex justify-center mb-8 p-1 rounded-full" :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'">
        <button @click="accountType = 'Buyer'"
                :class="accountType === 'Buyer' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500'"
                class="itbms-account-type flex-1 px-6 py-2 rounded-full font-semibold transition-all duration-300">
          Buyer
        </button>
        <button @click="accountType = 'Seller'"
                :class="accountType === 'Seller' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500'"
                class="itbms-account-type flex-1 px-6 py-2 rounded-full font-semibold transition-all duration-300">
          Seller
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-5">
        <div class="grid sm:grid-cols-2 gap-5">
            <div>
                <input type="text" v-model="nickname" class="itbms-nickname w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Nickname"  />
                <p v-if="nicknameError" class="itbms-message text-red-500 text-xs mt-1 px-2">{{ nicknameError }}</p>
            </div>
            <div>
                <input type="text" v-model="fullname" class="itbms-fullname w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Full Name"  />
                <p v-if="fullnameError" class="itbms-message text-red-500 text-xs mt-1 px-2">{{ fullnameError }}</p>
            </div>
        </div>
        <div>
            <input type="email" v-model="email" class="itbms-email w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Email Address"  />
            <p v-if="emailError" class="itbms-message text-red-500 text-xs mt-1 px-2">{{ emailError }}</p>
            <p v-if="emailApiError" class="itbms-message text-red-500 text-xs mt-1 px-2">{{ emailApiError }}</p>
        </div>
        <div class="grid sm:grid-cols-2 gap-5">
            <div class="relative">
                <input :type="showPassword ? 'text' : 'password'" v-model="password" class="itbms-password w-full p-4 rounded-lg pr-12 placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Password" />
                <button type="button" @click="togglePasswordVisibility" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
                     <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                     <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
                </button>
            </div>
            <div class="relative">
                <input :type="showConfirmPassword ? 'text' : 'password'" v-model="confirmPassword" class="w-full p-4 rounded-lg pr-12 placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Confirm Password" />
                <button type="button" @click="toggleConfirmPasswordVisibility" class="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-400 hover:text-indigo-500">
                     <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                     <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a9.956 9.956 0 012.223-3.592M6.223 6.223A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-2.223 3.592M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3l18 18"/></svg>
                </button>
            </div>
        </div>
        <p v-if="PasswordError" class="itbms-message text-red-500 text-sm -mt-2 text-center">{{ PasswordError }}</p>

        <div v-if="accountType === 'Seller'" class="space-y-5 pt-5 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-black/10'">
            <div class="grid sm:grid-cols-2 gap-5">
                 <input type="text" v-model="mobileNumber" class="itbms-mobile w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Mobile Number" @blur="formatMobileNumber" />
                 <input type="text" v-model="nationalId" class="itbms-card-no w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="National Card Number"  />
            </div>
            <div class="grid sm:grid-cols-2 gap-5">
                 <input type="text" v-model="bankAccountNumber" class="itbms-bank-account-no w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Bank Account Number"  />
                 <input type="text" v-model="bankName" class="itbms-bank-name w-full p-4 rounded-lg placeholder-slate-400 focus:ring-2 focus:ring-indigo-500 transition-all border-0 outline-none"
                    :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'" placeholder="Bank Name"  />
            </div>
            <div>
              <label class="block mb-2 text-sm font-medium" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">National ID Photo (Front & Back)</label>
              <div class="flex space-x-4">
                <div class="itbms-card-photo-front relative group aspect-video rounded-xl overflow-hidden w-1/2">
                   <img v-if="nationalIdPhotoFront" :src="nationalIdPhotoFrontUrl" alt="Front ID Photo" class="w-full h-full object-cover">
                   <label v-else class="block w-full h-full p-4 rounded-xl text-center cursor-pointer transition-colors duration-300 flex flex-col items-center justify-center"
                           :class="theme === 'dark' ? 'bg-gray-700/50 border-2 border-dashed border-gray-600 hover:bg-gray-700' : 'bg-slate-100 border-2 border-dashed border-gray-300 hover:bg-slate-200'">
                     <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                     <span class="text-xs">Front Side</span>
                     <input type="file" @change="handleFileChange($event, 'front')" accept="image/*" class="hidden" />
                   </label>
                   <button v-if="nationalIdPhotoFront" @click="removeNationalIdPhotoFront" type="button" class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 transition-opacity duration-200 opacity-0 group-hover:opacity-100">
                     <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" /></svg>
                   </button>
                </div>
                <div class="itbms-card-photo-back relative group aspect-video rounded-xl overflow-hidden w-1/2">
                  <img v-if="nationalIdPhotoBack" :src="nationalIdPhotoBackUrl" alt="Back ID Photo" class="w-full h-full object-cover">
                  <label v-else class="block w-full h-full p-4 rounded-xl text-center cursor-pointer transition-colors duration-300 flex flex-col items-center justify-center"
                         :class="theme === 'dark' ? 'bg-gray-700/50 border-2 border-dashed border-gray-600 hover:bg-gray-700' : 'bg-slate-100 border-2 border-dashed border-gray-300 hover:bg-slate-200'">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                    <span class="text-xs">Back Side</span>
                    <input type="file" @change="handleFileChange($event, 'back')" accept="image/*" class="hidden" />
                  </label>
                  <button v-if="nationalIdPhotoBack" @click="removeNationalIdPhotoBack" type="button" class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 transition-opacity duration-200 opacity-0 group-hover:opacity-100">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" /></svg>
                  </button>
                </div>
              </div>
            </div>
        </div>
        
        <p v-if="SubmitError" class="itbms-message text-red-500 text-sm text-center pt-2">{{ SubmitError }}</p>
        
        <div class="pt-4 space-y-4">
            <button type="button" @click="handleSubmit" class="itbms-submit-button w-full px-10 py-4 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1"
                :class="{'opacity-50 cursor-not-allowed': !isFormValid}" :disabled="!isFormValid">
                Create Account
            </button>
            <button type="button" @click="router.push('/')" class="itbms-cancel-button w-full py-4 font-semibold rounded-full transition-all duration-300"
                    :class="theme === 'dark' ? 'text-slate-300 hover:bg-gray-700/50' : 'text-slate-500 hover:bg-slate-100'">
                Cancel
            </button>
        </div>
      </form>

      <div class="text-center mt-6 text-sm">
        <p :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Already have an account?
          <router-link to="/signin" class="font-semibold transition" :class="theme === 'dark' ? 'text-indigo-400 hover:text-indigo-300' : 'text-indigo-600 hover:text-indigo-700'">
            Login Now
          </router-link>
        </p>
      </div>
    </div>
  </div>

  <transition name="bounce-popup">
    <div v-if="showConfirmationSignUp"
        class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
            :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
            <h2 class="text-2xl font-bold mb-4">Confirm Registration</h2>
            <p class="itbms-message mb-6 text-lg">Are you sure you want to create this account?</p>
            <div class="flex justify-center gap-4">
                <button @click="confirmSignUp"
                    class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
                <button @click="cancelSignUp"
                    class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">No</button>
            </div>
        </div>
    </div>
</transition>

<transition name="fade-background">
    <div v-if="isLoading"
        class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
        <div class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
            :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
            <svg class="animate-spin h-8 w-8 text-indigo-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
            </svg>
            <p class="itbms-message text-lg font-medium">Registering account...</p>
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
.fade-background-enter-active,
.fade-background-leave-active {
    transition: opacity 0.3s ease;
}
.fade-background-enter-from,
.fade-background-leave-to {
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

.itbms-bg {
    background-color: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(4px);
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