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
    
    async function addItem(url, newItem) {
      try {
        const res = await fetch(url, {
          method: 'POST',
          headers: {
            'content-type': 'application/json'
          },
          body: JSON.stringify({
            ...newItem
          })
        })
        const addedItem = await res.json()
        return addedItem
      } catch (error) {
        throw new Error('can not add your item')
      }
    }
    
    async function editItem(url, id, editItem) {
      try {
        const res = await fetch(`${url}/${id}`, {
          method: 'PUT',
          headers: {
            'content-type': 'application/json'
          },
          body: JSON.stringify({
            ...editItem
          })
        })
        const editedItem = await res.json()
        return editedItem
      } catch (error) {
        throw new Error('can not edit your item')
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
    
    export { getItems, getItemById, deleteItemById, addItem, editItem, patchItem }
