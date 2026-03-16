import type { JobType } from '../entities/GameSlot';

export const JOB_TYPE_LABELS: Record<JobType, string> = {
  LARGE_BIZ: '대기업 직장인',
  STARTUP: '스타트업 직장인',
  PUBLIC: '공무원',
  FREELANCER: '프리랜서',
  SELF_EMPLOYED: '자영업자',
};
