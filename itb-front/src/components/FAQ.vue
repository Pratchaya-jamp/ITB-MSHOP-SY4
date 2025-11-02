<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();

// --- Theming (มาจาก Store) ---
const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

// --- State สำหรับ Accordion ---
// (null = ปิดทั้งหมด, 0 = เปิดอันแรก, 1 = เปิดอันสอง)
const openFaqIndex = ref(null);

const toggleFaq = (index) => {
  if (openFaqIndex.value === index) {
    openFaqIndex.value = null; // ถ้ากดซ้ำอันเดิม ให้ปิด
  } else {
    openFaqIndex.value = index; // ถ้ากดอันใหม่ ให้อัปเดต
  }
};

// --- Mock Data สำหรับคำถาม-คำตอบ ---
const faqs = ref([
  {
    q: "How do I become a seller?",
    a: "To become a seller, you must first register for a regular user account. After logging in, navigate to your profile page where you will find an option to 'Upgrade to Seller'. You will be required to provide additional information, such as bank details, for verification."
  },
  {
    q: "How long does order processing take?",
    a: "Once an order is 'PLACED', the seller is notified. The status will change to 'PAID' once the payment is confirmed. After that, the seller is responsible for shipping. A 'COMPLETED' status means the transaction is finished."
  },
  {
    q: "What is Two-Factor Authentication (OTP)?",
    a: "For enhanced security, our site uses a One-Time Password (OTP) system. If you log in from a new device or after your 'Remember Me' session expires, we will send a 6-digit code to your registered email. You must enter this code to complete your login."
  },
  {
    q: "What is 'Remember Me'?",
    a: "If you check 'Remember Me' during the OTP verification step, we will securely store a 'remember_token' in your browser. This allows you to stay logged in for up to 30 days without needing to re-enter your email, password, or OTP."
  },
  {
    q: "How do I manage my items? (For Sellers)",
    a: "Sellers have access to a dedicated dashboard (List View) where they can add new items, edit existing item details (price, quantity, etc.), and delete items that are no longer for sale."
  }
]);

</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12 max-w-4xl animate-fade-in-up">

      <div class="mb-12">
        <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Frequently Asked Questions
        </h1>
        <p class="mt-4 text-xl" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Find answers to common questions about our platform.
        </p>
      </div>

      <div 
        class="p-8 md:p-10 rounded-2xl space-y-4" 
        :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'"
      >
        
        <div 
          v-for="(faq, index) in faqs" 
          :key="index" 
          class="border-b" 
          :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'"
        >
          <button 
            @click="toggleFaq(index)" 
            class="w-full flex justify-between items-center py-5 text-left"
          >
            <span class="text-lg font-semibold" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'">
              {{ faq.q }}
            </span>
            <svg 
              xmlns="http://www.w3.org/2000/svg" 
              class="h-5 w-5 transition-transform duration-300 flex-shrink-0" 
              :class="{ 'rotate-180': openFaqIndex === index }" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor" 
              stroke-width="2"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          
          <transition name="accordion">
            <div 
              v-if="openFaqIndex === index"
              class="pb-5"
            >
              <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
                {{ faq.a }}
              </p>
            </div>
          </transition>
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

/* Accordion Animation */
.accordion-enter-active,
.accordion-leave-active {
  transition: max-height 0.4s ease-in-out, opacity 0.3s ease-in-out;
  max-height: 200px; /* ตั้งค่าความสูงสูงสุดเผื่อไว้ */
  opacity: 1;
}
.accordion-enter-from,
.accordion-leave-to {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
}
</style>