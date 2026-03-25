import { useState, useEffect } from 'react';
import { useAuthStore } from '@core/store/authStore';

interface AuthImageProps {
  src: string | null | undefined;
  alt: string;
  fallback?: React.ReactNode;
  className?: string;
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

function isAbsoluteUrl(url: string) {
  return url.startsWith('http://') || url.startsWith('https://');
}

/**
 * cardImageUrl이 파일명인 경우 /api/v1/images?objectName=... 으로 fetch.
 * 절대 URL이면 직접 사용. OCI 비활성화 시 fallback 표시.
 */
export function AuthImage({ src, alt, fallback, className }: AuthImageProps) {
  const [blobUrl, setBlobUrl] = useState<string | null>(null);
  const [error, setError] = useState(false);
  const accessToken = useAuthStore((s) => s.accessToken);

  useEffect(() => {
    if (!src) {
      setError(true);
      return;
    }

    if (isAbsoluteUrl(src)) {
      setBlobUrl(src);
      return;
    }

    // 파일명 → /api/v1/images 로 fetch
    let objectUrl: string | null = null;
    setError(false);
    setBlobUrl(null);

    fetch(`${BASE_URL}/api/v1/images?objectName=${encodeURIComponent(src)}`, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    })
      .then((res) => {
        if (!res.ok) throw new Error(`${res.status}`);
        return res.blob();
      })
      .then((blob) => {
        objectUrl = URL.createObjectURL(blob);
        setBlobUrl(objectUrl);
      })
      .catch(() => setError(true));

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [src, accessToken]);

  if (error || !src) return <>{fallback ?? null}</>;
  if (!blobUrl) return null;

  return <img src={blobUrl} alt={alt} className={className} />;
}
