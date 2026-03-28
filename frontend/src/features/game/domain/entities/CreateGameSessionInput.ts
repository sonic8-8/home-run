import type { CharacterType } from './CharacterOption';
import type { JobType } from './GameSlot';

interface BaseCreateGameSessionInput {
  readonly slotNumber: 1 | 2 | 3;
  readonly characterType: CharacterType;
  readonly characterName: string;
  readonly regionCode: string;
  readonly districtCode: string;
  readonly targetPropertyId: number;
}

export type CreateGameSessionInput =
  | (BaseCreateGameSessionInput & {
      readonly useMyData: true;
      readonly jobType?: never;
    })
  | (BaseCreateGameSessionInput & {
      readonly useMyData: false;
      readonly jobType: JobType;
    });
