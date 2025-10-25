import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode'; // ✅ ใช้ import แบบนี้ให้ build ผ่านได้

async function resetPasswordForgot(url, token, body) {
  try {
    const options = {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`   // ✅ ใส่ reset token
      },
      body: JSON.stringify(body)
    };

    const res = await fetch(url, options);
    const text = await res.text();
    const data = text ? JSON.parse(text) : null;
    return { status: res.status, data };
  } catch (error) {
    throw new Error('Cannot reset password: ' + error.message);
  }
}


// ✅ ฟังก์ชันตรวจสอบว่า access token หมดอายุหรือยัง
function isTokenExpired(token) {
  try {
    const decoded = jwtDecode(token);
    const now = Date.now() / 1000; // เวลาในหน่วยวินาที
    return decoded.exp < now;
  } catch (err) {
    return true; // ถ้า decode ไม่ได้ ให้ถือว่าหมดอายุ
  }
}

async function refreshAccessToken() {
  const accessToken = Cookies.get('access_token'); // ใช้ access_token เดิมที่หมดอายุ
  console.log("Refreshing token...");
  if (!accessToken) return null;

   try {
    const res = await fetch('https://intproj24.sit.kmutt.ac.th/sy4/itb-mshop/v2/auth/refresh', {
      method: 'POST',
      credentials: 'include', // ✅ ส่งคุกกี้ (refresh_token) ไปกับ request
    });

    if (!res.ok) {
      console.error("Failed to refresh token:", res.status);

      return null;
    }

    const data = await res.json();
    // เก็บ access_token ใหม่ที่ได้กลับมา
    Cookies.set('access_token', data.access_token);
    return data.access_token;
  } catch (err) {
    console.error("Error refreshing token:", err);
    return null;
  }
}

//
// ✅ ฟังก์ชัน addItemWithAuth — ใช้โครงสร้างเดียวกัน (refresh token อัตโนมัติ)
//
async function addItemWithAuth(url, newItem, isMultipart = false) {
  try {
    let token = Cookies.get('access_token');
    console.log('Token exp:', jwtDecode(token).exp, 'Now:', Date.now() / 1000);
    // ถ้า access token หมดอายุ → refresh ก่อน
    if (!token || isTokenExpired(token)) {
      console.warn("Access token expired, refreshing...");
      token = await refreshAccessToken();
      if (!token) throw new Error("Cannot refresh token");
    }

    const options = {
      method: 'POST',
      headers: {},
    };

    // แนบ Authorization header
    if (token) {
      options.headers['Authorization'] = `Bearer ${token}`;
    }

    // ส่งเป็น JSON หรือ FormData
    if (isMultipart) {
      options.body = newItem; // newItem ต้องเป็น FormData
    } else {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(newItem);
    }

    // ยิง request
    let res = await fetch(url, options);

    // ถ้าเจอ 401 → refresh แล้วลองอีกครั้ง
    if (res.status === 401) {
      token = await refreshAccessToken();
      if (!token) throw new Error("Cannot refresh token");

      options.headers['Authorization'] = `Bearer ${token}`;
      res = await fetch(url, options);
    }

    const data = res.status !== 204 ? await res.json() : null;
    return { status: res.status, data };
  } catch (error) {
    throw new Error('Cannot add your item: ' + error.message);
  }
}


async function editItemWithAuth(url, id, updatedItem, token, isMultipart = false) {
  try {
    let token = Cookies.get('access_token');
    console.log('Token exp:', jwtDecode(token).exp, 'Now:', Date.now() / 1000);
    // ถ้า access token หมดอายุ → refresh ก่อน
    if (!token || isTokenExpired(token)) {
      console.warn("Access token expired, refreshing...");
      token = await refreshAccessToken();
      if (!token) throw new Error("Cannot refresh token");
    }

    const options = {
      method: 'PUT',
      headers: {}
    }

    // ถ้ามี token ให้ใส่ลงใน header
    if (token) {
      options.headers['Authorization'] = `Bearer ${token}`
    }

    if (isMultipart) {
      // updatedItem เป็น FormData
      options.body = updatedItem
    } else {
      options.headers['Content-Type'] = 'application/json'
      options.body = JSON.stringify(updatedItem)
    }

    const res = await fetch(`${url}/${id}`, options)
const text = await res.text();
    const data = text ? JSON.parse(text) : null;
    return { status: res.status, data }
  } catch (error) {
    throw new Error('Cannot edit your item: ' + error.message)
  }
}



async function getItemByIdWithAuth(baseUrl, id, token) {
  try {
    let token = Cookies.get('access_token');
    console.log('Token exp:', jwtDecode(token).exp, 'Now:', Date.now() / 1000);
    // ถ้า access token หมดอายุ → refresh ก่อน
    if (!token || isTokenExpired(token)) {
      console.warn("Access token expired, refreshing...");
      token = await refreshAccessToken();
      if (!token) throw new Error("Cannot refresh token");
    }

    const response = await fetch(`${baseUrl}/${id}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    return response; // ส่ง response object กลับไปตรง ๆ
  } catch (error) {
    console.error('Fetch error:', error);
    return undefined;
  }
}

// ฟังก์ชัน fetch แบบรองรับ Authorization token
async function getItemsWithAuth(url, options = {}) {
  try {
    let accesstoken = Cookies.get('access_token');
    console.log('Token exp:', jwtDecode(accesstoken).exp, 'Now:', Date.now() / 1000);
    // ถ้า access token หมดอายุ → refresh ก่อน
    if (!accesstoken || isTokenExpired(accesstoken)) {
      console.warn("Access token expired, refreshing...");
      accesstoken = await refreshAccessToken();
      if (!accesstoken) throw new Error("Cannot refresh token");
    }

    const { params, token } = options;

    // ประกอบ query string
    const query = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          value.forEach(v => v !== null && query.append(key, v));
        } else if (value !== undefined && value !== null) {
          query.append(key, value);
        }
      });
    }

    const fullUrl = `${url}?${query.toString()}`;

    const data = await fetch(fullUrl, {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
      },
    });

    if (!data.ok) {
      const errText = await data.text();
      throw new Error(`HTTP error ${data.status}: ${errText}`);
    }

    return await data.json();
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
}


async function getItems(url, options = {}) {
  try {
    const { params } = options;

    // ประกอบ query string
    const query = new URLSearchParams();

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          value.forEach(v => query.append(key, v))
        } else if (value !== undefined && value !== null) {
          query.append(key, value)
        }
      });
    }

    const fullUrl = `${url}?${query.toString()}`

    const data = await fetch(fullUrl);

    if (!data.ok) {
      throw new Error(`HTTP error`);
    }

    const items = await data.json();
    return items;
  } catch (error) {
    throw new Error(`HTTP error`);
  }
}


async function getItemById(url, id) {
  try {
    //console.log(`🔍 Fetching from: ${url}/${id}`);

    const data = await fetch(`${url}/${id}`)

    if (!data.ok) {
      if (data.status === 404) return undefined;
      throw new Error(`HTTP error`);
    }

    const item = await data.json()
    return item
  } catch (error) {
    console.error("Error fetching item:", error);
    return undefined;
  }
}

async function deleteItemById(url, id) {
  try {
    const res = await fetch(`${url}/${id}`, {
      method: 'DELETE'
    })
    return res.status
  } catch (error) {
    throw new Error('can not delete your item')
  }
}

async function addItem(url, newItem, isMultipart = false) {
  try {
    const options = { method: 'POST' }

    if (isMultipart) {
      options.body = newItem
    } else {
      options.headers = { 'Content-Type': 'application/json' }
      options.body = JSON.stringify(newItem)
    }

    const res = await fetch(url, options)

    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)

    // ตรวจสอบว่า response มี content-length > 0
    const text = await res.text()
    const data = text ? JSON.parse(text) : null

    return { status: res.status, data }

  } catch (error) {
    throw new Error('Cannot add your item: ' + error.message)
  }
}


async function editItem(url, id, updatedItem, isMultipart = false) {
  try {
    const options = {
      method: 'PUT',
      headers: {}
    }

    if (isMultipart) {
      // updatedItem เป็น FormData
      options.body = updatedItem
    } else {
      options.headers = { 'Content-Type': 'application/json' }
      options.body = JSON.stringify(updatedItem)
    }

    const res = await fetch(`${url}/${id}`, options)
    const data = res.status !== 204 ? await res.json() : null
    return { status: res.status, data }
  } catch (error) {
    throw new Error('Cannot edit your item: ' + error.message)
  }
}

async function patchItem(url, id, partialItem) {
  try {
    const res = await fetch(`${url}/${id}`, {
      method: 'PATCH',
      headers: {
        'content-type': 'application/json'
      },
      body: JSON.stringify({
        ...partialItem
      })
    });

    if (!res.ok) {
      throw new Error('Failed to patch the item');
    }

    const patchedItem = await res.json();
    return patchedItem;
  } catch (error) {
    throw new Error('can not patch your item');
  }
}

export { resetPasswordForgot, getItems, getItemById, deleteItemById, addItem, editItem, patchItem,getItemByIdWithAuth,getItemsWithAuth, addItemWithAuth, editItemWithAuth}
