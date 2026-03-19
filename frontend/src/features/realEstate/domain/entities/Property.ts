export type HousingType = 'NONE' | 'STUDIO' | 'VILLA' | 'JEONSE_APT' | 'OWNED_APT';

export interface PropertySpecs {
  readonly area: number;
  readonly floor: string;
  readonly direction: string;
}

export interface Property {
  readonly propertyId: string;
  readonly name: string;
  readonly recentPrice: number;
  readonly address: string;
  readonly latitude: number;
  readonly longitude: number;
  readonly housingType: HousingType;
  readonly deposit: number;
  readonly maintenanceFee: number;
  readonly specs: PropertySpecs;
}

export interface PropertySummary {
  readonly propertyId: string;
  readonly name: string;
  readonly recentPrice: number;
  readonly latitude: number;
  readonly longitude: number;
}
