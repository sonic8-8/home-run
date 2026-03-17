import type { JobType } from './GameSlot';

export interface JobTypeStats {
  salary: number;
  health: number;
  stability: number;
  growthSpeed: number;
  difficulty: number;
}

export interface JobTypeInfo {
  jobType: JobType;
  label: string;
  stats: JobTypeStats;
}
