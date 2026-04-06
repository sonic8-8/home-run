import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ROUTES } from '@app/routes';
import { SelectJobPage } from './SelectJobPage';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({
      state: {
        slotNumber: 3,
        characterType: 'FEMALE',
        characterName: '테스터',
        useMyData: false,
      },
    }),
  };
});

vi.mock('../../hooks/useSelectJob', () => ({
  useSelectJob: () => ({
    currentJob: {
      jobType: 'STARTUP',
      label: '스타트업 직장인',
      stats: {
        salary: 60,
        health: 55,
        stability: 40,
        growthSpeed: 80,
        difficulty: 65,
      },
    },
    handlePrev: vi.fn(),
    handleNext: vi.fn(),
  }),
}));

describe('SelectJobPage', () => {
  beforeEach(() => {
    mockNavigate.mockReset();
  });

  it('routes the selected job flow into the new-game property route', () => {
    render(
      <MemoryRouter>
        <SelectJobPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('button', { name: 'START' }));

    expect(mockNavigate).toHaveBeenCalledWith(ROUTES.REAL_ESTATE_NEW_GAME, {
      state: {
        slotNumber: 3,
        characterType: 'FEMALE',
        characterName: '테스터',
        useMyData: false,
        jobType: 'STARTUP',
      },
    });
  });
});
