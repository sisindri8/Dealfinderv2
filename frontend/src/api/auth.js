import { authHeader } from '../auth/tokenStorage';

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

export async function loginWithGoogle(idToken) {
  const res = await fetch(`${API_BASE}/api/v1/auth/google`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken }),
  });
  if (!res.ok) {
    let detail = '';
    try {
      const body = await res.json();
      detail = body?.message ? `: ${body.message}` : '';
    } catch {
      // response wasn't JSON — ignore
    }
    throw new Error(`Google sign-in failed (${res.status})${detail}`);
  }
  return res.json(); // { token, user }
}

export async function fetchMe() {
  const res = await fetch(`${API_BASE}/api/v1/auth/me`, {
    headers: { ...authHeader() },
  });
  if (!res.ok) return null;
  return res.json();
}

export async function getFavorites() {
  const res = await fetch(`${API_BASE}/api/v1/favorites`, {
    headers: { ...authHeader() },
  });
  if (!res.ok) throw new Error(`Failed to load favorites: ${res.status}`);
  return res.json();
}

export async function addFavorite(product) {
  const res = await fetch(`${API_BASE}/api/v1/favorites`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(product),
  });
  if (!res.ok) throw new Error(`Failed to save favorite: ${res.status}`);
  return res.json();
}

export async function removeFavorite(asin) {
  const res = await fetch(`${API_BASE}/api/v1/favorites/${encodeURIComponent(asin)}`, {
    method: 'DELETE',
    headers: { ...authHeader() },
  });
  if (!res.ok) throw new Error(`Failed to remove favorite: ${res.status}`);
}

export async function getHistory() {
  const res = await fetch(`${API_BASE}/api/v1/history`, {
    headers: { ...authHeader() },
  });
  if (!res.ok) throw new Error(`Failed to load history: ${res.status}`);
  return res.json();
}
