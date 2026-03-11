export type GameEventId =
  | 'SANTA_GIFT'
  | 'STOCK_CRASH'
  | 'BONUS_SALARY'
  | 'PROPERTY_TAX'
  | 'GOVERNMENT_SUBSIDY'; 

export interface GameEventButton {
    readonly label: string;
    readonly variant: 'primary' | 'secondary' ;
    readonly actionId: string;
}

export interface GameEvent {
  readonly id: GameEventId;
  readonly title: string;
  readonly description: string;
  readonly imageSrc: string;
  readonly cardColor?: string;
  readonly buttons: GameEventButton[]; 
}