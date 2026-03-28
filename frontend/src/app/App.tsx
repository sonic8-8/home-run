import { Router } from '@app/Router';
import { useAuthSessionWatcher } from '@features/auth/presentation/hooks/useAuthSessionWatcher';

export function App() {
  useAuthSessionWatcher();

  return <Router />;
}
