import type { EndingReport } from '../entities/EndingReport';
import type { EndingTimelinePoint } from '../entities/EndingTimeline';

export interface IEndingRepository {
  getEndingReport(sessionId: number): Promise<EndingReport>;
  getEndingLogs(sessionId: number): Promise<EndingTimelinePoint[]>;
}
