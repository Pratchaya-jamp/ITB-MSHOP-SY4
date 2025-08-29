<script setup>
import { useRouter } from 'vue-router';
import { ref, computed, onMounted, watch } from 'vue';
import { addItem } from '@/libs/fetchUtilsOur'

const router = useRouter();
const theme = ref(localStorage.getItem('theme') || 'dark');
const accountType = ref('Buyer'); // Default to Buyer
const showPassword = ref(false);
const showConfirmPassword = ref(false);

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
  } else if (!passwordRegex.test(val)) {
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


const validateForm = () => {
    return (
      isPasswordValid.value&
      isnicknameValid.value&
      isemailValid.value&
      isfullnameValid.value
    )
}

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
        const result = await addItem('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/users/register', formData, true);

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
        <p v-if="nicknameError" class="itbms-message text-red-500 text-sm mt-1">{{ nicknameError }}</p>
        <input type="text" v-model="nickname" class="itbms-nickname w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Nickname"  />
        <p v-if="emailError" class="itbms-message text-red-500 text-sm mt-1">{{ emailError }}</p>
        <p v-if="emailApiError" class="itbms-message text-red-500 text-sm mt-1">{{ emailApiError }}</p>
        <input type="email" v-model="email" class="itbms-email w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Email"  />

               <p v-if="PasswordError" class="itbms-message text-red-500 text-sm mt-1">{{ PasswordError }}</p>
        <div class="relative">
          <input :type="showPassword ? 'text' : 'password'" v-model="password" class="itbms-password w-full p-4 rounded-xl pr-12 placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Password"  />
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
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Confirm Password"  />
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
        <p v-if="fullnameError" class="itbms-message text-red-500 text-sm mt-1">{{ fullnameError }}</p>
        <input type="text" v-model="fullname" class="itbms-fullname w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
               :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Fullname"  />

        <div v-if="accountType === 'Seller'" class="space-y-4 pt-4 border-t" :class="theme === 'dark' ? 'border-gray-700' : 'border-gray-300'">
          <h4 class="font-semibold text-xl">Seller Information</h4>
          <p v-if="SellerError" class="itbms-message text-red-500 text-sm mt-1">{{ SellerError }}</p>
          <input type="text" v-model="mobileNumber" class="itbms-mobile w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Mobile Number"  />
          <input type="text" v-model="bankAccountNumber" class="itbms-bank-account-no w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Bank Account Number"  />
          <input type="text" v-model="bankName" class="itbms-bank-name w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="Bank Name"  />
          <input type="text" v-model="nationalId" class="itbms-card-no w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 transition-all"
                 :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-gray-950'" placeholder="National Card Number"  />
          <div>
      <label class="block mb-2 font-semibold">National ID Photo (front & back)</label>
      <div class="flex space-x-4">
        <div class="itbms-card-photo-front relative group aspect-video rounded-xl overflow-hidden w-1/2">
          <img v-if="nationalIdPhotoFront" :src="nationalIdPhotoFrontUrl" alt="Front ID Photo" class="w-full h-full object-cover">
          <label v-else class="block w-full h-full p-4 rounded-xl text-center cursor-pointer transition-colors duration-300 flex flex-col items-center justify-center"
                 :class="theme === 'dark' ? 'bg-gray-800 border-2 border-dashed border-gray-700 hover:bg-gray-700' : 'bg-gray-100 border-2 border-dashed border-gray-300 hover:bg-gray-200'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span>Front side</span>
            <input type="file" @change="handleFileChange($event, 'front')" accept="image/*" class="hidden" />
          </label>
          <button v-if="nationalIdPhotoFront" @click="removeNationalIdPhotoFront" class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 transition-opacity duration-200 opacity-0 group-hover:opacity-100">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>

        <div class="itbms-card-photo-back relative group aspect-video rounded-xl overflow-hidden w-1/2">
          <img v-if="nationalIdPhotoBack" :src="nationalIdPhotoBackUrl" alt="Back ID Photo" class="w-full h-full object-cover">
          <label v-else class="block w-full h-full p-4 rounded-xl text-center cursor-pointer transition-colors duration-300 flex flex-col items-center justify-center"
                 :class="theme === 'dark' ? 'bg-gray-800 border-2 border-dashed border-gray-700 hover:bg-gray-700' : 'bg-gray-100 border-2 border-dashed border-gray-300 hover:bg-gray-200'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span>Back side</span>
            <input type="file" @change="handleFileChange($event, 'back')" accept="image/*" class="hidden" />
          </label>
          <button v-if="nationalIdPhotoBack" @click="removeNationalIdPhotoBack" class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 transition-opacity duration-200 opacity-0 group-hover:opacity-100">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
        <p v-if="SubmitError" class="itbms-message text-red-500 text-sm mt-1">{{ SubmitError }}</p>
        <button type="button" @click="handleSubmit"
          class="itbms-submit-button w-full px-10 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
          :class="{'opacity-50 cursor-not-allowed': !isFormValid}"
          :disabled="!isFormValid">
            Submit
        </button>
        <button type="button" @click="router.push('/')" class="itbms-cancel-button w-full px-10 py-3 font-semibold rounded-full transition-all duration-300 transform hover:bg-white/10 hover:border-white/50" :class="theme === 'dark' ? 'border-2 border-white/20 text-white hover:bg-white/10 hover:border-white/50' : 'border-2 border-gray-400 text-gray-800 hover:bg-gray-200/50 hover:border-gray-500'">
          Cancel
        </button>
      </form>

      <div class="text-center mt-6">
        <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
          Already have an account?
          <router-link to="/signin" class="font-semibold text-orange-500 hover:underline ml-1">
            Login
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
            <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
            </svg>
            <p class="itbms-message text-lg font-medium">Registering account...</p>
        </div>
    </div>
</transition>
  <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
    </button>
</template>

<style scoped>
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

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.animate-spin {
    animation: spin 1s linear infinite;
}

span {
    white-space: normal;
    word-break: break-word;
}

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
    transition: opacity 0.3s ease;
}

.fade-background-enter-from,
.fade-background-leave-to {
    opacity: 0;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.animate-spin {
    animation: spin 1s linear infinite;
}
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
