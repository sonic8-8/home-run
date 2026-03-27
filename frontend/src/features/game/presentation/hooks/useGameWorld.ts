import { useState, useCallback } from 'react'
import { container } from '@core/di/container'
import { GetGameTurnUseCase } from '@features/game/domain/usecases/GetGameTurnUseCase'
import { GetLatestNewsUseCase } from '@features/game/domain/usecases/GetLatestNewsUseCase'
import type { GameTurn, TurnNews } from '@features/game/domain/entities/GameTurn'

export function useGameWorld(sessionId: number | null) {
  const [turn, setTurn] = useState<GameTurn | null>(null)
  const [news, setNews] = useState<TurnNews | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTurn = useCallback(async (): Promise<GameTurn | null> => {
    if (sessionId === null) {
      return null
    }
    setLoading(true)
    setError(null)
    try {
      const getTurnUseCase = container.resolve(GetGameTurnUseCase)
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
    if (sessionId === null) {
      return null
    }
    setLoading(true)
    setError(null)
    try {
      const getLatestNewsUseCase = container.resolve(GetLatestNewsUseCase)
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
