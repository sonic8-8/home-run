import { useState, useEffect } from 'react';
import { container } from '@core/di/container';
import type { CharacterOption } from '@features/game/domain/entities/CharacterOption';
import { GetCharactersUseCase } from '../../domain/usecases/GetCharactersUseCase';

export const useSelectCharacter = () => {
  const [characters, setCharacters] = useState<CharacterOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const getCharactersUseCase = container.resolve(GetCharactersUseCase);

    getCharactersUseCase.execute()
      .then((data) => setCharacters(data))
      .catch(() => setError('캐릭터 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return { characters, loading, error };
};
