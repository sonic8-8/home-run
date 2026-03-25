import type { CharacterOption } from '../entities/CharacterOption';
import type { JobTypeInfo } from '../entities/JobTypeInfo';
import type { Region, District, TargetProperty } from '../entities/Region';
import type { ProfileOption } from '../entities/ProfileOption';

export interface IGameInitRepository {
  getCharacters(): Promise<CharacterOption[]>;
  getJobTypes(): Promise<JobTypeInfo[]>;
  getProfiles(): Promise<ProfileOption[]>;
  getRegions(): Promise<Region[]>;
  getDistricts(regionCode: string): Promise<District[]>;
  getTargetProperties(regionCode: string, districtCode: string): Promise<TargetProperty[]>;
}
