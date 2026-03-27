export interface UserMe {
  userId: number;
  email: string;
  name: string;
  isAssetLinked: boolean;
  totalAssetAmount: number | null;
  netAssetAmount: number | null;
}
