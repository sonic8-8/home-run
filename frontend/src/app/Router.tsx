import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ROUTES } from './routes';
import { PrivateRoute } from '@shared/components/PrivateRoute';
import { PublicRoute } from '@shared/components/PublicRoute';

const AuthPage = lazy(() =>
  import('@features/auth').then((m) => ({ default: m.AuthPage }))
);

const HomePage = lazy(() =>
  import('@features/home').then((m) => ({ default: m.HomePage }))
);

const GameStartPage = lazy(() =>
  import('@features/game/presentation/pages/GameStart')
);

const GameSaveSlotPage = lazy(() =>
  import('@features/game/presentation/pages/GameSaveSlot/GameSaveSlot')
);

const SelectCharacterPage = lazy(() =>
  import('@features/game/presentation/pages/SelectCharacter/SelectCharacter')
);

const SetNicknamePage = lazy(() =>
  import('@features/game/presentation/pages/SetNickname/SetNickname')
);

const SelectStartMethodPage = lazy(() =>
  import('@features/game/presentation/pages/SelectStartMethod/SelectStartMethod')
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
      { path: ROUTES.HOME,     element: <Suspense fallback={null}><HomePage /></Suspense> },
      { path: ROUTES.GAME,       element: <TempPage /> },
      { path: ROUTES.GAME_START, element: <Suspense fallback={null}><GameStartPage /></Suspense> },
      { path: ROUTES.GAME_SAVE,  element: <Suspense fallback={null}><GameSaveSlotPage /></Suspense> },
      { path: ROUTES.GAME_SELECT_CHARACTER, element: <Suspense fallback={null}><SelectCharacterPage /></Suspense> },
      { path: ROUTES.GAME_SET_NICKNAME, element: <Suspense fallback={null}><SetNicknamePage /></Suspense> },
      { path: ROUTES.GAME_SELECT_START_METHOD, element: <Suspense fallback={null}><SelectStartMethodPage /></Suspense> },
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
