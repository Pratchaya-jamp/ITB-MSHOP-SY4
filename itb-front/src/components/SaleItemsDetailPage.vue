<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemById, deleteItemById } from '@/libs/fetchUtilsOur'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const id = route.params.id

const imageLoading = ref(true);
const imageError = ref(false);
const productImages = ref([])

const mainImage = ref('')
const showNotFoundPopup = ref(false)
const isDeleting = ref(false)
const showDeleteConfirmationPopup = ref(false)
const deleteResponseMessage = ref('')
const countdown = ref(3)
const startCountdown = () => {
    if (countdown.value > 0) {
        setTimeout(() => {
            countdown.value--
            startCountdown()
        }, 1000)
    }
}
const showEditSuccessPopup = ref(false)
const showEditFallPopup = ref(false)

const closeSuccessPopup = () => {
    showEditSuccessPopup.value = false
    showEditFallPopup.value = false
}

// **ปรับปรุง Theme Logic**
const theme = ref(localStorage.getItem('theme') || 'dark')

const applyTheme = (newTheme) => {
    document.body.className = newTheme === 'dark' ? 'dark-theme' : ''
    localStorage.setItem('theme', newTheme)
    theme.value = newTheme
}

const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(newTheme)
}

// function handleProductImages(images) {
//     if (!images) return
//     productImages.value = images
//         .sort((a, b) => a.imageViewOrder - b.imageViewOrder) // จัดเรียงตามลำดับ
//         .map(img => `/sy4/product-images/${img.fileName}`) // ชี้ไปที่โฟลเดอร์ public
// }

const handleMainImageError = () => {
    imageError.value = true;
    imageLoading.value = false;

    // ลองใช้รูปภาพสำรอง
    if (mainImage.value !== '/sy4/phone/iPhone.png') {
        mainImage.value = '/sy4/phone/iPhone.png';
        imageLoading.value = true;
    }
};

onMounted(async () => {
    try {
        const data = await getItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items', id)
        if (!data || data?.status === 404) {
            showNotFoundPopup.value = true
            startCountdown()
            setTimeout(() => {
                router.push('/sale-items')
            }, 3000)
            return
        }

        product.value = data
        if (data.saleItemImages && data.saleItemImages.length > 0) {
            const sortedImages = data.saleItemImages
                .sort((a, b) => a.imageViewOrder - b.imageViewOrder)
                .map(img => `http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/sale-items/images/${img.fileName}`)
      
            productImages.value = sortedImages
            mainImage.value = sortedImages[0]
        }

    } catch (error) {
        console.error('Failed to fetch product:', error)
        showNotFoundPopup.value = true
        startCountdown()
        setTimeout(() => {
            router.push('/sale-items')
        }, 3000)
    }
    // ใช้ onMounted เพื่อตั้งค่า theme เมื่อ component โหลดเสร็จ
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
        applyTheme(savedTheme)
    }
})

watch(() => mainImage.value, (newVal) => {
    if (newVal) {
        imageLoading.value = true;
        imageError.value = false;
    }
})

watch(
    () => route.query.editSuccess,
    (editSuccess) => {
        if (editSuccess === 'true') {
            setTimeout(() => {
                showEditSuccessPopup.value = true
            }, 200)
            router.replace({ path: route.path, query: {} })
            //router.go(0)
        }
    },
    { immediate: true }
)

watch(
    () => route.query.editFail,
    (editFail) => {
        if (editFail === 'true') {
            setTimeout(() => {
                showEditFallPopup.value = true
            }, 200)
            router.replace({ path: route.path, query: {} })
        }
    },
    { immediate: true }
)

const deleteproduct = async () => {
    showDeleteConfirmationPopup.value = true
}

const confirmDelete = async () => {
    showDeleteConfirmationPopup.value = false
    isDeleting.value = true
    try {
        const statusCode = await deleteItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items', id)
        if (statusCode === 204) {
            setTimeout(() => {
                isDeleting.value = false
                router.push({ path: '/sale-items', query: { deleteSuccess: 'true' } })
            }, 1000)
        } else if (statusCode === 404) {
            isDeleting.value = false
            showNotFoundPopup.value = true
            startCountdown()
            setTimeout(() => {
                router.push('/sale-items')
            }, 3000)
        }
    } catch (error) {
        console.error('delete Fall:', error)
        deleteResponseMessage.value = 'เกิดข้อผิดพลาดในการลบสินค้า'
        isDeleting.value = false
    }
}

const cancelDeleteItem = () => {
    showDeleteConfirmationPopup.value = false
}

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

const iconComponent = computed(() => {
    return theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>` // sun icon
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>` // moon icon
})
</script>

<template>
    <div :class="themeClass" class="p-4 w-full min-h-screen font-sans transition-colors duration-500">
        <nav class="text-sm mb-6 max-w-6xl mx-auto transition-colors duration-500"
            :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
            <router-link to="/sale-items" class="itbms-home-button hover:underline cursor-pointer">
                Home
            </router-link>
            <span class="mx-2 text-gray-400">/</span>
            <span class="itbms-row font-medium transition-colors duration-500"
                :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
                {{ product?.model || '-' }}
            </span>
        </nav>

        <div class="ltbms-product-detail grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-8 p-6 rounded-2xl shadow-xl max-w-6xl mx-auto transition-colors duration-500"
            :class="theme === 'dark' ? 'bg-gray-900' : 'bg-white'">
            <div class="flex flex-col">
                <!-- Main Product Image -->
                <div class="relative w-full aspect-[4/3] overflow-hidden rounded-xl shadow-lg mb-4 bg-gray-100">
                    <img :src="mainImage"
                        :alt="product?.model ? `${product?.brandName} ${product?.model}` : 'Product image'"
                        class="w-full h-full object-contain transition-transform duration-500 hover:scale-105"
                        @load="imageLoading = false" @error="handleMainImageError" />
                </div>

                <!-- Thumbnail Gallery -->
                <div v-if="productImages.length > 0"
                    class="flex gap-3 px-4 justify-start overflow-x-auto pb-4 scrollbar-thin">

                    <div v-for="(img, index) in productImages" :key="`thumbnail-${index}`"
                        class="relative flex-shrink-0" @click="mainImage = img; mainImageError = false">

                        <img :src="img"
                            :alt="`${product?.brandName || ''} ${product?.model || ''} - ภาพที่ ${index + 1}`"
                            class="w-20 h-20 object-cover rounded-lg cursor-pointer border-2 shadow-sm transition-all duration-300"
                            :class="{
                                'border-blue-500 scale-105': img === mainImage,
                                'border-transparent hover:border-gray-300': img !== mainImage
                            }" />
                    </div>
                </div>
            </div>

            <div class="itbms-row space-y-5 text-base transition-colors duration-500">
                <h1 class="text-3xl lg:text-4xl font-bold mb-2">
                    {{ product?.model || 'Product Name' }}
                </h1>

                <div class="flex flex-wrap items-center gap-2">
                    <div class="itbms-brand text-xl font-medium">
                        <span :class="{ 'text-gray-400': !product?.brandName }">
                            {{ product?.brandName || 'Unknown Brand' }}
                        </span>
                    </div>
                    <div class="text-gray-400">•</div>
                    <div class="itbms-color text-lg font-medium">
                        <span :class="{ 'text-gray-400': !product?.color }">
                            {{ product?.color || 'Color' }}
                        </span>
                    </div>
                </div>

                <div class="itbms-price text-4xl font-extrabold text-blue-500 mb-4">
                    <span :class="{ 'text-gray-400': product?.price === null || product?.price === undefined }">
                        {{ product?.price !== null && product?.price !== undefined ? product?.price.toLocaleString() :
                            '-' }}
                    </span>
                    <span class="text-xl font-normal ml-1">฿</span>
                </div>

                <p class="itbms-description text-lg transition-colors duration-500"
                    :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
                    {{ product?.description || 'No description available for this product.' }}
                </p>

                <div class="grid grid-cols-2 gap-4">
                    <div class="itbms-ramGb">
                        <strong class="font-semibold block text-gray-500 mb-1">RAM</strong>
                        <span class="text-xl font-semibold" :class="{ 'text-gray-400': !product?.ramGb }">
                            {{ product?.ramGb || '-' }}
                            <span class="text-base font-normal">GB</span>
                        </span>
                    </div>
                    <div class="itbms-storageGb">
                        <strong class="font-semibold block text-gray-500 mb-1">Storage</strong>
                        <span class="text-xl font-semibold" :class="{ 'text-gray-400': !product?.storageGb }">
                            {{ product?.storageGb || '-' }}
                            <span class="text-base font-normal">GB</span>
                        </span>
                    </div>
                    <div class="itbms-screenSizeInch">
                        <strong class="font-semibold block text-gray-500 mb-1">Screen Size</strong>
                        <span class="text-xl font-semibold" :class="{ 'text-gray-400': !product?.screenSizeInch }">
                            {{ product?.screenSizeInch || '-' }}
                            <span class="text-base font-normal">Inches</span>
                        </span>
                    </div>
                    <div class="itbms-quantity">
                        <strong class="font-semibold block text-gray-500 mb-1">Available</strong>
                        <span class="text-xl font-semibold"
                            :class="{ 'text-green-500': product?.quantity > 0, 'text-red-500': product?.quantity <= 0 }">
                            {{ product?.quantity !== null && product?.quantity !== undefined ? product?.quantity : '-'
                            }}
                            <span class="text-base font-normal">Units</span>
                        </span>
                    </div>
                </div>

                <div class="flex flex-col sm:flex-row gap-4 pt-4">
                    <button @click="router.push(`/sale-items/${product.id}/edit`)"
                        class="itbms-edit-button w-full font-semibold border-2 rounded-xl px-6 py-3 transition-all duration-300 transform active:scale-95 shadow-md hover:cursor-pointer"
                        :class="theme === 'dark'
                            ? 'bg-yellow-500 text-white border-yellow-500 hover:bg-yellow-600'
                            : 'bg-yellow-500 text-white border-yellow-500 hover:bg-yellow-600'">
                        Edit Product
                    </button>
                    <button @click="deleteproduct"
                        class="itbms-delete-button w-full font-semibold border-2 rounded-xl px-6 py-3 transition-all duration-300 transform active:scale-95 shadow-md hover:cursor-pointer"
                        :class="theme === 'dark'
                            ? 'bg-red-500 text-white border-red-500 hover:bg-red-600'
                            : 'bg-red-500 text-white border-red-500 hover:bg-red-600'">
                        Delete Product
                    </button>
                </div>
            </div>
        </div>

        <transition name="bounce-popup">
            <div v-if="showDeleteConfirmationPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">Confirm Deletion</h2>
                    <p class="itbms-message mb-6 text-lg">Are you sure you want to delete this sale item?</p>
                    <div class="flex justify-center gap-4">
                        <button @click="confirmDelete"
                            class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
                        <button @click="cancelDeleteItem"
                            class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">No</button>
                    </div>
                </div>
            </div>
        </transition>

        <transition name="bounce-popup">
            <div v-if="showNotFoundPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center max-w-sm w-full transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4">⚠️ Item not found.</h2>
                    <p class="itbms-message mb-4 text-lg">The requested sale item does not exist.</p>
                    <p class="text-sm transition-colors duration-500"
                        :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                        Redirecting in {{ countdown }} second<span v-if="countdown > 1">s</span>...
                    </p>
                </div>
            </div>
        </transition>

        <transition name="fade-background">
            <div v-if="isDeleting"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
                <div class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
                        fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
                    </svg>
                    <p class="itbms-message text-lg font-medium">Deleting product...</p>
                </div>
            </div>
        </transition>

        <transition name="bounce-popup">
            <div v-if="showEditSuccessPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4 text-green-500">Success!</h2>
                    <p class="itbms-message mb-6 text-lg">The sale item has been successfully updated!</p>
                    <button @click="closeSuccessPopup"
                        class="bg-green-500 text-white border-2 border-green-500 rounded-full px-6 py-2 transition-colors duration-300 hover:bg-transparent hover:text-green-500 font-semibold hover:cursor-pointer">Done</button>
                </div>
            </div>
        </transition>

        <transition name="bounce-popup">
            <div v-if="showEditFallPopup"
                class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
                <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                    :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                    <h2 class="text-2xl font-bold mb-4 text-red-500">The sale item has been fail to Edit!</h2>
                    <p class="itbms-message mb-6 text-lg">Please try again later.</p>
                    <button @click="closeSuccessPopup"
                        class="bg-blue-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-blue-600 active:scale-95 hover:cursor-pointer">Done</button>
                </div>
            </div>
        </transition>

        <button @click="toggleTheme"
            class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-md shadow-lg transition-all duration-300 z-50 hover:shadow-2xl hover:cursor-pointer"
            :class="theme === 'dark' ? 'bg-gray-700/80 text-white' : 'bg-gray-200/80 text-black'"
            v-html="iconComponent">
        </button>
    </div>
</template>

<style scoped>
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

/* New styles for better visuals */
.ltbms-product-detail {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.bg-gray-900 .ltbms-product-detail {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
}

/* Hide scrollbar for thumbnail gallery but keep functionality */
.overflow-x-auto::-webkit-scrollbar {
    display: none;
}

.overflow-x-auto {
    -ms-overflow-style: none;
    /* IE and Edge */
    scrollbar-width: none;
    /* Firefox */
}
</style>
