export interface TurnActionEffects {
  cash?: number;
  health?: number;
  knowledge?: number;
  happiness?: number;
  fatigue?: number;
  stress?: number;
}

export interface TurnAction {
  readonly actionType: string;
  readonly label: string;
  readonly iconUrl: string;
  readonly effects: TurnActionEffects;
}

export interface TurnActions {
  readonly shopping: TurnAction[];
  readonly activities: TurnAction[];
}
