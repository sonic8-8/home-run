import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ROUTES } from './routes';
import { PrivateRoute } from '@shared/components/PrivateRoute';
import { PublicRoute } from '@shared/components/PublicRoute';

const AuthPage = lazy(() =>
  import('@features/auth').then((m) => ({ default: m.AuthPage }))
);

const TempPage = () => <div>준비 중</div>;

const router = createBrowserRouter([
  {
    element: <PublicRoute />,
    children: [
      { path: ROUTES.LOGIN, element: <Suspense fallback={null}><AuthPage /></Suspense> },
    ],
  },
  {
    element: <PrivateRoute />,
    children: [
      { path: ROUTES.HOME,     element: <TempPage /> },
      { path: ROUTES.GAME,     element: <TempPage /> },
      { path: ROUTES.LOAN,     element: <TempPage /> },
      { path: ROUTES.PROPERTY, element: <TempPage /> },
      { path: ROUTES.CARD,     element: <TempPage /> },
      { path: ROUTES.MY_PAGE,  element: <TempPage /> },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
