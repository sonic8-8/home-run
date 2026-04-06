import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { SelectStartMethodPage } from '@features/game/presentation/pages/SelectStartMethod';

function StatePreview() {
  const location = useLocation();
  return <pre data-testid="route-state">{JSON.stringify(location.state ?? null)}</pre>;
}

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={[
        {
          pathname: ROUTES.GAME_SELECT_START_METHOD,
          state: {
            slotNumber: 3,
            characterType: 'FEMALE',
            characterName: '테스터',
          },
        },
      ]}
    >
      <Routes>
        <Route path={ROUTES.GAME_SELECT_START_METHOD} element={<SelectStartMethodPage />} />
        <Route
          path={ROUTES.GAME_SELECT_JOB}
          element={
            <div>
              <span>job-page</span>
              <StatePreview />
            </div>
          }
        />
        <Route
          path={ROUTES.REAL_ESTATE_NEW_GAME}
          element={
            <div>
              <span>property-page</span>
              <StatePreview />
            </div>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe('SelectStartMethod', () => {
  it('routes MY_DATA starts directly to the new-game property route', () => {
    renderPage();

    fireEvent.click(screen.getByRole('button', { name: '자산 연결해서 시작하기' }));
    fireEvent.click(screen.getByRole('button', { name: 'NEXT >' }));

    expect(screen.getByText('property-page')).toBeInTheDocument();
    expect(screen.getByTestId('route-state')).toHaveTextContent(
      JSON.stringify({
        slotNumber: 3,
        characterType: 'FEMALE',
        characterName: '테스터',
        useMyData: true,
      }),
    );
  });

  it('routes PROFILE starts to the job selection page', () => {
    renderPage();

    fireEvent.click(screen.getByRole('button', { name: '직업 선택하기' }));
    fireEvent.click(screen.getByRole('button', { name: 'NEXT >' }));

    expect(screen.getByText('job-page')).toBeInTheDocument();
    expect(screen.getByTestId('route-state')).toHaveTextContent(
      JSON.stringify({
        slotNumber: 3,
        characterType: 'FEMALE',
        characterName: '테스터',
        useMyData: false,
      }),
    );
  });
});
