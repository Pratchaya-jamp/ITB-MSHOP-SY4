<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { addItem, editItem, getItems, getItemById } from '@/libs/fetchUtilsOur'

const router = useRouter()
const route = useRoute()

const product = ref({
    id: '',
    brandName: '',
    model: '',
    price: '',
    description: '',
    ramGb: '',
    screenSizeInch: '',
    storageGb: '',
    color: '',
    quantity: '',
})

const pictures = ref([])
const warningMessage = ref('')
const mainImage = ref('')
const placeholder = '/sy4/phone/iPhone.png'

const responseMessage = ref('')
const originalProduct = ref(null)
const addnewitemMessage = ref('New Sale Item') // แก้คำว่า ltem เป็น Item
const selectedBrandId = ref(null)

// State สำหรับควบคุมการแสดง Pop-up
const showConfirmationAddPopup = ref(false)
const showConfirmationEditPopup = ref(false)
const isLoading = ref(false)
const brandList = ref([])
const id = route.params.id
const isEditMode = ref(false)
const isAdding = computed(() => !isEditMode.value)
const countdown = ref(3)
const startCountdown = () => {
    if (countdown.value > 0) {
        setTimeout(() => {
            countdown.value--
            startCountdown()
        }, 1000)
    }
}
const showNotFoundPopup = ref(false)

// Valid flags
const isPriceValid = ref(false)
const isModelValid = ref(false)
const isDescriptionValid = ref(false)
const isRamValid = ref(true)
const isScreenValid = ref(true)
const isStorageValid = ref(true)
const isColorValid = ref(true)
const isQuantityValid = ref(true)
const isBrandSelected = ref(false); // New validation flag for brand

// ติดตาม Valid แบบ real-time
const modelError = ref('')
const priceError = ref('')
const descriptionError = ref('')
const ramError = ref('')
const screenSizeError = ref('')
const storageError = ref('')
const colorError = ref('')
const quantityError = ref('')
const brandError = ref('')

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

// --- Upload Handler ---
function handleFileUpload(event) {
    const input = event.target
    if (!input.files) return

    const selectedFiles = Array.from(input.files)
    const allowedExtensions = ['jpg', 'jpeg', 'png', 'webp']

    for (const file of selectedFiles) {
        const fileExtension = file.name.split('.').pop().toLowerCase()

        if (!allowedExtensions.includes(fileExtension)) {
            warningMessage.value = `File type "${fileExtension}" is not allowed. Only ${allowedExtensions.join(', ')} are supported.`;
            continue;
        }

        if (file.size > 2 * 1024 * 1024) {
            warningMessage.value = `File "${file.name}" exceeds 2MB limit.`
            continue
        }
        if (pictures.value.length >= 4) {
            warningMessage.value = 'Maximum 4 pictures are allowed.'
            break
        }
        pictures.value.push({
            name: file.name,
            file,
            previewUrl: URL.createObjectURL(file),
        })
    }

    if (pictures.value.length > 0) {
        mainImage.value = pictures.value[0].previewUrl
    }

    input.value = ''
}

// --- Reorder functions ---
function moveUp(index) {
    if (index === 0) return
    const newPictures = [...pictures.value]
    const temp = newPictures[index]
    newPictures[index] = newPictures[index - 1]
    newPictures[index - 1] = temp
    
    pictures.value = newPictures;
    mainImage.value = pictures.value[0].previewUrl
}

function moveDown(index) {
    if (index === pictures.value.length - 1) return
    const newPictures = [...pictures.value]
    const temp = newPictures[index]
    newPictures[index] = newPictures[index + 1]
    newPictures[index + 1] = temp

    pictures.value = newPictures;
    mainImage.value = pictures.value[0].previewUrl
}

// --- Remove picture ---
function removePicture(index) {
    const removed = pictures.value.splice(index, 1)[0]
    if (removed) {
        URL.revokeObjectURL(removed.previewUrl) 
    }

    if (pictures.value.length > 0) {
        mainImage.value = pictures.value[0].previewUrl
    } else {
        mainImage.value = ''
    }
}

function getImageUrl(item) {
    return item.previewUrl || item.url;
}

const currentMainImage = computed(() => {
    const firstImage = pictures.value[0];
    return firstImage ? (firstImage.previewUrl || firstImage.url) : placeholder;
});

onMounted(async () => {
    try {
        const data = await getItems('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/brands')
        brandList.value = data.sort((a, b) => {
            if (a.brandName < b.brandName) {
                return -1;
            }
            if (a.brandName > b.brandName) {
                return 1;
            }
            return 0;
        });
    } catch (err) {
        console.error('Error loading items:', err)
    }
    if (id) {
        isEditMode.value = true
        const data = await getItemById('http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items', id)
        if (data) {
            const formattedProduct = {
                id: data.id,
                brandName: data.brandName,
                model: data.model,
                price: data.price,
                description: data.description,
                ramGb: data.ramGb,
                screenSizeInch: data.screenSizeInch,
                storageGb: data.storageGb,
                color: data.color,
                quantity: data.quantity,
            }
            product.value = { ...formattedProduct }
            originalProduct.value = { ...formattedProduct }

            const selectedBrand = brandList.value.find(brand => brand.brandName === data.brandName);
            if (selectedBrand) {
                selectedBrandId.value = selectedBrand.id;
            }

            const existingImages = data.saleItemImages
                .sort((a, b) => a.imageViewOrder - b.imageViewOrder)
                .map(img => ({
                id: img.id,
                name: img.fileName,
                url: `http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/sale-items/images/${img.fileName}`,
                isExisting: true
          }));

            pictures.value = existingImages;

            if (pictures.value.length > 0) {
                mainImage.value = pictures.value[0].url;
            }
        } else {
            if (!data || data?.status === 404) {
                showNotFoundPopup.value = true
                startCountdown()

                setTimeout(() => {
                    router.push('/sale-items')
                }, 3000)
                return
            }
        }
    }
    if (!isEditMode.value) {
        product.value.price = '';
        product.value.model = '';
        product.value.description = '';
        product.value.quantity = '';
    }
    // ดึงค่า theme จาก localStorage เมื่อ component โหลด
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
        applyTheme(savedTheme)
    }
})

// --- Price ---
watch(() => product.value.price, (newVal) => {
    const val = Number(newVal)
    if (newVal === null || newVal === '' || isNaN(val) || !Number.isInteger(val) || val < 0) {
        priceError.value = 'Price must be non-negative integer.'
        isPriceValid.value = false
    } else if (val > 2147483647) {
        priceError.value = `The price must not exceed 2147483647 Baht.`
        isPriceValid.value = false
    } else {
        priceError.value = ''
        isPriceValid.value = true
    }
})

// --- Model ---
watch(() => product.value.model, (newVal) => {
    const val = newVal?.trim() ?? ''
    if (!val || val.length > 60) {
        modelError.value = 'Model must be 1-60 characters long.'
        isModelValid.value = false
    } else {
        modelError.value = ''
        isModelValid.value = true
    }
})

// --- Description ---
watch(() => product.value.description, (newVal) => {
    const val = newVal?.trim() ?? ''
    if (!val || val.length > 16384) {
        descriptionError.value = 'Description must be 1-16,384 characters long.'
        isDescriptionValid.value = false
    } else {
        descriptionError.value = ''
        isDescriptionValid.value = true
    }
})

// --- RAM (optional) ---
watch(() => product.value.ramGb, (newVal) => {
    const val = Number(newVal)
    if (newVal === null || newVal === '') {
        ramError.value = ''
        isRamValid.value = true
    } else if (!Number.isInteger(val) || val <= 0) {
        ramError.value = 'RAM size must be positive integer or not specified.'
        isRamValid.value = false
    } else if (val > 2147483647) {
        ramError.value = `RAM must not exceed 2147483647 GB.`
        isRamValid.value = false
    } else {
        ramError.value = ''
        isRamValid.value = true
    }
})

// --- Screen Size (optional) ---
watch(() => product.value.screenSizeInch, (newVal) => {
    // Check if newVal is null or undefined before attempting to use it
    if (newVal === null || newVal === '') {
        screenSizeError.value = '';
        isScreenValid.value = true;
        return; // Stop execution here
    }

    const val = Number(newVal);
    // Use newVal directly for the test, as it's now guaranteed to not be null or empty string.
    const isValidFormat = /^\d{1,8}(\.\d{1,2})?$/.test(newVal.toString()); 

    if (val <= 0 || !isValidFormat) {
        screenSizeError.value = 'Screen size must be positive number with at most 2 decimal points or not specified.';
        isScreenValid.value = false;
    } else {
        screenSizeError.value = '';
        isScreenValid.value = true;
    }
});

// --- Storage (optional) ---
watch(() => product.value.storageGb, (newVal) => {
    const val = Number(newVal)
    if (newVal === null || newVal === '') {
        storageError.value = ''
        isStorageValid.value = true
    } else if (!Number.isInteger(val) || val < 1) {
        storageError.value = 'Storage size must be positive integer or not specified.'
        isStorageValid.value = false
    } else if (val > 2147483647) {
        storageError.value = `Storage must not exceed 2147483647 GB.`
        isStorageValid.value = false
    } else {
        storageError.value = ''
        isStorageValid.value = true
    }
})

// --- Color (optional) ---
watch(() => product.value.color, (newVal) => {
    const val = newVal?.trim() ?? ''
    if (val.length > 40) {
        colorError.value = 'Color must be 1-40 characters long or not specified.'
        isColorValid.value = false
    } else {
        colorError.value = ''
        isColorValid.value = true
    }
})

// --- Quantity ---
watch(() => product.value.quantity, (newVal) => {
    const val = Number(newVal)
    if (newVal === null || newVal === '') {
        quantityError.value = ''
        isQuantityValid.value = true
    } else if (!Number.isInteger(val) || val < 0) {
        quantityError.value = 'Quantity must be non-negative integer.' // แก้ class เป็น itbms-message
        isQuantityValid.value = false
    } else if (val > 2147483647) {
        quantityError.value = 'The quantity of products must not exceed 2147483647.'
        isQuantityValid.value = false
    } else {
        quantityError.value = ''
        isQuantityValid.value = true
    }
})

// --- Brand ---
watch(selectedBrandId, (newVal) => {
    if (newVal) {
        isBrandSelected.value = true;
        brandError.value = '';
    } else {
        isBrandSelected.value = false;
        brandError.value = 'Brand must be selected.';
    }
})

const isModified = computed(() => {
    if (!originalProduct.value) return true
    const productChanged = Object.keys(product.value).some(key => {
        const currentValue = String(product.value[key] || '').trim();
        const originalValue = String(originalProduct.value[key] || '').trim();
        return currentValue !== originalValue;
    });

    const originalBrandId = brandList.value.find(brand => brand.brandName === originalProduct.value.brandName)?.id;
    const brandChanged = selectedBrandId.value !== originalBrandId;

    return productChanged || brandChanged;
})


const isValid = () => {
    return (
        isBrandSelected.value &&
        isPriceValid.value &&
        isModelValid.value &&
        isDescriptionValid.value &&
        isRamValid.value &&
        isScreenValid.value &&
        isStorageValid.value &&
        isColorValid.value &&
        isQuantityValid.value
    );
};

const submitForm = async () => {
    // Force validation on brand selection before showing popup
    if (!selectedBrandId.value) {
        validateBrand();
    }
    if (!isValid()) {
        responseMessage.value = 'กรุณากรอกข้อมูลให้ครบถ้วนและถูกต้อง';
        return;
    }

    if (isEditMode.value) {
        showConfirmationEditPopup.value = true
    } else {
        showConfirmationAddPopup.value = true
    }
}

const confirmAddItem = async () => {
    showConfirmationAddPopup.value = false
    showConfirmationEditPopup.value = false
    isLoading.value = true

    const selectedBrand = brandList.value.find(brand => brand.id === selectedBrandId.value);
    
    const saleItem = {
        brand: selectedBrand ? {
            id: selectedBrand.id,
            brandName: selectedBrand.brandName
        } : null,
        model: product.value.model?.trim() || '',
        description: product.value.description?.trim() || '',
        price: parseFloat(product.value.price),
        ramGb: product.value.ramGb ? parseInt(product.value.ramGb) : null,
        screenSizeInch: product.value.screenSizeInch
            ? parseFloat(product.value.screenSizeInch)
            : null,
        storageGb: product.value.storageGb ? parseInt(product.value.storageGb) : null,
        quantity: parseInt(product.value.quantity),
        color: product.value.color?.trim() || ''
    }

    
    const formData = new FormData()
    // แนบ JSON ใน field saleItem
    formData.append(
        'saleItem',
        new Blob([JSON.stringify(saleItem)], { type: 'application/json' })
    )
    
    // แนบรูปทั้งหมด (แก้ไขให้เข้าถึง .file)
    pictures.value.forEach((picture) => {
        formData.append('pictures', picture.file, picture.name)
    })

    try {
        let result
        if (isEditMode.value) {
            result = await editItem(
                'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items',
                id,
                formData,
                true // ส่งแบบ multipart
            )

            if (result.status !== 200 || !result.data?.id) {
                throw new Error('Edit failed or invalid data returned')
            }

            setTimeout(() => {
                isLoading.value = false
                router.push({
                    path: `/sale-items/${id}`,
                    query: { editSuccess: 'true' }
                })
            }, 1000)
        } else {
            result = await addItem(
                'http://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v1/sale-items',
                formData,
                true
            )

            if (result.status !== 201 || !result.data?.id) {
                throw new Error('Add failed or invalid data returned')
            }

            setTimeout(() => {
                isLoading.value = false
                router.push({
                    path: '/sale-items',
                    query: { addSuccess: 'true' }
                })
            }, 1000)
        }
    } catch (err) {
        console.error(err)
        responseMessage.value = isEditMode.value
            ? 'เกิดข้อผิดพลาดในการแก้ไขสินค้า'
            : 'เกิดข้อผิดพลาดในการเพิ่มสินค้า'
        isLoading.value = false

        router.push({
            path: isEditMode.value ? `/sale-items/${id}` : '/sale-items',
            query: { [isEditMode.value ? 'editFail' : 'addFail']: 'true' }
        })
    }
}


const cancelAddItem = () => {
    showConfirmationAddPopup.value = false
    showConfirmationEditPopup.value = false
}
</script>

<template>
    <head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
</head>
  <div :class="themeClass" class="min-h-screen font-sans transition-colors duration-500 p-6 sm:p-8">
    
    <nav class="mb-6 max-w-6xl mx-auto text-sm transition-colors duration-500"
      :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
      <router-link to="/sale-items" class="itbms-home-button hover:underline cursor-pointer">
        Home
      </router-link>
      <span class="mx-2">/</span>
      <span v-if="product?.id" class="itbms-row font-medium transition-colors duration-500"
        :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
        <router-link :to="{ path: `/sale-items/${product.id}` }"
          class="itbms-back-button hover:underline cursor-pointer">
          {{ product?.model || '-' }}
        </router-link>
      </span>
      <span v-else class="itbms-row font-medium transition-colors duration-500"
        :class="theme === 'dark' ? 'text-gray-200' : 'text-gray-800'">
        {{ addnewitemMessage }}
      </span>
    </nav>

    <div class="max-w-6xl mx-auto rounded-3xl shadow-2xl overflow-hidden transition-colors duration-500"
      :class="theme === 'dark' ? 'bg-gray-900' : 'bg-white'">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 p-6 lg:p-12">
        
        <div class="flex flex-col items-center lg:items-start space-y-4">
    <div class="relative w-full aspect-video overflow-hidden rounded-2xl shadow-lg">
        <img :src="currentMainImage" alt="Main Product Image"
             class="itbms-product-image w-full h-full object-contain transition-transform duration-500 hover:scale-105"
             @error="e => e.target.src = placeholder" />
    </div>

          <p v-if="warningMessage" class="text-red-500 text-sm w-full text-center lg:text-left">{{ warningMessage }}</p>

          <div class="flex flex-wrap justify-center lg:justify-start gap-3 w-full max-w-full overflow-x-auto pb-2">
        <div v-for="(file, index) in pictures" :key="file.name || index" class="relative flex flex-col items-center">
            <img :src="getImageUrl(file)" alt="Thumbnail"
                 class="w-20 h-20 object-cover rounded-lg cursor-pointer border-2 shadow-sm transition-all duration-300"
                 :class="{
                     'border-orange-500 scale-105': getImageUrl(file) === mainImage,
                     'border-transparent hover:border-gray-400': getImageUrl(file) !== mainImage
                 }"
                 @click="mainImage = getImageUrl(file)" />
              
              <button @click="removePicture(index)"
                    class="absolute top-1 right-1 bg-red-500 text-white rounded-full h-5 w-5 flex items-center justify-center text-xs transition-colors hover:bg-red-600">
                <i class="fas fa-times"></i>
            </button>
        </div>

            <label
            class="flex flex-col items-center justify-center w-20 h-20 rounded-lg border-2 border-dashed border-gray-300 cursor-pointer hover:border-orange-500 hover:bg-orange-50 transition">
            <span class="text-xs text-gray-500 text-center px-1">
                <i class="fas fa-plus"></i><br>
                Upload
            </span>
            <input type="file" multiple accept="image/*" class="hidden" @change="handleFileUpload" />
        </label>
    </div>

          <div class="w-full space-y-2 mt-4">
    <h3 class="font-semibold text-lg" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">Uploaded Images</h3>
    <div v-if="pictures.length > 0" class="space-y-2">
        <div v-for="(file, index) in pictures" :key="file.name || file.newPictureName"
            class="flex items-center justify-between p-3 rounded-xl transition-all duration-300"
            :class="theme === 'dark' ? 'bg-gray-800' : 'bg-gray-100'">
            <div class="flex items-center min-w-0">
                <span class="truncate text-sm" :class="theme === 'dark' ? 'text-white' : 'text-gray-800'">
                    {{ file.newPictureName || file.name || 'new-image.jpg' }}
                </span>
            </div>
            <div class="flex-shrink-0 flex gap-2 ml-4">
                <button @click="moveUp(index)" :disabled="index === 0"
                    class="p-1 rounded-full transition-colors duration-300"
                    :class="[
                      index === 0 ? 'text-gray-400 cursor-not-allowed' : 'text-gray-500 hover:bg-gray-300 hover:text-black',
                      theme === 'dark' ? 'hover:bg-gray-700 hover:text-white' : ''
                    ]">
                    <i class="fas fa-chevron-up"></i>
                </button>
                <button @click="moveDown(index)" :disabled="index === pictures.length - 1"
                    class="p-1 rounded-full transition-colors duration-300"
                    :class="[
                      index === pictures.length - 1 ? 'text-gray-400 cursor-not-allowed' : 'text-gray-500 hover:bg-gray-300 hover:text-black',
                      theme === 'dark' ? 'hover:bg-gray-700 hover:text-white' : ''
                    ]">
                    <i class="fas fa-chevron-down"></i>
                </button>
                <button @click="removePicture(index)"
                    class="p-1 text-red-500 rounded-full hover:bg-red-100 transition-colors duration-300">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    </div>
    <div v-else class="text-sm text-gray-500" :class="theme === 'dark' ? 'text-gray-400' : ''">
        No images have been uploaded yet.
    </div>
          </div>
        </div>

        <div class="space-y-6 text-base transition-colors duration-500"
          :class="theme === 'dark' ? 'text-white' : 'text-gray-950'">
          <h2 class="text-3xl font-bold mb-4">
            {{ product?.id ? 'Edit Product' : 'Add New Product' }}
          </h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-y-4 gap-x-6">
            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Brand<span class="text-red-500">*</span>
              </label>
              <select v-if="brandList.length > 0" v-model="selectedBrandId"
                class="itbms-brand p-2 rounded-lg w-full border transition-colors duration-300 hover:cursor-pointer focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'"
                @blur="validateBrand">
                <option value="">Select Brand</option>
                <option v-for="brand in brandList" :key="brand.id" :value="brand.id">
                  {{ brand.brandName }}
                </option>
              </select>
              <p v-if="brandError" class="itbms-message text-red-500 text-sm mt-1">{{ brandError }}</p>
            </div>

            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Model<span class="text-red-500">*</span>
              </label>
              <input v-model="product.model" type="text"
                class="itbms-model p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="modelError" class="itbms-message text-red-500 text-sm mt-1">{{ modelError }}</p>
            </div>

            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Price (Baht)<span class="text-red-500">*</span>
              </label>
              <input v-model="product.price" type="number"
                class="itbms-price p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="priceError" class="itbms-message text-red-500 text-sm mt-1">{{ priceError }}</p>
            </div>

            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Quantity<span class="text-red-500">*</span>
              </label>
              <input v-model="product.quantity" type="number"
                class="itbms-quantity p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="quantityError" class="itbms-message text-red-500 text-sm mt-1">{{ quantityError }}</p>
            </div>

            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                RAM (GB)
              </label>
              <input v-model="product.ramGb" type="number"
                class="itbms-ramGb p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="ramError" class="itbms-message text-red-500 text-sm mt-1">{{ ramError }}</p>
            </div>
            
            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Storage (GB)
              </label>
              <input v-model="product.storageGb" type="number"
                class="itbms-storageGb p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="storageError" class="itbms-message text-red-500 text-sm mt-1">{{ storageError }}</p>
            </div>

            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Screen Size (in)
              </label>
              <input v-model="product.screenSizeInch" type="number"
                class="itbms-screenSizeInch p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="screenSizeError" class="itbms-message text-red-500 text-sm mt-1">{{ screenSizeError }}</p>
            </div>

            <div class="col-span-1">
              <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
                Color
              </label>
              <input v-model="product.color" type="text"
                class="itbms-color p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
                :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'" />
              <p v-if="colorError" class="itbms-message text-red-500 text-sm mt-1">{{ colorError }}</p>
            </div>
          </div>
          
          <div class="col-span-full">
            <label class="block font-semibold mb-1" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-500'">
              Description<span class="text-red-500">*</span>
            </label>
            <textarea v-model="product.description" rows="4"
              class="itbms-description p-2 rounded-lg w-full border transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-orange-500"
              :class="theme === 'dark' ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-100 border-gray-300'"></textarea>
            <p v-if="descriptionError" class="itbms-message text-red-500 text-sm mt-1">{{ descriptionError }}</p>
          </div>

          <div class="flex flex-col sm:flex-row gap-4 pt-4">
            <button @click="submitForm" :disabled="!isValid() || (isEditMode && !isModified)" :class="[
              'itbms-save-button w-full font-semibold border-2 rounded-xl px-6 py-3 transition-all duration-300 transform active:scale-95 shadow-md',
              (isValid() && (!isEditMode || isModified))
                ? 'bg-green-500 text-white border-green-500 hover:bg-green-600 hover:cursor-pointer'
                : 'bg-gray-300 text-gray-500 border-gray-300 cursor-not-allowed'
            ]">
              Save
            </button>
            <router-link :to="isEditMode ? `/sale-items/${product.id}` : '/sale-items'" class="w-full">
              <button
                class="itbms-cancel-button w-full font-semibold border-2 rounded-xl px-6 py-3 transition-all duration-300 transform active:scale-95 shadow-md hover:cursor-pointer"
                :class="theme === 'dark'
                  ? 'bg-red-500 text-white border-red-500 hover:bg-red-600'
                  : 'bg-red-500 text-white border-red-500 hover:bg-red-600'">
                Cancel
              </button>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <transition name="bounce-popup">
      <div v-if="showConfirmationAddPopup" class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
          <h2 class="text-2xl font-bold mb-4">Confirm adding the product</h2>
          <p class="itbms-message mb-6 text-lg">Do you want to add this product?</p>
          <div class="flex justify-center gap-4">
            <button @click="confirmAddItem"
              class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
            <button @click="cancelAddItem"
              class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">No</button>
          </div>
        </div>
      </div>
    </transition>
    <transition name="bounce-popup">
      <div v-if="showConfirmationEditPopup" class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
        <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
          <h2 class="text-2xl font-bold mb-4">Confirm editing the product</h2>
          <p class="itbms-message mb-6 text-lg">Do you want to save changes to this product?</p>
          <div class="flex justify-center gap-4">
            <button @click="confirmAddItem"
              class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
            <button @click="cancelAddItem"
              class="itbms-cancel-button bg-gray-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-gray-600 active:scale-95 hover:cursor-pointer">No</button>
          </div>
        </div>
      </div>
    </transition>
    <transition name="fade-background">
      <div v-if="isLoading" class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50 loading-overlay">
        <div class="p-8 rounded-2xl shadow-xl text-center transition-colors duration-500 transform scale-110"
          :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
          <svg class="animate-spin h-8 w-8 text-orange-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg"
            fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor"
              d="M4 12a8 8 0 018-8v4l3.5-3.5L12 0v4a8 8 0 100 16v-4l-3.5 3.5L12 24v-4a8 8 0 01-8-8z" />
          </svg>
          <p class="itbms-message text-lg font-medium">Saving product...</p>
        </div>
      </div>
    </transition>
    <transition name="bounce-popup">
      <div v-if="showNotFoundPopup" class="itbms-bg fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
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

    <button @click="toggleTheme"
      class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-md shadow-lg transition-all duration-300 z-50 hover:shadow-2xl hover:cursor-pointer"
      :class="theme === 'dark' ? 'bg-gray-700/80 text-white' : 'bg-gray-200/80 text-black'">
      <svg v-if="theme === 'dark'" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
      </svg>
    </button>
  </div>
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
</style>