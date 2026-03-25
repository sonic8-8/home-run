import type { CharacterOption } from '../entities/CharacterOption';
import type { JobTypeInfo } from '../entities/JobTypeInfo';
import type { Region, District, TargetProperty } from '../entities/Region';

export interface IGameInitRepository {
  getCharacters(): Promise<CharacterOption[]>;
  getJobTypes(): Promise<JobTypeInfo[]>;
  getRegions(): Promise<Region[]>;
  getDistricts(regionCode: string): Promise<District[]>;
  getTargetProperties(regionCode: string, districtCode: string): Promise<TargetProperty[]>;
}
