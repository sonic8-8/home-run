import { useState, useCallback } from 'react'
import { GameWorldRemoteDataSource } from '@features/game/data/datasources/GameWorldRemoteDataSource'
import { GameWorldRepositoryImpl } from '@features/game/data/repositories/GameWorldRepositoryImpl'
import { GetGameTurnUseCase } from '@features/game/domain/usecases/GetGameTurnUseCase'
import { GetLatestNewsUseCase } from '@features/game/domain/usecases/GetLatestNewsUseCase'
import type { GameTurn, TurnNews } from '@features/game/domain/entities/GameTurn'

const gameWorldRepo = new GameWorldRepositoryImpl(new GameWorldRemoteDataSource())
const getTurnUseCase = new GetGameTurnUseCase(gameWorldRepo)
const getLatestNewsUseCase = new GetLatestNewsUseCase(gameWorldRepo)

export function useGameWorld(sessionId: number) {
  const [turn, setTurn] = useState<GameTurn | null>(null)
  const [news, setNews] = useState<TurnNews | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTurn = useCallback(async (): Promise<GameTurn | null> => {
    setLoading(true)
    setError(null)
    try {
      const result = await getTurnUseCase.execute(sessionId)
      setTurn(result)
      return result
    } catch (e) {
      setError(e instanceof Error ? e.message : '게임 턴 정보를 불러오지 못했습니다.')
      return null
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  const fetchLatestNews = useCallback(async (): Promise<TurnNews | null> => {
    setLoading(true)
    setError(null)
    try {
      const result = await getLatestNewsUseCase.execute(sessionId)
      setNews(result)
      return result
    } catch (e) {
      setError(e instanceof Error ? e.message : '뉴스를 불러오지 못했습니다.')
      return null
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  return { turn, news, loading, error, fetchTurn, fetchLatestNews }
}
