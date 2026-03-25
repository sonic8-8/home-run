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
  const [fetchError, setFetchError] = useState(false);
  const accessToken = useAuthStore((s) => s.accessToken);

  useEffect(() => {
    if (!src || isAbsoluteUrl(src)) return;

    let cancelled = false;
    let objectUrl: string | null = null;

    fetch(`${BASE_URL}/api/v1/images?objectName=${encodeURIComponent(src)}`, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    })
      .then((res) => {
        if (!res.ok) throw new Error(`${res.status}`);
        return res.blob();
      })
      .then((blob) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(blob);
        setBlobUrl(objectUrl);
        setFetchError(false);
      })
      .catch(() => {
        if (!cancelled) setFetchError(true);
      });

    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [src, accessToken]);

  if (!src) return <>{fallback ?? null}</>;
  if (isAbsoluteUrl(src)) return <img src={src} alt={alt} className={className} />;
  if (fetchError) return <>{fallback ?? null}</>;
  if (!blobUrl) return null;

  return <img src={blobUrl} alt={alt} className={className} />;
}
