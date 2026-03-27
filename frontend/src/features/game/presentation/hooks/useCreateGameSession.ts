import { useCallback, useState } from 'react';
import { container } from '@core/di/container';
import type { CreateGameSessionInput } from '@features/game/domain/entities/CreateGameSessionInput';
import type { GameSessionCreation } from '@features/game/domain/entities/GameSessionCreation';
import { CreateGameSessionUseCase } from '@features/game/domain/usecases/CreateGameSessionUseCase';

export const useCreateGameSession = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createSession = useCallback(
    async (input: CreateGameSessionInput): Promise<GameSessionCreation> => {
      setIsSubmitting(true);
      setError(null);
      try {
        const useCase = container.resolve(CreateGameSessionUseCase);
        return await useCase.execute(input);
      } catch (createError) {
        const message =
          createError instanceof Error
            ? createError.message
            : '새 게임을 시작하지 못했습니다.';
        setError(message);
        throw createError;
      } finally {
        setIsSubmitting(false);
      }
    },
    [],
  );

  return { createSession, isSubmitting, error };
};
