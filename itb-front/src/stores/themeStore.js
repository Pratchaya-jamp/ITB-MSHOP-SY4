// src/themeStore.js
import { ref } from 'vue'

// สร้างตัวแปร theme แบบ reactive และอ่านค่าเริ่มต้นจาก localStorage
export const theme = ref(localStorage.getItem('theme') || 'dark')

// สร้างฟังก์ชันสำหรับเปลี่ยน theme
export function toggleTheme() {
  const newTheme = theme.value === 'dark' ? 'light' : 'dark'
  theme.value = newTheme // อัปเดตค่า reactive
  localStorage.setItem('theme', newTheme) // บันทึกลง localStorage
  document.body.className = newTheme === 'dark' ? 'dark-theme' : '' // อัปเดต class ที่ body
}

// ฟังก์ชันสำหรับ apply theme ตอนเริ่มต้น
export function applyInitialTheme() {
    document.body.className = theme.value === 'dark' ? 'dark-theme' : ''
}