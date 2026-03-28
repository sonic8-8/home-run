function getSessionStorage(): Storage | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return window.sessionStorage;
}

export function readSessionStorage(key: string | null): string | null {
  if (key === null) {
    return null;
  }

  return getSessionStorage()?.getItem(key) ?? null;
}

export function writeSessionStorage(key: string | null, value: string | null): void {
  if (key === null || value === null) {
    return;
  }

  getSessionStorage()?.setItem(key, value);
}

export function removeSessionStorage(key: string | null): void {
  if (key === null) {
    return;
  }

  getSessionStorage()?.removeItem(key);
}
