// utils/storage.ts
export const safeLocalStorage = {
  get(key) {
    try {
      return localStorage.getItem(key);
    } catch (e) {
      console.error('LocalStorage access denied', e);
      return null;
    }
  },
  set(key, value) {
    try {
      localStorage.setItem(key, value);
    } catch (e) {
      console.error('LocalStorage set failed', e);
    }
  },
  remove(key) {
    localStorage.removeItem(key);
  },
};
