import { authHeader } from '../auth/tokenStorage';

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

export async function searchProducts(query) {
  const res = await fetch(`${API_BASE}/api/v1/search`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify({ query }),
  });
  if (res.status === 429) throw new Error('Too many searches — please wait a moment and try again.');
  if (!res.ok) throw new Error(`Search failed: ${res.status}`);
  return res.json();
}
