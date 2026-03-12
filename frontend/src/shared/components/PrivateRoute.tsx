import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';

// 로그인 안 한 사용자 → /login 으로 보냄
// 로그인 한 사용자  → 그대로 통과
export const PrivateRoute = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    // 로그인 후 원래 가려던 페이지로 돌아올 수 있게 현재 경로 저장
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};