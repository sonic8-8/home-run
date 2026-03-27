import type { JobType } from '../entities/GameSlot';

export const JOB_TYPE_LABELS: Record<JobType, string> = {
  LARGE_BIZ: '대기업 직장인',
  MID_BIZ: '중견기업 직장인',
  SMALL_BIZ: '중소기업 직장인',
  STARTUP: '스타트업 직장인',
  FREELANCER: '프리랜서',
};
