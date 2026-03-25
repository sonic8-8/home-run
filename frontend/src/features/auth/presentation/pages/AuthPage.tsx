import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { OnboardingView } from '../components/OnboardingView/OnboardingView';
import { EmailLoginForm } from '../components/EmailLoginForm/EmailLoginForm';
import { SignUpForm } from '../components/SignUpForm/SignUpForm';
import styles from './AuthPage.module.css';

export const AuthPage: React.FC = () => {
  const { view, setView, isLoading, error, login, signUp } = useAuth();

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        {view === 'onboarding' && (
          <OnboardingView
            onEmailLogin={() => setView('emailLogin')}
          />
        )}
        {view === 'emailLogin' && (
          <EmailLoginForm
            isLoading={isLoading}
            error={error}
            onSubmit={login}
            onSignUp={() => setView('signUp')}
          />
        )}
        {view === 'signUp' && (
          <SignUpForm
            isLoading={isLoading}
            error={error}
            onSubmit={signUp}
            onBack={() => setView('emailLogin')}
          />
        )}
      </div>
    </div>
  );
};
