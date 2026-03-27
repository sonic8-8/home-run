import { inject, injectable } from 'tsyringe';
import type { IGameInitRepository } from '../../domain/repositories/IGameInitRepository';
import type { CharacterOption, CharacterType } from '../../domain/entities/CharacterOption';
import type { JobTypeInfo } from '../../domain/entities/JobTypeInfo';
import type { JobType } from '../../domain/entities/GameSlot';
import type { ProfileOption } from '../../domain/entities/ProfileOption';
import type { Region, District, TargetProperty } from '../../domain/entities/Region';
import { GameInitRemoteDataSource } from '../datasources/GameInitRemoteDataSource';

@injectable()
export class GameInitRepositoryImpl implements IGameInitRepository {
  private readonly dataSource: GameInitRemoteDataSource;
  constructor(
    @inject(GameInitRemoteDataSource)
    dataSource: GameInitRemoteDataSource,
  ) {
    this.dataSource = dataSource;
  }

  async getCharacters(): Promise<CharacterOption[]> {
    const m = await this.dataSource.getCharacters();
    return m.characters.map((c) => ({
      characterType: c.characterType as CharacterType,
      thumbnailUrl: c.thumbnailUrl,
    }));
  }

  async getJobTypes(): Promise<JobTypeInfo[]> {
    const m = await this.dataSource.getJobTypes();
    return m.jobTypes.map((j) => ({
      jobType: j.jobType as JobType,
      label: j.label,
      stats: {
        salary: j.stats.salary,
        health: j.stats.health,
        stability: j.stats.stability,
        growthSpeed: j.stats.growthSpeed,
        difficulty: j.stats.difficulty,
      },
    }));
  }

  async getProfiles(): Promise<ProfileOption[]> {
    const m = await this.dataSource.getProfiles();
    return m.profiles.map((p) => ({
      profileCode: p.profileCode,
      name: p.name,
      jobType: p.jobType as JobType,
      annualSalary: p.annualSalary,
      initialCash: p.initialCash,
      stats: {
        salary: p.stats.salary,
        health: p.stats.health,
        stability: p.stats.stability,
        growthSpeed: p.stats.growthSpeed,
        difficulty: p.stats.difficulty,
      },
    }));
  }

  async getRegions(): Promise<Region[]> {
    const m = await this.dataSource.getRegions();
    return m.regions.map((r) => ({ regionCode: r.regionCode, name: r.name }));
  }

  async getDistricts(regionCode: string): Promise<District[]> {
    const m = await this.dataSource.getDistricts(regionCode);
    return m.districts.map((d) => ({ districtCode: d.districtCode, name: d.name }));
  }

  async getTargetProperties(regionCode: string, districtCode: string): Promise<TargetProperty[]> {
    const m = await this.dataSource.getTargetProperties(regionCode, districtCode);
    return m.properties.map((p) => ({
      propertyId: p.propertyId,
      name: p.name,
      recentPrice: p.recentPrice,
      latitude: p.latitude,
      longitude: p.longitude,
    }));
  }
}
