<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();

const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12 max-w-4xl animate-fade-in-up">

      <div class="mb-12">
        <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Cookie Policy
        </h1>
        <p class="mt-4 text-lg" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Last updated: November 2, 2025
        </p>
      </div>

      <div 
        class="p-8 md:p-10 rounded-2xl space-y-8" 
        :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'"
      >
        <section>
          <h2 class="text-2xl font-bold mb-4">What are Cookies?</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            Cookies are small text files stored on your device (computer, phone, tablet) when you visit certain websites. They are widely used to make websites work, or work more efficiently, as well as to provide information to the owners of the site.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">How We Use Cookies</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            Our website uses a few essential cookies to manage your login session and security. We do not use third-party tracking or advertising cookies. The cookies we use are strictly necessary for the website to function.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">Types of Cookies We Use</h2>
          <div class="space-y-6">

            <div class="p-6 rounded-lg" :class="theme === 'dark' ? 'bg-gray-700/30' : 'bg-slate-100'">
              <h3 class="text-lg font-semibold mb-2 text-indigo-500 dark:text-indigo-400">1. Access</h3>
              <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
                <li>
                  **Purpose:** To verify your identity as you navigate the site.
                </li>
                <li>
                  **How it works:** This is your "digital keycard". It's sent with every request (like fetching your profile or adding an item to your cart) to prove to our server that you are logged in.
                </li>
                <li>
                  **Duration:** It is very short-lived (e.g., 10-15 minutes) for high security.
                </li>
              </ul>
            </div>

            <div class="p-6 rounded-lg" :class="theme === 'dark' ? 'bg-gray-700/30' : 'bg-slate-100'">
              <h3 class="text-lg font-semibold mb-2 text-indigo-500 dark:text-indigo-400">2. Refresh</h3>
              <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
                <li>
                  **Purpose:** To securely keep you logged in.
                </li>
                <li>
                  **How it works:** When your short-lived Access Token expires, this "master key" is securely and automatically used to get a new Access Token without forcing you to log in again.
                </li>
                <li>
                  **Security:** This token is stored as an **HttpOnly** cookie, meaning it cannot be accessed by browser scripts, which protects it from theft.
                </li>
                 <li>
                  **Duration:** This token lasts for your session (e.g., 1 day) and is destroyed when you log out.
                </li>
              </ul>
            </div>

            <div class="p-6 rounded-lg" :class="theme === 'dark' ? 'bg-gray-700/30' : 'bg-slate-100'">
              <h3 class="text-lg font-semibold mb-2 text-indigo-500 dark:text-indigo-400">3. Remember Me</h3>
              <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
                <li>
                  **Purpose:** To remember you after you close your browser.
                </li>
                <li>
                  **How it works:** If you check "Remember Me" during the OTP step, this cookie is created. When you return to our site days later, this token is used to log you in automatically, skipping the email, password, and OTP steps.
                </li>
                <li>
                  **Security:** This is also stored as an **HttpOnly** cookie for maximum security.
                </li>
                <li>
                  **Duration:** This token is long-lived (e.g., 30 days). If you do not tick "Remember Me", this cookie is not created, or it is actively removed.
                </li>
              </ul>
            </div>

          </div>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">Your Choices</h2>
          <p class="mb-4" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            When you first visit our site, you are given the choice to accept or decline our functional cookies.
          </p>
          <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            <li>**Accept:** You agree to our use of these essential tokens. We will remember your choice for 3 days.</li>
            <li>**Decline:** We will not store the `remember_token`. You will be asked again on your next visit.</li>
          </ul>
        </section>

        <div class="pt-6 border-t text-center" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
            <button 
                @click="router.go(-1)" 
                class="w-full sm:w-auto px-8 py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1">
                Go Back
            </button>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out forwards;
}
</style>