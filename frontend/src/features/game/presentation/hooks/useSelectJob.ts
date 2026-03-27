import { useState, useEffect } from 'react';
import { container } from '@core/di/container';
import type { JobTypeInfo } from '@features/game/domain/entities/JobTypeInfo';
import { GetJobTypesUseCase } from '../../domain/usecases/GetJobTypesUseCase';

export const useSelectJob = () => {
  const [jobs, setJobs] = useState<JobTypeInfo[]>([]);
  const [index, setIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const getJobTypesUseCase = container.resolve(GetJobTypesUseCase);

    getJobTypesUseCase.execute()
      .then((data) => setJobs(data))
      .catch(() => setError('직업 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  const handlePrev = () => setIndex((i) => (i - 1 + jobs.length) % jobs.length);
  const handleNext = () => setIndex((i) => (i + 1) % jobs.length);

  return {
    currentJob: jobs[index] ?? null,
    hasPrev: jobs.length > 1,
    hasNext: jobs.length > 1,
    handlePrev,
    handleNext,
    loading,
    error,
  };
};
