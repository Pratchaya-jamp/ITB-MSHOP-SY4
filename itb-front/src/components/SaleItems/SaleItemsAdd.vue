<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  editItem,
  getItems,
  getItemById,
  addItemWithAuth,
} from "@/libs/fetchUtilsOur";
import Cookies from "js-cookie";
import { theme } from "@/stores/themeStore.js";

const router = useRouter();
const route = useRoute();
const id = route.params.id;

// --- State ---
const product = ref({
  id: "",
  brandName: "",
  model: "",
  price: "",
  description: "",
  ramGb: "",
  screenSizeInch: "",
  storageGb: "",
  color: "",
  quantity: "",
});
const pictures = ref([]);
const warningMessage = ref("");
const placeholder = "/sy4/phone/iPhone.png";
const responseMessage = ref("");
const originalProduct = ref(null);
const originalPictures = ref([]);
const selectedBrandId = ref(null);
const showConfirmationAddPopup = ref(false);
const showConfirmationEditPopup = ref(false);
const isLoading = ref(false);
const brandList = ref([]);
const isEditMode = ref(!!id);
const countdown = ref(3);
const showNotFoundPopup = ref(false);

// Validation Flags
const isPriceValid = ref(false);
const isModelValid = ref(false);
const isDescriptionValid = ref(false);
const isRamValid = ref(true);
const isScreenValid = ref(true);
const isStorageValid = ref(true);
const isColorValid = ref(true);
const isQuantityValid = ref(true);
const isBrandSelected = ref(false);

// Validation Error Messages
const modelError = ref("");
const priceError = ref("");
const descriptionError = ref("");
const ramError = ref("");
const screenSizeError = ref("");
const storageError = ref("");
const colorError = ref("");
const quantityError = ref("");
const brandError = ref("");

// --- Theming---
const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

// --- Image Handling ---
const mainImage = computed(() => {
    const firstVisibleImage = pictures.value.find(p => p.status !== 'remove');
    return firstVisibleImage ? (firstVisibleImage.previewUrl || firstVisibleImage.url) : placeholder;
});

function handleFileUpload(event) {
  const input = event.target;
  if (!input.files) return;
  const selectedFiles = Array.from(input.files);
  const allowedExtensions = ["jpg", "jpeg", "png", "webp"];
  warningMessage.value = "";

  for (const file of selectedFiles) {
    const fileExtension = file.name.split(".").pop().toLowerCase();
    if (!allowedExtensions.includes(fileExtension)) {
      warningMessage.value = `File type "${fileExtension}" is not allowed.`;
      continue;
    }
    if (file.size > 2 * 1024 * 1024) {
      warningMessage.value = `File "${file.name}" exceeds 2MB limit.`;
      continue;
    }
    if (pictures.value.filter(p => p.status !== 'remove').length >= 4) {
      warningMessage.value = "Maximum 4 pictures are allowed.";
      break;
    }
    pictures.value.push({
      name: file.name,
      file,
      previewUrl: URL.createObjectURL(file),
      status: "new",
    });
  }
  input.value = "";
}

function moveUp(index) {
  if (index === 0) return;
  const visiblePics = visiblePictures.value;
  const itemToMove = visiblePics[index];
  const itemToSwapWith = visiblePics[index - 1];
  
  const actualIndexToMove = pictures.value.findIndex(p => p === itemToMove);
  const actualIndexToSwapWith = pictures.value.findIndex(p => p === itemToSwapWith);

  [pictures.value[actualIndexToMove], pictures.value[actualIndexToSwapWith]] = 
  [pictures.value[actualIndexToSwapWith], pictures.value[actualIndexToMove]];

  pictures.value.forEach(pic => {
    if (pic.status === 'keep' || pic.status === 'move') pic.status = 'move';
  });
}

function moveDown(index) {
  const visiblePics = visiblePictures.value;
  if (index >= visiblePics.length - 1) return;
  const itemToMove = visiblePics[index];
  const itemToSwapWith = visiblePics[index + 1];

  const actualIndexToMove = pictures.value.findIndex(p => p === itemToMove);
  const actualIndexToSwapWith = pictures.value.findIndex(p => p === itemToSwapWith);
  
  [pictures.value[actualIndexToMove], pictures.value[actualIndexToSwapWith]] = 
  [pictures.value[actualIndexToSwapWith], pictures.value[actualIndexToMove]];

  pictures.value.forEach(pic => {
    if (pic.status === 'keep' || pic.status === 'move') pic.status = 'move';
  });
}


function removePicture(index) {
    const visiblePics = visiblePictures.value;
    const pictureToRemove = visiblePics[index];
    const actualIndex = pictures.value.findIndex(p => p === pictureToRemove);

    if (pictures.value[actualIndex].status === 'new') {
        URL.revokeObjectURL(pictures.value[actualIndex].previewUrl);
        pictures.value.splice(actualIndex, 1);
    } else {
        pictures.value[actualIndex].status = 'remove';
    }
}

const visiblePictures = computed(() => pictures.value.filter(p => p.status !== 'remove'));

const getImageUrl = (item) => {
  return item.previewUrl || item.url;
};

onMounted(async () => {
  try {
    const data = await getItems(`${import.meta.env.VITE_BACKEND}/v1/brands`);
    brandList.value = data.sort((a, b) => a.brandName.localeCompare(b.brandName));
  } catch (err) {
    console.error("Error loading brands:", err);
  }

  if (id) {
    isEditMode.value = true;
    const data = await getItemById(
      `${import.meta.env.VITE_BACKEND}/v2/sale-items`,
      id
    );
    if (data && data.id) {
      const formattedProduct = {
        id: data.id, brandName: data.brandName, model: data.model,
        price: data.price, description: data.description, ramGb: data.ramGb,
        screenSizeInch: data.screenSizeInch, storageGb: data.storageGb,
        color: data.color, quantity: data.quantity,
      };
      product.value = { ...formattedProduct };
      originalProduct.value = { ...formattedProduct };

      const selectedBrand = brandList.value.find(
        (brand) => brand.brandName === data.brandName
      );
      if (selectedBrand) {
        selectedBrandId.value = selectedBrand.id;
      }

      if (data.saleItemImages && data.saleItemImages.length > 0) {
        originalPictures.value = data.saleItemImages
            .sort((a, b) => a.imageViewOrder - b.imageViewOrder)
            .map(img => ({
                id: img.id,
                fileName: img.fileName,
                order: img.imageViewOrder
            }));
        
        pictures.value = originalPictures.value.map(img => ({
            id: img.id,
            name: img.fileName,
            url: `${import.meta.env.VITE_BACKEND}/v2/sale-items/images/${img.fileName}`,
            order: img.order,
            status: 'keep',
            file: null
        }));
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
});

// --- Watchers for Validation ---
watch(() => product.value.price, (newVal) => {
    const val = Number(newVal);
    if (newVal === null || newVal === "" || isNaN(val) || !Number.isInteger(val) || val < 0) {
        priceError.value = "Price must be a non-negative integer.";
        isPriceValid.value = false;
    } else if (val > 2147483647) {
        priceError.value = `The price must not exceed 2,147,483,647 Baht.`;
        isPriceValid.value = false;
    } else {
        priceError.value = "";
        isPriceValid.value = true;
    }
});

watch(() => product.value.model, (newVal) => {
    const val = newVal?.trim() ?? "";
    if (!val || val.length > 60) {
        modelError.value = "Model must be 1-60 characters long.";
        isModelValid.value = false;
    } else {
        modelError.value = "";
        isModelValid.value = true;
    }
});

watch(() => product.value.description, (newVal) => {
    const val = newVal?.trim() ?? "";
    if (!val || val.length > 16384) {
        descriptionError.value = "Description must be 1-16,384 characters long.";
        isDescriptionValid.value = false;
    } else {
        descriptionError.value = "";
        isDescriptionValid.value = true;
    }
});

watch(() => product.value.quantity, (newVal) => {
    const val = Number(newVal);
    if (newVal === null || newVal === "" || isNaN(val) || !Number.isInteger(val) || val < 0) {
        quantityError.value = "Quantity must be a non-negative integer.";
        isQuantityValid.value = false;
    } else if (val > 2147483647) {
        quantityError.value = "The quantity must not exceed 2,147,483,647.";
        isQuantityValid.value = false;
    } else {
        quantityError.value = "";
        isQuantityValid.value = true;
    }
});

watch(selectedBrandId, (newVal) => {
    isBrandSelected.value = !!newVal;
    brandError.value = newVal ? "" : "Brand must be selected.";
});


const isModified = computed(() => {
  if (!originalProduct.value) return true;
  const productChanged = Object.keys(product.value).some((key) => {
    const currentValue = String(product.value[key] || "").trim();
    const originalValue = String(originalProduct.value[key] || "").trim();
    return currentValue !== originalValue;
  });
  const originalBrandId = brandList.value.find(
    (brand) => brand.brandName === originalProduct.value.brandName
  )?.id;
  const brandChanged = selectedBrandId.value !== originalBrandId;
  const picturesChanged =
    JSON.stringify(pictures.value.map(p => ({ name: p.name, status: p.status }))) !==
    JSON.stringify(originalPictures.value.map(p => ({ name: p.fileName, status: 'keep' })));
  return productChanged || brandChanged || picturesChanged;
});

const isFormValid = computed(() => {
  return (
    isBrandSelected.value && isPriceValid.value && isModelValid.value &&
    isDescriptionValid.value && isRamValid.value && isScreenValid.value &&
    isStorageValid.value && isColorValid.value && isQuantityValid.value
  );
});

const submitForm = async () => {
  if (!selectedBrandId.value) {
    brandError.value = 'Brand must be selected.';
  }
  if (!isFormValid.value) {
    responseMessage.value = "Please complete the form correctly.";
    return;
  }
  if (isEditMode.value) {
    showConfirmationEditPopup.value = true;
  } else {
    showConfirmationAddPopup.value = true;
  }
};

const confirmAddItem = async () => {
    showConfirmationAddPopup.value = false
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
    formData.append('saleItem', new Blob([JSON.stringify(saleItem)], { type: 'application/json' }))
    pictures.value.forEach((picture) => {
        if (picture.file) {
           formData.append('pictures', picture.file, picture.name)
        }
    })
    const token = Cookies.get('access_token');
    try {
        const result = await addItemWithAuth(
            `${import.meta.env.VITE_BACKEND}/v2/sale-items`,
            formData,
            true,
            token,
        )
        if (result.status !== 201 || !result.data?.id) {
            throw new Error('Add failed or invalid data returned')
        }
        setTimeout(() => {
            isLoading.value = false
            router.push({ path: '/sale-items/list', query: { addSuccess: 'true' } })
        }, 500)
    } catch (err) {
        console.error(err)
        responseMessage.value = 'เกิดข้อผิดพลาดในการเพิ่มสินค้า'
        isLoading.value = false
        router.push({ path: '/sale-items/list', query: { addFail: 'true' } })
    }
}


const confirmEditItem = async () => {
  isLoading.value = true;
  showConfirmationEditPopup.value = false;

  const selectedBrand = brandList.value.find(b => b.id === selectedBrandId.value)
  const formData = new FormData()

  // --- saleItem fields ---
  if (selectedBrand) formData.append('saleItem.brand.id', selectedBrand.id)
  formData.append('saleItem.model', product.value.model || '')
  formData.append('saleItem.description', product.value.description || '')
  formData.append('saleItem.price', product.value.price?.toString() || '0')
  formData.append('saleItem.quantity', product.value.quantity?.toString() || '0')
  if (product.value.ramGb) formData.append('saleItem.ramGb', product.value.ramGb.toString())
  if (product.value.screenSizeInch) formData.append('saleItem.screenSizeInch', product.value.screenSizeInch.toString())
  if (product.value.storageGb) formData.append('saleItem.storageGb', product.value.storageGb.toString())
  formData.append('saleItem.color', product.value.color || '')

  // --- imageInfos ---
  pictures.value.forEach((pic, index) => {
    formData.append(`imageInfos[${index}].order`, (index + 1).toString())
    formData.append(`imageInfos[${index}].fileName`, pic.name || `image-${index}`)
    formData.append(`imageInfos[${index}].status`, pic.status)
    if (pic.file) {
      formData.append(`imageInfos[${index}].imageFile`, pic.file, pic.file.name)
    }
  })

  try {
    const result = await editItem(
      `${import.meta.env.VITE_BACKEND}/v2/sale-items`,
      route.params.id,
      formData,
      true
    )
    if (result.status !== 200 || !result.data?.id) throw new Error('Edit failed')
    setTimeout(async () => {
        isLoading.value = false
        await router.push({ path: `/sale-items/${route.params.id}`, query: { editSuccess: 'true' } })
    }, 500);

  } catch (err) {
    console.error(err)
    responseMessage.value = 'เกิดข้อผิดพลาดในการแก้ไขสินค้า'
    isLoading.value = false
  }
}

const cancelAddItem = () => {
  showConfirmationAddPopup.value = false;
  showConfirmationEditPopup.value = false;
};
</script>


<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12">
      <h1
        class="text-4xl font-extrabold tracking-tight mb-8" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'"
      >
        {{ isEditMode ? 'Edit Sale Item' : 'Add New Sale Item' }}
      </h1>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
        <div class="lg:col-span-1 space-y-6">
          <div>
            <h2 class="text-xl font-bold mb-4">Product Images</h2>
            <div
              class="relative w-full aspect-square rounded-2xl overflow-hidden shadow-lg mb-4"
              :class="
                theme === 'dark' ? 'bg-gray-800/50' : 'bg-slate-100'
              "
            >
              <img
                :src="mainImage"
                alt="Main Product Image"
                class="w-full h-full object-contain"
                @error="e => e.target.src = placeholder"
              />
            </div>
            <p v-if="warningMessage" class="text-red-500 text-sm w-full text-center">
              {{ warningMessage }}
            </p>
          </div>
          
          <div class="space-y-3">
              <h3 class="text-lg font-semibold">Image Order</h3>
              <div
                  v-for="(file, index) in visiblePictures"
                  :key="file.name || index"
                  class="flex items-center gap-4 p-2 rounded-lg transition-colors"
                  :class="[
                      index === 0 ? 'bg-indigo-500/10 dark:bg-indigo-500/20' : '',
                      theme === 'dark' ? 'hover:bg-gray-700/50' : 'hover:bg-slate-100'
                  ]"
              >
                  <span class="font-mono text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">#{{ index + 1 }}</span>
                  <img :src="getImageUrl(file)" alt="Thumbnail" class="w-12 h-12 object-cover rounded-md flex-shrink-0" />
                  <span class="flex-grow truncate text-sm font-medium">{{ file.name }}</span>
                  
                  <div class="flex items-center gap-1">
                      <button @click="moveUp(index)" :disabled="index === 0" class="p-2 rounded-full disabled:opacity-30 disabled:cursor-not-allowed" :class="theme === 'dark' ? 'hover:bg-gray-600' : 'hover:bg-slate-200'">
                          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" /></svg>
                      </button>
                      <button @click="moveDown(index)" :disabled="index === visiblePictures.length - 1" class="p-2 rounded-full disabled:opacity-30 disabled:cursor-not-allowed" :class="theme === 'dark' ? 'hover:bg-gray-600' : 'hover:bg-slate-200'">
                          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                      </button>
                      <button @click="removePicture(index)" class="p-2 text-red-500 rounded-full" :class="theme === 'dark' ? 'hover:bg-red-500/20' : 'hover:bg-red-500/10'">
                          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                      </button>
                  </div>
              </div>

              <div v-if="visiblePictures.length === 0" class="text-center py-4 text-sm rounded-lg" :class="theme === 'dark' ? 'text-slate-500 bg-gray-800/50' : 'text-slate-500 bg-slate-100'">
                  No images uploaded.
              </div>
          </div>


          <label
            v-if="visiblePictures.length < 4"
            for="file-upload"
            class="flex items-center justify-center w-full p-4 rounded-lg border-2 border-dashed cursor-pointer transition"
            :class="
              theme === 'dark'
                ? 'border-gray-600 hover:border-indigo-500 hover:bg-gray-800'
                : 'border-slate-300 hover:border-indigo-500 hover:bg-slate-100'
            "
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" :class="theme === 'dark' ? 'text-slate-500' : 'text-slate-400'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
            </svg>
            <span class="font-semibold">Upload Images</span>
            <input
              id="file-upload"
              type="file"
              multiple
              accept="image/*"
              class="hidden"
              @change="handleFileUpload"
            />
          </label>
        </div>

        <div class="lg:col-span-2">
          <h2 class="text-xl font-bold mb-4">Product Details</h2>
          <div
            class="p-8 rounded-2xl space-y-6"
            :class="theme === 'dark' ? 'bg-gray-800/50' : 'bg-white shadow-sm'"
          >
            <div class="grid sm:grid-cols-2 gap-6">
              <div>
                <label class="block font-semibold mb-1"
                  >Brand<span class="text-red-500">*</span></label
                >
                <select
                  v-model="selectedBrandId"
                  class="itbms-brand p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                >
                  <option :value="null" disabled>Select a Brand</option>
                  <option
                    v-for="brand in brandList"
                    :key="brand.id"
                    :value="brand.id"
                  >
                    {{ brand.brandName }}
                  </option>
                </select>
                <p v-if="brandError" class="text-red-500 text-xs mt-1">
                  {{ brandError }}
                </p>
              </div>
              <div>
                <label class="block font-semibold mb-1"
                  >Model<span class="text-red-500">*</span></label
                >
                <input
                  v-model="product.model"
                  type="text"
                  class="itbms-model p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="modelError" class="text-red-500 text-xs mt-1">
                  {{ modelError }}
                </p>
              </div>
            </div>

            <div class="grid sm:grid-cols-2 gap-6">
              <div>
                <label class="block font-semibold mb-1"
                  >Price (Baht)<span class="text-red-500">*</span></label
                >
                <input
                  v-model="product.price"
                  type="number"
                  class="itbms-price p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="priceError" class="text-red-500 text-xs mt-1">
                  {{ priceError }}
                </p>
              </div>
              <div>
                <label class="block font-semibold mb-1"
                  >Quantity<span class="text-red-500">*</span></label
                >
                <input
                  v-model="product.quantity"
                  type="number"
                  class="itbms-quantity p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="quantityError" class="text-red-500 text-xs mt-1">
                  {{ quantityError }}
                </p>
              </div>
            </div>

            <div class="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <div>
                <label class="block font-semibold mb-1">RAM (GB)</label>
                <input
                  v-model="product.ramGb"
                  type="number"
                  class="itbms-ramGb p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="ramError" class="text-red-500 text-xs mt-1">
                  {{ ramError }}
                </p>
              </div>
              <div>
                <label class="block font-semibold mb-1">Storage (GB)</label>
                <input
                  v-model="product.storageGb"
                  type="number"
                  class="itbms-storageGb p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="storageError" class="text-red-500 text-xs mt-1">
                  {{ storageError }}
                </p>
              </div>
              <div>
                <label class="block font-semibold mb-1">Screen (Inch)</label>
                <input
                  v-model="product.screenSizeInch"
                  type="number"
                  class="itbms-screenSizeInch p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="screenSizeError" class="text-red-500 text-xs mt-1">
                  {{ screenSizeError }}
                </p>
              </div>
              <div>
                <label class="block font-semibold mb-1">Color</label>
                <input
                  v-model="product.color"
                  type="text"
                  class="itbms-color p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                  :class="
                    theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'
                  "
                />
                <p v-if="colorError" class="text-red-500 text-xs mt-1">
                  {{ colorError }}
                </p>
              </div>
            </div>

            <div>
              <label class="block font-semibold mb-1"
                >Description<span class="text-red-500">*</span></label
              >
              <textarea
                v-model="product.description"
                rows="5"
                class="itbms-description p-3 rounded-lg w-full border-0 outline-none focus:ring-2 focus:ring-indigo-500"
                :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
              ></textarea>
              <p v-if="descriptionError" class="text-red-500 text-xs mt-1">
                {{ descriptionError }}
              </p>
            </div>

            <div
              class="flex flex-col sm:flex-row gap-4 pt-4 border-t"
              :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'"
            >
              <button
                @click="submitForm"
                :disabled="!isFormValid || (isEditMode && !isModified)"
                class="itbms-save-button w-full font-semibold rounded-full px-6 py-3 transition-all"
                :class="
                  !isFormValid || (isEditMode && !isModified)
                    ? 'bg-slate-300 text-slate-500 cursor-not-allowed dark:bg-gray-600 dark:text-gray-400'
                    : 'bg-indigo-600 text-white hover:bg-indigo-700'
                "
              >
                Save Changes
              </button>
              <router-link
                :to="isEditMode ? `/sale-items/${product.id}` : '/sale-items'"
                class="w-full"
              >
                <button
                  class="itbms-cancel-button w-full font-semibold rounded-full px-6 py-3 transition-all"
                  :class="
                    theme === 'dark' ? 'hover:bg-gray-700' : 'hover:bg-slate-100'
                  "
                >
                  Cancel
                </button>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <transition name="bounce-popup">
        <div v-if="showConfirmationAddPopup"
            class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
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
        <div v-if="showConfirmationEditPopup"
            class="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center z-50">
            <div class="rounded-2xl p-8 shadow-xl text-center transition-colors duration-500"
                :class="theme === 'dark' ? 'bg-gray-800 text-white' : 'bg-white text-black'">
                <h2 class="text-2xl font-bold mb-4">Confirm editing the product</h2>
                <p class="itbms-message mb-6 text-lg">Do you want to save changes to this product?</p>
                <div class="flex justify-center gap-4">
                    <button @click="confirmEditItem"
                        class="itbms-confirm-button bg-green-500 text-white font-semibold rounded-lg px-6 py-2 transition-all duration-300 hover:bg-green-600 active:scale-95 hover:cursor-pointer">Yes</button>
                    <button @click="cancelAddItem"
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
                <p class="itbms-message text-lg font-medium">Saving product...</p>
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
  </div>
</template>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.itbms-row {
  opacity: 0;
  animation: fadeInUp 0.5s ease forwards;
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