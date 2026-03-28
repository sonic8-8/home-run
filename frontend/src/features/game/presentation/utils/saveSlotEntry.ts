import type { GameSlot } from '@features/game/domain/entities/GameSlot';

export type GameSaveEntryMode = 'continue' | 'new';

export interface GameSaveLocationState {
  entryMode?: GameSaveEntryMode;
}

export function parseGameSaveEntryMode(
  state: GameSaveLocationState | GameSaveEntryMode | string | null | undefined,
): GameSaveEntryMode | null {
  if (state === 'continue' || state === 'new') {
    return state;
  }

  if (
    typeof state === 'object' &&
    state !== null &&
    (state.entryMode === 'continue' || state.entryMode === 'new')
  ) {
    return state.entryMode;
  }

  return null;
}

export function getGameSaveTitle(entryMode: GameSaveEntryMode | null): string {
  if (entryMode === 'continue') {
    return '이어할 세션 선택';
  }

  if (entryMode === 'new') {
    return '새로 시작할 슬롯 선택';
  }

  return 'SAVE';
}

export function getGameSaveDescription(entryMode: GameSaveEntryMode | null): string {
  if (entryMode === 'continue') {
    return '진행 중인 세션만 이어할 수 있습니다.';
  }

  if (entryMode === 'new') {
    return '빈 슬롯에서만 새 게임을 시작할 수 있습니다.';
  }

  return '저장 슬롯을 선택하세요.';
}

export function isSlotSelectable(
  entryMode: GameSaveEntryMode | null,
  slot: GameSlot,
): boolean {
  if (entryMode === 'continue') {
    return slot.status === 'IN_PROGRESS';
  }

  if (entryMode === 'new') {
    return slot.status === 'EMPTY';
  }

  return slot.status === 'EMPTY' || slot.status === 'IN_PROGRESS';
}

export function getSlotUnavailableMessage(
  entryMode: GameSaveEntryMode | null,
  slot: GameSlot,
): string {
  if (entryMode === 'continue') {
    if (slot.status === 'EMPTY') {
      return '이어하기는 진행 중인 슬롯에서만 가능합니다.';
    }

    return '종료된 세션은 아직 이어하기를 지원하지 않습니다.';
  }

  if (entryMode === 'new') {
    if (slot.status === 'IN_PROGRESS') {
      return '새 게임은 빈 슬롯에서만 시작할 수 있습니다.';
    }

    return '종료된 슬롯은 새 게임 시작에 사용할 수 없습니다.';
  }

  if (slot.status !== 'IN_PROGRESS') {
    return '종료된 세션은 아직 이어하기를 지원하지 않습니다.';
  }

  return '세션 정보를 확인하지 못했습니다.';
}

export function getSlotHelperText(
  entryMode: GameSaveEntryMode | null,
  slot: GameSlot,
): string | null {
  if (slot.status === 'EMPTY') {
    if (entryMode === 'continue') {
      return '빈 슬롯 - 새로하기에서 시작 가능';
    }

    return '빈 슬롯 - 새 게임 시작';
  }

  if (slot.status === 'IN_PROGRESS' && entryMode === 'new') {
    return '진행 중 - 이어하기에서 선택 가능';
  }

  if (slot.status !== 'IN_PROGRESS' && entryMode === 'new') {
    return '종료됨 - 새 게임 시작 불가';
  }

  if (slot.status !== 'IN_PROGRESS' && entryMode === 'continue') {
    return '종료됨 - 이어하기 불가';
  }

  return null;
}
