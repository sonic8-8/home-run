import { useState } from 'react';
import type { JobTypeInfo } from '@features/game/domain/entities/JobTypeInfo';

// TODO: container.resolve(GetJobTypesUseCase).execute() 로 교체
const MOCK_JOB_TYPES: JobTypeInfo[] = [
  {
    jobType: 'LARGE_BIZ',
    label: '대기업 직장인',
    stats: { salary: 80, health: 60, stability: 90, growthSpeed: 40, difficulty: 70 },
  },
  {
    jobType: 'MID_BIZ',
    label: '중견기업 직장인',
    stats: { salary: 60, health: 70, stability: 70, growthSpeed: 50, difficulty: 50 },
  },
  {
    jobType: 'SMALL_BIZ',
    label: '중소기업 직장인',
    stats: { salary: 40, health: 80, stability: 50, growthSpeed: 60, difficulty: 30 },
  },
  {
    jobType: 'FREELANCER',
    label: '프리랜서',
    stats: { salary: 50, health: 50, stability: 20, growthSpeed: 90, difficulty: 85 },
  },
];

export const useSelectJob = () => {
  const [index, setIndex] = useState(0);
  const jobs = MOCK_JOB_TYPES;

  const handlePrev = () => setIndex((i) => (i - 1 + jobs.length) % jobs.length);
  const handleNext = () => setIndex((i) => (i + 1) % jobs.length);

  return {
    currentJob: jobs[index],
    hasPrev: jobs.length > 1,
    hasNext: jobs.length > 1,
    handlePrev,
    handleNext,
  };
};
