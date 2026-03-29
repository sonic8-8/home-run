export interface TurnSlotRequestModel {
  slotIndex: number
  actionType: string
}

export interface SubmitTurnSlotsRequestModel {
  slots: TurnSlotRequestModel[]
}

export interface ResolveEventRequestModel {
  choiceId?: number
}
