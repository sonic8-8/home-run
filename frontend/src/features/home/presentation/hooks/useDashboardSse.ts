import { useEffect, useCallback } from 'react';
import { connectSse } from '@core/network/sseClient';
import type { Dashboard } from '../../domain/entities/Dashboard';

export function useDashboardSse(
  isAssetLinked: boolean | null,
  onUpdate: (dashboard: Dashboard) => void,
) {
  const handleUpdate = useCallback(
    (dashboard: Dashboard) => onUpdate(dashboard),
    [onUpdate],
  );

  useEffect(() => {
    if (!isAssetLinked) return;

    const disconnect = connectSse(
      '/api/home/dashboard/subscribe',
      (event) => {
        if (event.name === 'dashboard-update') {
          try {
            const data = JSON.parse(event.data) as Dashboard;
            handleUpdate(data);
          } catch {
            // 파싱 실패 무시
          }
        }
      },
    );

    return disconnect;
  }, [isAssetLinked, handleUpdate]);
}
