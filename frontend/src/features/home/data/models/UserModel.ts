export interface UserMeResponseModel {
  userId: number;
  email: string;
  name: string;
  isAssetLinked: boolean;
  totalAssetAmount: number | null;
}

export interface AssetLinkResponseModel {
  isAssetLinked: boolean;
  mainAccountCreated: boolean;
  seedmoneyAccountCreated: boolean;
  summaryInitialized: boolean;
}
