import type { JobType } from './GameSlot';

export interface ProfileStats {
  salary: number;
  health: number;
  stability: number;
  growthSpeed: number;
  difficulty: number;
}

export interface ProfileOption {
  profileCode: string;
  name: string;
  jobType: JobType;
  annualSalary: number;
  initialCash: number;
  stats: ProfileStats;
}
