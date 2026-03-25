export interface Region {
  regionCode: string;
  name: string;
}

export interface District {
  districtCode: string;
  name: string;
}

export interface TargetProperty {
  propertyId: number;
  name: string;
  recentPrice: number;
  latitude: number;
  longitude: number;
}
