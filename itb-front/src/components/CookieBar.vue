<script setup>
import { ref, onMounted, computed } from 'vue'
// ✨ FIX: 1. Import useRoute
import { useRouter, useRoute } from 'vue-router'
import { theme } from '@/stores/themeStore.js'

const router = useRouter()
const route = useRoute() // ✨ FIX: 2. ใช้งาน useRoute

// --- ✨ FIX: 3. เปลี่ยน State จาก true/false เป็น 'status' ---
const cookieStatus = ref('loading') // 'loading', 'needed', 'accepted', 'declined'
const cookieConsentKey = 'cookie_consent_expiry'

// --- ✨ FIX: 4. สร้าง computed property ควบคุมการแสดงผล ---
const showCookieBar = computed(() => {
  // ถ้ากดยอมรับแล้ว (accepted) หรือ กดปฏิเสธไปแล้ว (declined) -> ไม่ต้องแสดง
  if (cookieStatus.value === 'accepted' || cookieStatus.value === 'declined') {
    return false
  }
  // ถ้ากำลังอยู่ที่หน้า /about-cookie -> ไม่ต้องแสดง
  if (route.path === '/about-cookie') {
    return false
  }
  // ถ้าจำเป็นต้องแสดง (needed) และไม่ได้อยู่หน้า about -> ให้แสดง
  return cookieStatus.value === 'needed'
})

// 1. ฟังก์ชันเมื่อผู้ใช้กด "Accept"
const acceptCookies = () => {
  const expiryTime = Date.now() + (3 * 24 * 60 * 60 * 1000);
  localStorage.setItem(cookieConsentKey, expiryTime.toString());
  cookieStatus.value = 'accepted'; // ✨ FIX: 5. อัปเดต Status
};

// 2. ฟังก์ชันเมื่อผู้ใช้กด "Decline"
const declineCookies = () => {
  cookieStatus.value = 'declined'; // ✨ FIX: 6. อัปเดต Status (ซ่อนชั่วคราวจนกว่าจะโหลดหน้าใหม่)
};

// 3. ฟังก์ชันเมื่อผู้ใช้กด "More Info"
const goToCookiePolicy = () => {
  router.push('/about-cookie');
  // ✨ FIX: 7. ไม่ต้องซ่อน Bar ที่นี่ (Computed property จะจัดการเอง)
};

// 4. ตรวจสอบเมื่อ Component โหลด
onMounted(() => {
  const consentExpiry = localStorage.getItem(cookieConsentKey);
  
  if (consentExpiry) {
    const now = Date.now();
    if (now > parseInt(consentExpiry)) {
      localStorage.removeItem(cookieConsentKey);
      cookieStatus.value = 'needed'; // ✨ FIX: 8. อัปเดต Status
    } else {
      cookieStatus.value = 'accepted'; // ✨ FIX: 9. อัปเดต Status
    }
  } else {
    cookieStatus.value = 'needed'; // ✨ FIX: 10. อัปเดต Status
  }
});

// 5. Computed property สำหรับ Theme ของ Cookie Bar
const cookieBarClass = computed(() => {
  return theme.value === 'dark'
    ? 'bg-gray-900 text-slate-300 ring-1 ring-white/10'
    : 'bg-slate-100 text-slate-700 ring-1 ring-black/5';
});
</script>

<template>
  <transition name="slide-up">
    <div
      v-if="showCookieBar" 
      class="fixed bottom-0 left-0 right-0 z-50 p-6"
    >
      <div 
        :class="cookieBarClass"
        class="max-w-6xl mx-auto p-6 rounded-2xl shadow-xl flex flex-col md:flex-row items-center gap-6"
      >
        <div class="flex-shrink-0 text-3xl">🍪</div>
        <div class="flex-grow text-center md:text-left">
          <h3 class="font-semibold text-lg" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'">
            About Cookies
          </h3>
          <p class="text-sm">
            We use cookies to ensure you get the best experience on our website.
            By accepting, you agree to our terms.
          </p>
        </div>
        <div class="flex-shrink-0 flex items-center gap-4">
          <button 
            @click="declineCookies" 
            class="font-semibold px-4 py-2 rounded-full transition-colors"
            :class="theme === 'dark' ? 'hover:bg-gray-700' : 'hover:bg-slate-200'"
          >
            Decline
          </button>
          <button 
            @click="goToCookiePolicy" 
            class="font-semibold px-4 py-2 rounded-full transition-colors"
            :class="theme === 'dark' ? 'text-indigo-400 hover:bg-gray-700' : 'text-indigo-600 hover:bg-slate-200'"
          >
            More Info
          </button>
          <button 
            @click="acceptCookies"
            class="font-semibold px-6 py-2 rounded-full text-white bg-indigo-600 hover:bg-indigo-700 transition-all"
          >
            Accept
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
/* CSS สำหรับ Cookie Bar Animation */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(150%);
  opacity: 0;
}
</style>