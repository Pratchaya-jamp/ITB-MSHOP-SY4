<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DotLottieVue } from '@lottiefiles/dotlottie-vue';
import { getItems,addItem } from '@/libs/fetchUtilsOur'


const route = useRoute();
const router = useRouter();
const verificationStatus = ref('verifying');
const message = ref('');

const theme = ref(localStorage.getItem('theme') || 'dark');
const themeClass = ref(theme.value === 'dark' ? 'bg-gray-950 text-white' : 'bg-white text-gray-950');

const applyTheme = (newTheme) => {
    document.body.className = newTheme === 'dark' ? 'dark-theme' : '';
    localStorage.setItem('theme', newTheme);
    theme.value = newTheme;
    themeClass.value = newTheme === 'dark' ? 'bg-gray-950 text-white' : 'bg-white text-gray-950';
};

const iconComponent = ref('');
const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark';
    applyTheme(newTheme);
    updateIconComponent();
};
const updateIconComponent = () => {
    iconComponent.value = theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`;
};
updateIconComponent();

const verifyEmail = async () => {
    const token = route.query.token;

    if (!token) {
        verificationStatus.value = 'error';
        message.value = 'Verification token is missing. Please check the link or request a new one.';
        return;
    }
    router.replace({ query: {} });
    // หน่วงเวลา 3 วินาที
    setTimeout(async () => {
        try {
            const response = await addItem(
  `http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/auth/verify-email?token=${token}`
);


            if (response.status === 200) {
                verificationStatus.value = 'success';
                message.value = 'Your account has been successfully activated.';
                // Redirect ไปหน้า Login หลังจากแสดงผล 3 วินาที
                setTimeout(() => {
                    router.push({ name: 'Login' });
                }, 3000);
            } else {
                if (response.status === 409) {
                    verificationStatus.value = 'warning';
                    message.value = 'This token has already been verified. Your account is already active.';
                } else {
                    const errorData = await response.json();
                    verificationStatus.value = 'error';
                    message.value = errorData.message || 'An error occurred, or the verification link has expired. Please request a new verification email.';
                }
            }
        } catch (error) {
            console.error('Error during email verification:', error);
            verificationStatus.value = 'error';
            message.value = 'An error occurred. Please check your network connection and try again.';
        }
    }, 3000); // หน่วงเวลา 3 วินาที
};

onMounted(() => {
    applyTheme(theme.value);
    verifyEmail();
});
</script>

<template>
    <div :class="themeClass" class="relative min-h-screen font-sans overflow-hidden flex flex-col items-center justify-center text-center">
        <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
            <div class="absolute w-96 h-96 bg-gradient-to-r from-orange-500 to-pink-500 rounded-full blur-3xl opacity-50 top-1/4 left-1/4 animate-blob"></div>
            <div class="absolute w-80 h-80 bg-gradient-to-r from-teal-400 to-blue-500 rounded-full blur-3xl opacity-50 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
        </div>

        <div class="relative z-10 p-8 md:p-12 rounded-[2rem] max-w-2xl w-full" :class="theme === 'dark' ? 'bg-gray-900 shadow-xl border border-gray-800' : 'bg-gray-100 shadow-xl border border-gray-200'">
            <div v-if="verificationStatus === 'verifying'" class="space-y-4">
                <DotLottieVue src="/sy4/animation/Loading.lottie" autoplay loop class="w-64 h-64 mx-auto mb-4"/>
                <h1 class="text-3xl md:text-4xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-pink-500">
                    Verifying your email...
                </h1>
                <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">
                    Please wait a moment while we activate your account.
                </p>
            </div>
            
            <div v-if="verificationStatus === 'success'" class="space-y-4 animate-fade-in-up">
                <DotLottieVue src="/sy4/animation/Loading_success.lottie" autoplay class="w-64 h-64 mx-auto mb-4"/>
                <h1 class="text-3xl md:text-4xl font-extrabold text-green-500">
                    Success!
                </h1>
                <p class="text-lg md:text-xl font-medium" :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
                    {{ message }}
                </p>
            </div>
            
            <div v-if="verificationStatus === 'error'" class="space-y-4 animate-fade-in-up">
                <DotLottieVue src="/sy4/animation/Loading_failed.lottie" autoplay class="w-64 h-64 mx-auto mb-4"/>
                <h1 class="text-3xl md:text-4xl font-extrabold text-red-500">
                    Verification Failed
                </h1>
                <p class="text-lg md:text-xl font-medium" :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
                    {{ message }}
                </p>
                <!-- <button
                    @click="() => window.location.href = 'http://intproj24.sit.kmutt.ac.th/<TEAMCODE>/forgot-password'"
                    class="mt-6 px-8 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
                >
                    Request a New Link
                </button> -->
            </div>
            <div v-if="verificationStatus === 'warning'" class="space-y-4 animate-fade-in-up">
                <DotLottieVue src="/sy4/animation/warning.lottie" autoplay class="w-64 h-64 mx-auto mb-4"/>
                <h1 class="text-3xl md:text-4xl font-extrabold text-yellow-500">
                    Already Verified!
                </h1>
                <p class="text-lg md:text-xl font-medium" :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
                    {{ message }}
                </p>
                <button
                    @click="router.push('/signin')"
                    class="mt-6 px-8 py-3 bg-gradient-to-r from-teal-400 to-blue-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
                >
                    Go to Login
                </button>
            </div>
        </div>

        <button
            @click="toggleTheme"
            class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50"
            :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'"
            v-html="iconComponent"
        >
        </button>
    </div>
</template>

<style scoped>
/* ส่วนของ @keyframes และอื่นๆ เหมือนเดิม */
@keyframes fade-in-up {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in-up {
    animation: fade-in-up 0.8s ease-out forwards;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.animate-spin {
    animation: spin 1s linear infinite;
}

@keyframes blob {
    0% { transform: scale(1) translate(0px, 0px); }
    33% { transform: scale(1.1) translate(30px, -50px); }
    66% { transform: scale(0.9) translate(-20px, 20px); }
    100% { transform: scale(1) translate(0px, 0px); }
}

.animate-blob {
    animation: blob 7s infinite ease-in-out;
}

.animation-delay-2000 {
    animation-delay: 2s;
}
</style>
