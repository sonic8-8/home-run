import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ROUTES } from './routes';
import { PageSpinner } from '@shared/components/PageSpinner';
import { PrivateRoute } from '@shared/components/PrivateRoute';
import { PublicRoute } from '@shared/components/PublicRoute';

const AuthPage = lazy(() =>
  import('@features/auth/presentation/pages/AuthPage').then((m) => ({ default: m.AuthPage }))
);

const HomePage = lazy(() =>
  import('@features/home/presentation/pages/HomePage').then((m) => ({ default: m.HomePage }))
);

const GameStartPage = lazy(() =>
  import('@features/game/presentation/pages/GameStart').then((m) => ({ default: m.GameStartPage }))
);

const GameGuidePage = lazy(() =>
  import('@features/game/presentation/pages/GameGuide').then((m) => ({ default: m.GameGuidePage }))
);

const GameSaveSlotPage = lazy(() =>
  import('@features/game/presentation/pages/GameSaveSlot').then((m) => ({ default: m.GameSaveSlotPage }))
);

const SelectCharacterPage = lazy(() =>
  import('@features/game/presentation/pages/SelectCharacter').then((m) => ({ default: m.SelectCharacterPage }))
);

const SetNicknamePage = lazy(() =>
  import('@features/game/presentation/pages/SetNickname').then((m) => ({ default: m.SetNicknamePage }))
);

const SelectStartMethodPage = lazy(() =>
  import('@features/game/presentation/pages/SelectStartMethod').then((m) => ({ default: m.SelectStartMethodPage }))
);

const SelectJobPage = lazy(() =>
  import('@features/game/presentation/pages/SelectJob').then((m) => ({ default: m.SelectJobPage }))
);

const GameMainPage = lazy(() =>
  import('@features/game/presentation/pages/GameMain').then((m) => ({ default: m.GameMainPage }))
);

const RealEstatePage = lazy(() =>
  import('@features/realEstate/presentation/pages/RealEstatePage').then((m) => ({ default: m.RealEstatePage }))
);

const LoanPlaceholderPage = lazy(() =>
  import('@features/loan/presentation/pages/LoanPlaceholderPage').then((m) => ({ default: m.LoanPlaceholderPage }))
);

const CardPlaceholderPage = lazy(() =>
  import('@features/card/presentation/pages/CardPlaceholderPage').then((m) => ({ default: m.CardPlaceholderPage }))
);

const MyPagePlaceholderPage = lazy(() =>
  import('@features/mypage/presentation/pages/MyPagePlaceholderPage').then((m) => ({ default: m.MyPagePlaceholderPage }))
);

const NewsPage = lazy(() =>
  import('@features/game/presentation/pages/NewsPage').then((m) => ({ default: m.NewsPage }))
);

const EndingPage = lazy(() =>
  import('@features/ending/presentation/pages/EndingPage').then((m) => ({ default: m.EndingPage }))
);

const NotFoundContent = lazy(() =>
  import('@shared/components/NotFoundContent').then((m) => ({ default: m.NotFoundContent }))
);

const router = createBrowserRouter([
  {
    element: <PublicRoute />,
    children: [
      { path: ROUTES.LOGIN, element: <AuthPage /> },
    ],
  },
  {
    element: <PrivateRoute />,
    children: [
      { path: ROUTES.HOME, element: <HomePage /> },
      { path: ROUTES.GAME, element: <GameMainPage /> },
      { path: ROUTES.GAME_START, element: <GameStartPage /> },
      { path: ROUTES.GAME_GUIDE, element: <GameGuidePage /> },
      { path: ROUTES.GAME_SAVE, element: <GameSaveSlotPage /> },
      { path: ROUTES.GAME_SELECT_CHARACTER, element: <SelectCharacterPage /> },
      { path: ROUTES.GAME_SET_NICKNAME, element: <SetNicknamePage /> },
      { path: ROUTES.GAME_SELECT_START_METHOD, element: <SelectStartMethodPage /> },
      { path: ROUTES.GAME_SELECT_JOB, element: <SelectJobPage /> },
      { path: ROUTES.GAME_NEWS_PATTERN, element: <NewsPage /> },
      { path: ROUTES.GAME_ENDING_PATTERN, element: <EndingPage /> },
      { path: ROUTES.LOAN, element: <LoanPlaceholderPage /> },
      { path: ROUTES.REAL_ESTATE, element: <RealEstatePage /> },
      { path: ROUTES.CARD, element: <CardPlaceholderPage /> },
      { path: ROUTES.MY_PAGE, element: <MyPagePlaceholderPage /> },
    ],
  },
  { path: ROUTES.NOT_FOUND, element: <NotFoundContent /> },
]);

export function Router() {
  return (
    <Suspense fallback={<PageSpinner />}>
      <RouterProvider router={router} />
    </Suspense>
  );
}
