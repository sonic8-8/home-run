import { useState, useEffect } from 'react';
import type { CharacterOption } from '@features/game/domain/entities/CharacterOption';
import { GameInitRemoteDataSource } from '../../data/datasources/GameInitRemoteDataSource';
import { GameInitRepositoryImpl } from '../../data/repositories/GameInitRepositoryImpl';
import { GetCharactersUseCase } from '../../domain/usecases/GetCharactersUseCase';

const repository = new GameInitRepositoryImpl(new GameInitRemoteDataSource());
const getCharactersUseCase = new GetCharactersUseCase(repository);

export const useSelectCharacter = () => {
  const [characters, setCharacters] = useState<CharacterOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getCharactersUseCase.execute()
      .then((data) => setCharacters(data))
      .catch(() => setError('캐릭터 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return { characters, loading, error };
};
