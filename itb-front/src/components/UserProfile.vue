<script setup>
import { useRouter, useRoute } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import { getItemByIdWithAuth } from '@/libs/fetchUtilsOur';
import Cookies from 'js-cookie'

const router = useRouter()
const route = useRoute()
const theme = ref(localStorage.getItem('theme') || 'dark')
const userPicture = ref({ image: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' })
const userProfile = ref(null) 
const showUpdateSuccess = ref(true)

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

const iconComponent = computed(() => {
    return theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
})

const applyTheme = (newTheme) => {
    document.body.className = newTheme === 'dark' ? 'dark-theme' : ''
    localStorage.setItem('theme', newTheme)
    theme.value = newTheme
}

const cardClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-900 shadow-xl border border-gray-800'
        : 'bg-gray-100 shadow-xl border border-gray-200'
})

const isSeller = computed(() => userProfile.value?.userType === 'Seller')

const maskedMobile = computed(() => {
    const mobile = userProfile.value?.mobile;
    if (!mobile || mobile.length < 4) return mobile;
    return mobile.slice(0, -4).replace(/./g, 'x') + mobile.slice(-4);
})

const maskedNationalId = computed(() => {
    const nationalId = userProfile.value?.nationalId;
    if (!nationalId || nationalId.length < 4) return nationalId;
    const len = nationalId.length;
    const start = nationalId.slice(0, len - 4).replace(/./g, 'x');
    const middle = nationalId.slice(len - 4, len - 1);
    const end = nationalId.slice(len - 1);
    return `${start}${middle}${end}`;
})

const navigateToEdit = () => {
    router.push('/profile/edit')
}

const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(newTheme)
}

const decodeJwtToken = (token) => {
    try {
        const payload = token.split('.')[1];
        const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decodedPayload);
    } catch (error) {
        console.error('Failed to decode JWT token:', error);
        return null;
    }
};

const fetchUserProfile = async () => {
    const token = Cookies.get('access_token');

    if (!token) {
        console.error('Authentication error: No access token found in storage.');
        router.push('/signin'); 
        return;
    }

    const decodedToken = decodeJwtToken(token);

    if (!decodedToken || !decodedToken.id) {
        console.error('Invalid token payload: Could not find user ID.');
        router.push('/signin');
        return;
    }

    const userId = decodedToken.id;

    try {
const response = await getItemByIdWithAuth(
        `${import.meta.env.VITE_BACKEND}/v2/users`,
        userId,
        token
        );
        
        if (response.status === 401 || response.status === 403) {
            console.error('Authentication error: Unauthorized or Forbidden');
            router.push('/signin');
            return;
        }
        
        if (!response.ok) {
            throw new Error(`Failed to fetch user profile: ${response.statusText}`);
        }

        const data = await response.json();
        data.userType = decodedToken.role === 'SELLER' ? 'Seller' : 'Buyer';
        userProfile.value = data;
    } catch (error) {
        console.error('Error fetching user profile:', error);
    }
};

const closeSuccessPopup = async () => {
  showUpdateSuccess.value = false
  router.replace({ path: route.path, query: {} });
  window.location.reload();
  await fetchItems();
}

watch(
  () => route.query.updateSuccess,
  (updateSuccess) => {
    if (updateSuccess === 'true') {
      setTimeout(() => {
        showUpdateSuccess.value = true
      }, 200)
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)

onMounted(() => {
    fetchUserProfile();
    applyTheme(theme.value);
});

// แก้ไขในส่วนนี้
const maskedMobileNumber = computed(() => {
    const mobile = userProfile.value?.mobile;
    if (!mobile || mobile.length <= 4) return mobile;
    const unmaskedPart = mobile.slice(-4, -1);
    const maskedPart = 'x'.repeat(mobile.length - 4);
    return `${maskedPart}${unmaskedPart}x`;
})

// แก้ไขในส่วนนี้
const maskedBankNumber = computed(() => {
    const bankNumber = userProfile.value?.bankNumber;
    if (!bankNumber || bankNumber.length <= 4) return bankNumber;
    const unmaskedPart = bankNumber.slice(-4, -1);
    const maskedPart = 'x'.repeat(bankNumber.length - 4);
    return `${maskedPart}${unmaskedPart}x`;
})

</script>

<template>
    <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden p-6 md:p-20 flex items-center justify-center">
        <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
            <div class="absolute w-96 h-96 bg-gradient-to-r from-orange-500 to-pink-500 rounded-full blur-3xl opacity-50 top-1/4 left-1/4 animate-blob"></div>
            <div class="absolute w-80 h-80 bg-gradient-to-r from-teal-400 to-blue-500 rounded-full blur-3xl opacity-50 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
        </div>
        
        <div class="relative z-10 w-full max-w-2xl animate-fade-in-up">
            
            <div v-if="userProfile" :class="cardClass" class="p-8 md:p-12 rounded-[2rem] space-y-6">
                <div class="flex justify-center mb-6">
                    <img :src="userPicture.image" alt="User Profile Picture" 
                         class="w-32 h-32 rounded-full border-4 border-orange-500 object-cover shadow-lg transform hover:scale-110 transition-transform duration-300" />
                </div>
                
                <h2 class="text-4xl font-bold text-center mb-6">User Profile</h2>
                
                <div class="space-y-4">
                    <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                        <i class="fas fa-user-circle text-orange-500 text-2xl"></i>
                        <div>
                            <p class="text-sm opacity-75">Nickname</p>
                            <p class="itbms-nickname font-semibold text-lg">{{ userProfile.nickname }}</p>
                        </div>
                    </div>
                    
                    <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                        <i class="fas fa-id-card-alt text-red-500 text-2xl"></i>
                        <div>
                            <p class="text-sm opacity-75">Fullname</p>
                            <p class="itbms-fullname font-semibold text-lg">{{ userProfile.fullname }}</p>
                        </div>
                    </div>

                    <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                        <i class="fas fa-envelope text-teal-500 text-2xl"></i>
                        <div>
                            <p class="text-sm opacity-75">Email</p>
                            <p class="itbms-email font-semibold text-lg">{{ userProfile.email }}</p>
                        </div>
                    </div>

                    <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                            <i class="fas fa-credit-card text-green-500 text-2xl"></i>
                            <div>
                                <p class="text-sm opacity-75">Type</p>
                                <p class="itbms-type font-semibold text-lg">{{ userProfile.userType }}</p>
                            </div>
                        </div>

                    <template v-if="isSeller">
                        <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                            <i class="fas fa-mobile-alt text-purple-500 text-2xl"></i>
                            <div>
                                <p class="text-sm opacity-75">Mobile</p>
                                <p class="itbms-mobile font-semibold text-lg">{{ maskedMobileNumber }}</p>
                            </div>
                        </div>
                        <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                            <i class="fas fa-university text-blue-500 text-2xl"></i>
                            <div>
                                <p class="text-sm opacity-75">Bank Name</p>
                                <p class="itbms-bankName font-semibold text-lg text-orange-500">{{ userProfile.bankName }}</p>
                            </div>
                        </div>
                        <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                            <i class="fas fa-credit-card text-green-500 text-2xl"></i>
                            <div>
                                <p class="text-sm opacity-75">Bank Account</p>
                                <p class="itbms-bankAccount font-semibold text-lg">{{ maskedBankNumber }}</p>
                            </div>
                        </div>
                        <!-- <div class="p-4 rounded-xl flex items-center gap-4" :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-200'">
                            <i class="fas fa-address-card text-yellow-500 text-2xl"></i>
                            <div>
                                <p class="text-sm opacity-75">National ID</p>
                                <p class="font-semibold text-lg">{{ maskedNationalId }}</p>
                            </div>
                        </div> -->
                    </template>
                </div>

                <div class="itbms-profile-button pt-4">
                    <button @click="navigateToEdit"
                        class="w-full px-10 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105">
                        Edit Profile
                    </button>
                </div>
            </div>
            
            <div v-else :class="cardClass" class="p-8 md:p-12 rounded-[2rem] text-center">
                <p>Failed to load user profile. Please try again.</p>
            </div>
        </div>

        <transition name="bounce-popup">
      <div v-if="showUpdateSuccess"
        class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="p-6 rounded-3xl shadow-lg text-center"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-gray-950'">
          <DotLottieVue src="/sy4/animation/Success.lottie" autoplay class="w-24 h-24 mx-auto mb-4" />
          <h2 class="text-xl font-semibold mb-4">Success!</h2>
          <p class="itbms-message mb-4">Profile data is updated successfully!</p>
          <button @click="closeSuccessPopup"
            class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
        </div>
      </div>
    </transition>
        <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
    </button>
    </div>
</template>

<style scoped>
@keyframes fade-in-up {
    from { opacity: 0; transform: translateY(40px); }
    to { opacity: 1; transform: translateY(0); }
}
@keyframes blob {
    0% { transform: scale(1) translate(0px, 0px); }
    33% { transform: scale(1.1) translate(30px, -50px); }
    66% { transform: scale(0.9) translate(-20px, 20px); }
    100% { transform: scale(1) translate(0px, 0px); }
}
.animate-fade-in-up { animation: fade-in-up 1s ease-out forwards; }
.animate-blob { animation: blob 7s infinite ease-in-out; }
.animation-delay-2000 { animation-delay: 2s; }
</style>
