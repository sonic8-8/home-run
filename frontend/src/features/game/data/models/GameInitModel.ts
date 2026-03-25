// ---- Characters ----
export interface CharacterOptionModel {
  characterType: string;
  thumbnailUrl: string;
}

export interface CharacterOptionsResponseModel {
  characters: CharacterOptionModel[];
}

// ---- Job Types ----
export interface JobTypeStatsModel {
  salary: number;
  health: number;
  stability: number;
  growthSpeed: number;
  difficulty: number;
}

export interface JobTypeOptionModel {
  jobType: string;
  label: string;
  stats: JobTypeStatsModel;
}

export interface JobTypeOptionsResponseModel {
  jobTypes: JobTypeOptionModel[];
}

// ---- Regions ----
export interface RegionModel {
  regionCode: string;
  name: string;
}

export interface RegionListResponseModel {
  regions: RegionModel[];
}

// ---- Districts ----
export interface DistrictModel {
  districtCode: string;
  name: string;
}

export interface DistrictListResponseModel {
  regionCode: string;
  districts: DistrictModel[];
}

// ---- Target Properties ----
export interface TargetPropertyModel {
  propertyId: number;
  name: string;
  recentPrice: number;
  latitude: number;
  longitude: number;
}

export interface TargetPropertyListResponseModel {
  properties: TargetPropertyModel[];
}
