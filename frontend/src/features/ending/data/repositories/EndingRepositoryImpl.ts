import { inject, injectable } from 'tsyringe';
import { ResponseMappingError } from '@core/error/AppError';
import { EndingRemoteDataSource } from '../datasources/EndingRemoteDataSource';
import type {
  EndingAchievementModel,
  EndingEventHistoryModel,
  EndingHousingHistoryModel,
  EndingHousingSnapshotModel,
  EndingHousingStateModel,
  EndingNewsHistoryModel,
  EndingReportResponseModel,
  EndingTimelinePointModel,
} from '../models/EndingModel';
import type {
  EndingAchievement,
  CharacterType,
  EndingEventHistory,
  EndingHousingHistory,
  EndingHousingSnapshot,
  EndingHousingState,
  EndingNewsHistory,
  EndingReport,
  EndingType,
  HousingType,
} from '../../domain/entities/EndingReport';
import type { EndingTimelinePoint } from '../../domain/entities/EndingTimeline';
import type { IEndingRepository } from '../../domain/repositories/IEndingRepository';

function toEndingType(value: string): EndingType {
  if (
    value === 'IN_PROGRESS' ||
    value === 'CLEAR' ||
    value === 'BANKRUPT' ||
    value === 'TIMEOUT' ||
    value === 'FORECLOSURE'
  ) {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 엔딩 타입입니다: ${value}`);
}

function toCharacterType(value: string): CharacterType {
  if (value === 'MALE' || value === 'FEMALE') {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 캐릭터 타입입니다: ${value}`);
}

function toHousingType(value: string): HousingType {
  if (
    value === 'NONE' ||
    value === 'STUDIO' ||
    value === 'VILLA' ||
    value === 'JEONSE_APT' ||
    value === 'OWNED_APT'
  ) {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 주거 형태입니다: ${value}`);
}

function toAchievement(model: EndingAchievementModel): EndingAchievement {
  return {
    name: model.name,
    iconUrl: model.iconUrl,
  };
}

function toNewsHistory(model: EndingNewsHistoryModel): EndingNewsHistory {
  return {
    turnNumber: model.turnNumber,
    newsId: model.newsId,
    headline: model.headline,
    publishedDate: new Date(model.publishedDate),
  };
}

function toEventHistory(model: EndingEventHistoryModel): EndingEventHistory {
  return {
    turnNumber: model.turnNumber,
    gameEventId: model.gameEventId,
    selectedChoiceCode: model.selectedChoiceCode,
    resultSummary: model.resultSummary,
    resolvedAt: model.resolvedAt === null ? null : new Date(model.resolvedAt),
  };
}

function toHousingState(model: EndingHousingStateModel | null): EndingHousingState | null {
  if (model === null) {
    return null;
  }

  return {
    housingType: toHousingType(model.housingType),
    propertyId: model.propertyId,
  };
}

function toHousingHistory(model: EndingHousingHistoryModel): EndingHousingHistory {
  return {
    turnNumber: model.turnNumber,
    summary: model.summary,
    beforeState: toHousingState(model.beforeState),
    afterState: toHousingState(model.afterState),
  };
}

function toHousingSnapshot(model: EndingHousingSnapshotModel | null): EndingHousingSnapshot | null {
  if (model === null) {
    return null;
  }

  return {
    currentHousingType: toHousingType(model.currentHousingType),
    currentPropertyId: model.currentPropertyId,
    targetPropertyId: model.targetPropertyId,
  };
}

function toTimelinePoint(model: EndingTimelinePointModel): EndingTimelinePoint {
  return {
    turnNumber: model.turnNumber,
    date: new Date(model.date),
    cash: model.cash,
    netAssets: model.netAssets,
    totalAssets: model.totalAssets,
    stockValue: model.stockValue,
    loanBalance: model.loanBalance,
    salary: model.salary,
  };
}

function toEndingReport(model: EndingReportResponseModel): EndingReport {
  return {
    characterType: toCharacterType(model.characterType),
    endingType: toEndingType(model.endingType),
    grade: model.grade,
    title: model.title,
    totalAssets: model.totalAssets,
    totalIncome: model.totalIncome,
    totalExpense: model.totalExpense,
    netProfit: model.netProfit,
    spendingPattern: model.spendingPattern,
    achievements: model.achievements.map(toAchievement),
    newsHistories: model.newsHistories.map(toNewsHistory),
    eventHistories: model.eventHistories.map(toEventHistory),
    housingHistories: model.housingHistories.map(toHousingHistory),
    housingSnapshot: toHousingSnapshot(model.housingSnapshot),
  };
}

@injectable()
export class EndingRepositoryImpl implements IEndingRepository {
  constructor(
    @inject(EndingRemoteDataSource)
    private readonly dataSource: EndingRemoteDataSource,
  ) {}

  async getEndingReport(sessionId: number): Promise<EndingReport> {
    const response = await this.dataSource.getEndingReport(sessionId);
    return toEndingReport(response);
  }

  async getEndingLogs(sessionId: number): Promise<EndingTimelinePoint[]> {
    const response = await this.dataSource.getEndingLogs(sessionId);
    return response.timeline.map(toTimelinePoint);
  }
}
