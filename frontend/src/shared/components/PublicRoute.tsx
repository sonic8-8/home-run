import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';

// 이미 로그인한 사용자가 /login 접속 시 → 홈으로 보냄
// 비로그인 사용자 → 그대로 통과
export const PublicRoute = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  if (isAuthenticated) {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <Outlet />;
};