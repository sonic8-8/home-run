import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ROUTES } from './routes';
import { PageSpinner } from '@shared/components/PageSpinner';
import { PrivateRoute } from '@shared/components/PrivateRoute';
import { PublicRoute } from '@shared/components/PublicRoute';
import { GameGuideLayout } from '@features/game/presentation/components/GameGuideLayout';

const AuthPage = lazy(() =>
  import('@features/auth/presentation/pages/AuthPage').then((m) => ({ default: m.AuthPage }))
);

const HomePage = lazy(() =>
  import('@features/home/presentation/pages/HomePage').then((m) => ({ default: m.HomePage }))
);

const GameStartPage = lazy(() =>
  import('@features/game/presentation/pages/GameStart').then((m) => ({ default: m.GameStartPage }))
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

const CardPage = lazy(() =>
  import('@features/card/presentation/pages/CardPage').then((m) => ({ default: m.CardPage }))
);

const CareerPage = lazy(() =>
  import('@features/career/presentation/pages/CareerPage').then((m) => ({ default: m.CareerPage }))
);

const NewsPage = lazy(() =>
  import('@features/game/presentation/pages/NewsPage').then((m) => ({ default: m.NewsPage }))
);

const EndingPage = lazy(() =>
  import('@features/ending/presentation/pages/EndingPage').then((m) => ({ default: m.EndingPage }))
);

const EndingArchivePage = lazy(() =>
  import('@features/ending/presentation/pages/EndingArchivePage').then((m) => ({
    default: m.EndingArchivePage,
  }))
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
      {
        element: <GameGuideLayout />,
        children: [
          { path: ROUTES.HOME, element: <HomePage /> },
          { path: ROUTES.GAME, element: <GameMainPage /> },
          { path: ROUTES.GAME_START, element: <GameStartPage /> },
          { path: ROUTES.GAME_SAVE, element: <GameSaveSlotPage /> },
          { path: ROUTES.GAME_SELECT_CHARACTER, element: <SelectCharacterPage /> },
          { path: ROUTES.GAME_SET_NICKNAME, element: <SetNicknamePage /> },
          { path: ROUTES.GAME_SELECT_START_METHOD, element: <SelectStartMethodPage /> },
          { path: ROUTES.GAME_SELECT_JOB, element: <SelectJobPage /> },
          { path: ROUTES.GAME_NEWS_PATTERN, element: <NewsPage /> },
          { path: ROUTES.GAME_ENDING_ARCHIVE, element: <EndingArchivePage /> },
          { path: ROUTES.GAME_ENDING_PATTERN, element: <EndingPage /> },
          { path: ROUTES.LOAN, element: <LoanPlaceholderPage /> },
          { path: ROUTES.REAL_ESTATE, element: <RealEstatePage mode="browse" /> },
          { path: ROUTES.REAL_ESTATE_NEW_GAME, element: <RealEstatePage mode="new-game" /> },
          { path: ROUTES.REAL_ESTATE_LOAN_APPLY, element: <RealEstatePage mode="loan-apply" /> },
          { path: ROUTES.CARD, element: <CardPage /> },
          { path: ROUTES.MY_PAGE, element: <CareerPage /> },
        ],
      },
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
