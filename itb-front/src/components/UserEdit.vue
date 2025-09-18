<script setup>
import { useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import Cookies from 'js-cookie'

const router = useRouter()
const theme = ref(localStorage.getItem('theme') || 'dark')
const userPicture = ref({ image: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' })
const userProfile = ref(null)
const originalProfile = ref(null)
const loading = ref(true)

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

const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(newTheme)
}

const cardClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-900 shadow-xl border border-gray-800'
        : 'bg-gray-100 shadow-xl border border-gray-200'
})

const isSeller = computed(() => userProfile.value?.userType === 'Seller')
const isBuyer = computed(() => userProfile.value?.userType === 'Buyer')

const isFormModified = computed(() => {
    if (!userProfile.value || !originalProfile.value) return false;
    
    // Check for changes in common editable fields
    const commonFieldsChanged = 
        userProfile.value.nickname !== originalProfile.value.nickname ||
        userProfile.value.fullname !== originalProfile.value.fullname ||
        userProfile.value.email !== originalProfile.value.email;

    // Check for changes in fields editable only by Seller
    const sellerFieldsChanged = isSeller.value && userProfile.value.password !== originalProfile.value.password;

    return commonFieldsChanged || sellerFieldsChanged;
})

const isSaveEnabled = computed(() => {
    return isFormModified.value;
})

const navigateToProfile = () => {
    router.push('/profile')
}

// **Function to handle saving changes to the backend (PUT/PATCH request)**
const saveProfile = async () => {
    if (!isSaveEnabled.value) return;

    // TODO: Implement the PUT/PATCH request to your backend here
    // const token = Cookies.get('access_token');
    // const userId = ...;
    // const API_URL = `http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/users/${userId}`;
    
    // try {
    //     const response = await fetch(API_URL, {
    //         method: 'PUT', // or 'PATCH'
    //         headers: {
    //             'Content-Type': 'application/json',
    //             'Authorization': `Bearer ${token}`
    //         },
    //         body: JSON.stringify({
    //             nickname: userProfile.value.nickname,
    //             fullname: userProfile.value.fullname,
    //             email: userProfile.value.email,
    //             password: isSeller.value ? userProfile.value.password : undefined
    //         })
    //     });

    //     if (!response.ok) {
    //         throw new Error(`Failed to update user profile: ${response.statusText}`);
    //     }

    //     console.log('Profile updated successfully!');
    //     navigateToProfile();
    // } catch (error) {
    //     console.error('Error saving profile:', error);
    // }

    // Placeholder until the backend endpoint is available
    console.log('Saving profile...');
    console.log('User Data to be saved:', userProfile.value);
    navigateToProfile();
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

// **Function to fetch user data for pre-filling the form**
const fetchUserProfileForEdit = async () => {
    loading.value = true;
    const token = Cookies.get('access_token');

    if (!token) {
        console.error('Authentication error: No access token found in cookies.');
        router.push('/login'); 
        loading.value = false;
        return;
    }

    const decodedToken = decodeJwtToken(token);

    if (!decodedToken || !decodedToken.id) {
        console.error('Invalid token payload: Could not find user ID.');
        router.push('/login');
        loading.value = false;
        return;
    }

    const userId = decodedToken.id;
    const API_URL = `http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/users/${userId}`;

    try {
        const response = await fetch(API_URL, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.status === 401 || response.status === 403) {
            console.error('Authentication error: Unauthorized or Forbidden');
            router.push('/login');
            return;
        }
        
        if (!response.ok) {
            throw new Error(`Failed to fetch user profile: ${response.statusText}`);
        }

        const data = await response.json();
        data.userType = decodedToken.role === 'SELLER' ? 'Seller' : 'Buyer';
        userProfile.value = data;
        originalProfile.value = { ...data }; // Store original data for comparison
    } catch (error) {
        console.error('Error fetching user profile:', error);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchUserProfileForEdit();
    applyTheme(theme.value);
});

const maskedMobileNumber = computed(() => {
    if (!userProfile.value?.mobile || userProfile.value.mobile.length < 4) return userProfile.value?.mobile;
    const len = userProfile.value.mobile.length;
    return userProfile.value.mobile.slice(0, len - 4).replace(/./g, 'x') + userProfile.value.mobile.slice(len - 4);
})

const maskedNationalId = computed(() => {
    if (!userProfile.value?.nationalId || userProfile.value.nationalId.length < 4) return userProfile.value?.nationalId;
    const len = userProfile.value.nationalId.length;
    const start = userProfile.value.nationalId.slice(0, len - 4).replace(/./g, 'x');
    const middle = userProfile.value.nationalId.slice(len - 4, len - 1);
    const end = userProfile.value.nationalId.slice(len - 1);
    return `${start}${middle}${end}`;
})
</script>

<template>
    <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden p-6 md:p-20 flex items-center justify-center">
        <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
            <div class="absolute w-96 h-96 bg-gradient-to-r from-orange-500 to-pink-500 rounded-full blur-3xl opacity-50 top-1/4 left-1/4 animate-blob"></div>
            <div class="absolute w-80 h-80 bg-gradient-to-r from-teal-400 to-blue-500 rounded-full blur-3xl opacity-50 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
        </div>
        
        <div class="relative z-10 w-full max-w-2xl animate-fade-in-up">
            <div v-if="loading" :class="cardClass" class="p-8 md:p-12 rounded-[2rem] text-center">
                <p>Loading user profile...</p>
            </div>
            <div v-else-if="userProfile" :class="cardClass" class="p-8 md:p-12 rounded-[2rem] space-y-6">
                 <div class="flex justify-center mb-6">
                    <img :src="userPicture.image" alt="User Profile Picture" 
                         class="w-32 h-32 rounded-full border-4 border-orange-500 object-cover shadow-lg transform hover:scale-110 transition-transform duration-300" />
                </div>
                
                <h2 class="text-4xl font-bold text-center mb-6">Edit Profile</h2>

                <div class="space-y-4">
                    <div class="flex flex-col gap-2">
                        <label for="nickname" class="text-sm opacity-75">Nickname</label>
                        <input type="text" id="nickname" v-model="userProfile.nickname"
                            class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                            :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" />
                    </div>

                    <div class="flex flex-col gap-2">
                        <label for="fullname" class="text-sm opacity-75">Fullname</label>
                        <input type="text" id="fullname" v-model="userProfile.fullname"
                            class="w-full p-4 rounded-xl placeholder-gray-500 transition-all focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                            :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" />
                    </div>

                    <div class="flex flex-col gap-2">
                        <label for="email" class="text-sm opacity-75">Email</label>
                        <input type="email" id="email" v-model="userProfile.email"
                            class="w-full p-4 rounded-xl placeholder-gray-500 transition-all focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                            :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" />
                    </div>

                    <template v-if="isSeller">
                        <div class="flex flex-col gap-2">
                            <label class="text-sm opacity-75">Mobile Number</label>
                            <input type="text" :value="maskedMobileNumber" disabled
                                class="w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed"
                                :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                        </div>
                        <div class="flex flex-col gap-2">
                            <label class="text-sm opacity-75">Bank Name</label>
                            <input type="text" :value="userProfile.bankName ? 'Provided' : ''" disabled
                                class="w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed text-orange-500 font-semibold"
                                :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                        </div>
                        <div class="flex flex-col gap-2">
                            <label class="text-sm opacity-75">Bank Account Number</label>
                            <input type="text" :value="userProfile.bankNumber" disabled
                                class="w-full p-4 rounded-xl transition-all opacity-50 cursor-not-allowed"
                                :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-gray-200 border border-gray-300'" />
                        </div>
                    </template>
                </div>

                <div class="flex flex-col sm:flex-row gap-4 pt-4">
                    <button @click="saveProfile"
                        :disabled="!isSaveEnabled"
                        class="w-full sm:w-1/2 px-10 py-3 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
                        :class="isSaveEnabled ? 'bg-gradient-to-r from-orange-500 to-red-500 hover:-translate-y-1' : 'bg-gray-500 cursor-not-allowed'">
                        Save
                    </button>
                    <button @click="navigateToProfile"
                        class="w-full sm:w-1/2 px-10 py-3 font-semibold rounded-full transition-all duration-300 transform hover:bg-white/10 hover:border-white/50"
                        :class="theme === 'dark' ? 'border-2 border-white/20 text-white hover:bg-white/10 hover:border-white/50' : 'border-2 border-gray-400 text-gray-800 hover:bg-gray-200/50 hover:border-gray-500'">
                        Cancel
                    </button>
                </div>
            </div>
            <div v-else :class="cardClass" class="p-8 md:p-12 rounded-[2rem] text-center">
                <p>Failed to load user profile for editing. Please try again.</p>
            </div>
        </div>
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