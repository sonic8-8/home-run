import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import { PrivateRoute } from '@shared/components/PrivateRoute';
import { PublicRoute } from '@shared/components/PublicRoute';
import { useAuthStore } from '@core/store/authStore';

const initialState = useAuthStore.getState();

function resetAuthStore() {
  useAuthStore.setState(initialState);
  localStorage.clear();
}

function renderPrivateRoute(initialEntry: string) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route path="/login" element={<div>Login Screen</div>} />
        <Route element={<PrivateRoute />}>
          <Route path="/loan" element={<div>Loan Page</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

function renderPublicRoute(initialEntry: string) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route path="/" element={<div>Home Screen</div>} />
        <Route element={<PublicRoute />}>
          <Route path="/login" element={<div>Login Screen</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('route guards', () => {
  beforeEach(() => {
    resetAuthStore();
  });

  it('redirects unauthenticated users away from private routes', () => {
    renderPrivateRoute('/loan');

    expect(screen.getByText('Login Screen')).toBeInTheDocument();
    expect(screen.queryByText('Loan Page')).not.toBeInTheDocument();
  });

  it('allows authenticated users through private routes', () => {
    useAuthStore.getState().setAuth('access-token', 'refresh-token', '테스터');

    renderPrivateRoute('/loan');

    expect(screen.getByText('Loan Page')).toBeInTheDocument();
    expect(screen.queryByText('Login Screen')).not.toBeInTheDocument();
  });

  it('keeps unauthenticated users on public routes', () => {
    renderPublicRoute('/login');

    expect(screen.getByText('Login Screen')).toBeInTheDocument();
    expect(screen.queryByText('Home Screen')).not.toBeInTheDocument();
  });

  it('redirects authenticated users away from public routes', () => {
    useAuthStore.getState().setAuth('access-token', 'refresh-token', '테스터');

    renderPublicRoute('/login');

    expect(screen.getByText('Home Screen')).toBeInTheDocument();
    expect(screen.queryByText('Login Screen')).not.toBeInTheDocument();
  });
});
