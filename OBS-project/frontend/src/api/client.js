import axios from 'axios';

const STORAGE_KEY = 'sms.auth';

export function readAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function writeAuth(value) {
  if (!value) {
    localStorage.removeItem(STORAGE_KEY);
    return;
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
}

export const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

let onUnauthenticated = null;
export function setUnauthenticatedHandler(handler) {
  onUnauthenticated = handler;
}

let refreshing = null;

async function performRefresh() {
  const auth = readAuth();
  if (!auth?.refreshToken) throw new Error('no_refresh_token');
  const { data } = await axios.post('/api/auth/refresh', { refreshToken: auth.refreshToken });
  writeAuth(data);
  return data.accessToken;
}

api.interceptors.request.use((config) => {
  const auth = readAuth();
  if (auth?.accessToken && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const url = original?.url ?? '';

    if (status === 401 && !original._retry && !url.includes('/auth/login') && !url.includes('/auth/refresh')) {
      original._retry = true;
      try {
        refreshing = refreshing ?? performRefresh().finally(() => { refreshing = null; });
        const newToken = await refreshing;
        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      } catch (refreshErr) {
        writeAuth(null);
        if (onUnauthenticated) onUnauthenticated();
        return Promise.reject(refreshErr);
      }
    }

    return Promise.reject(error);
  },
);

export function extractErrorMessage(err, fallback = 'İşlem başarısız') {
  return err?.response?.data?.message || err?.message || fallback;
}
