import { useEffect, useLayoutEffect, useRef } from 'react';
import { connectSse } from '@core/network/sseClient';
import type { Dashboard } from '../../domain/entities/Dashboard';

export function useDashboardSse(
  isAssetLinked: boolean | null,
  onUpdate: (dashboard: Dashboard) => void,
) {
  const onUpdateRef = useRef(onUpdate);
  useLayoutEffect(() => {
    onUpdateRef.current = onUpdate;
  }, [onUpdate]);

  useEffect(() => {
    if (!isAssetLinked) return;

    const disconnect = connectSse(
      '/home/dashboard/subscribe',
      (event) => {
        if (event.name === 'dashboard-update') {
          try {
            const data = JSON.parse(event.data) as Dashboard;
            onUpdateRef.current(data);
          } catch {
            // 파싱 실패 무시
          }
        }
      },
    );

    return disconnect;
  }, [isAssetLinked]);
}
