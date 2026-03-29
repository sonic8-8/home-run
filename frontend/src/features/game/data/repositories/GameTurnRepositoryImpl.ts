import { inject, injectable } from 'tsyringe'
import { ResponseMappingError } from '@core/error/AppError'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'
import type {
  GameTurn,
  EconomicCyclePhase,
  EconomicCycleType,
  TurnCommitResult,
  TurnNews,
  TurnPreview,
  TurnSlotSelection,
  TurnStatChanges,
} from '@features/game/domain/entities/GameTurn'
import type { TurnAction, TurnActions } from '@features/game/domain/entities/TurnAction'
import { GameTurnRemoteDataSource } from '@features/game/data/datasources/GameTurnRemoteDataSource'

@injectable()
export class GameTurnRepositoryImpl implements IGameTurnRepository {
  private readonly dataSource: GameTurnRemoteDataSource

  constructor(
    @inject(GameTurnRemoteDataSource)
    dataSource: GameTurnRemoteDataSource,
  ) {
    this.dataSource = dataSource
  }

  async getTurn(sessionId: number): Promise<GameTurn> {
    const model = await this.dataSource.getTurn(sessionId)
    return {
      turnNumber: model.turnNumber,
      currentDate: this.toDate(model.currentDate),
      month: model.month,
      economicCycle: {
        phase: this.toEconomicCyclePhase(model.economicCycle.phase),
        description: model.economicCycle.description,
      },
    }
  }

  async getAvailableActions(sessionId: number): Promise<TurnActions> {
    const model = await this.dataSource.getAvailableActions(sessionId)
    return {
      shopping: model.shopping.map((action) => this.toTurnAction(action)),
      activities: model.activities.map((action) => this.toTurnAction(action)),
    }
  }

  async getLatestNews(sessionId: number): Promise<TurnNews> {
    const model = await this.dataSource.getLatestNews(sessionId)
    return {
      turnNumber: model.turnNumber,
      currentDate: this.toDate(model.currentDate),
      news: model.news.map((newsItem) => ({
        newsId: newsItem.newsId,
        headline: newsItem.headline,
        content: newsItem.content,
        sourceName: newsItem.sourceName,
        publishedDate: this.toDate(newsItem.publishedDate),
        economicCycleType: this.toEconomicCycleType(newsItem.economicCycleType),
      })),
    }
  }

  async submitTurnSlots(
    sessionId: number,
    slots: readonly TurnSlotSelection[],
  ): Promise<TurnPreview> {
    const model = await this.dataSource.submitTurnSlots(sessionId, {
      slots: slots.map((slot) => ({
        slotIndex: slot.slotIndex,
        actionType: slot.actionType,
      })),
    })

    return {
      slots: model.slots.map((slot) => ({
        slotIndex: slot.slotIndex,
        actionType: slot.actionType,
        forcedAction: slot.forcedAction,
      })),
      previewCashChange: model.previewCashChange,
      previewStatChanges: this.toTurnStatChanges(model.previewStatChanges),
    }
  }

  async commitTurn(sessionId: number): Promise<TurnCommitResult> {
    const model = await this.dataSource.commitTurn(sessionId)

    return {
      turnNumber: model.turnNumber,
      settlementLog: model.settlementLog.map((item) => ({
        phase: item.phase,
        description: item.description,
        cashChange: item.cashChange,
        statChanges: this.toTurnStatChanges(item.statChanges),
      })),
      updatedAssets: {
        cash: model.updatedAssets.cash,
        loan: model.updatedAssets.loan,
        realEstateValue: model.updatedAssets.realEstateValue,
        netAssets: model.updatedAssets.netAssets,
      },
      statChanges: this.toTurnStatChanges(model.statChanges),
      flags: {
        isBankrupt: model.flags.isBankrupt,
        isCleared: model.flags.isCleared,
        isBurnout: model.flags.isBurnout,
        isForcedResignation: model.flags.isForcedResignation,
        hasEvent: model.flags.hasEvent,
      },
    }
  }

  private toTurnAction(action: TurnAction): TurnAction {
    return {
      actionType: action.actionType,
      label: action.label,
      iconUrl: action.iconUrl,
      effects: {
        cash: action.effects.cash,
        health: action.effects.health,
        fatigue: action.effects.fatigue,
        stress: action.effects.stress,
        happiness: action.effects.happiness,
        knowledge: action.effects.knowledge,
      },
    }
  }

  private toTurnStatChanges(statChanges: TurnStatChanges): TurnStatChanges {
    return {
      health: statChanges.health,
      fatigue: statChanges.fatigue,
      stress: statChanges.stress,
      happiness: statChanges.happiness,
      knowledge: statChanges.knowledge,
    }
  }

  private toDate(value: string): Date {
    const date = new Date(`${value}T00:00:00`)

    if (Number.isNaN(date.getTime())) {
      throw new ResponseMappingError(`유효하지 않은 날짜 형식입니다: ${value}`)
    }

    return date
  }

  private toEconomicCyclePhase(value: string): EconomicCyclePhase {
    if (value === 'BOOM' || value === 'CRISIS' || value === 'RECOVERY') {
      return value
    }

    throw new ResponseMappingError(`지원하지 않는 경기 사이클 단계입니다: ${value}`)
  }

  private toEconomicCycleType(value: string): EconomicCycleType {
    switch (value) {
      case 'BOOM_TO_BOOM':
      case 'BOOM_TO_CRISIS':
      case 'BOOM_TO_RECOVERY':
      case 'CRISIS_TO_CRISIS':
      case 'CRISIS_TO_RECOVERY':
      case 'CRISIS_TO_BOOM':
      case 'RECOVERY_TO_BOOM':
      case 'RECOVERY_TO_RECOVERY':
      case 'RECOVERY_TO_CRISIS':
      case 'EXPANSION':
      case 'CONTRACTION':
      case 'RECOVERY':
      case 'PEAK':
      case 'TROUGH':
        return value
      default:
        throw new ResponseMappingError(`지원하지 않는 경기 사이클 전이입니다: ${value}`)
    }
  }
}
