import type { CharacterType } from './CharacterOption';
import type { JobType } from './GameSlot';

export interface CreateGameSessionInput {
  readonly slotNumber: 1 | 2 | 3;
  readonly characterType: CharacterType;
  readonly characterName: string;
  readonly jobType: JobType;
  readonly regionCode: string;
  readonly districtCode: string;
  readonly targetPropertyId: number;
  readonly useMyData: boolean;
}
