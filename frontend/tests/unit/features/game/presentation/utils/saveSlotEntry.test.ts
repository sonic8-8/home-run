import { describe, expect, it } from 'vitest';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import {
  getGameSaveDescription,
  getGameSaveTitle,
  getSlotHelperText,
  getSlotUnavailableMessage,
  isSlotSelectable,
  parseGameSaveEntryMode,
} from '@features/game/presentation/utils/saveSlotEntry';

const emptySlot: GameSlot = {
  slotNumber: 3,
  sessionId: null,
  status: 'EMPTY',
};

const activeSlot: GameSlot = {
  slotNumber: 1,
  sessionId: 10,
  status: 'IN_PROGRESS',
  characterName: '테스터',
  jobType: 'STARTUP',
  currentTurn: 3,
};

const endedSlot: GameSlot = {
  slotNumber: 2,
  sessionId: 20,
  status: 'CLEAR',
  characterName: '완주자',
  jobType: 'LARGE_BIZ',
  currentTurn: 12,
};

describe('saveSlotEntry', () => {
  it('parses entry mode from route state', () => {
    expect(parseGameSaveEntryMode({ entryMode: 'continue' })).toBe('continue');
    expect(parseGameSaveEntryMode({ entryMode: 'new' })).toBe('new');
    expect(parseGameSaveEntryMode(null)).toBeNull();
  });

  it('describes continue mode and only allows in-progress slots', () => {
    expect(getGameSaveTitle('continue')).toBe('이어할 세션 선택');
    expect(getGameSaveDescription('continue')).toBe('진행 중인 세션만 이어할 수 있습니다.');
    expect(isSlotSelectable('continue', activeSlot)).toBe(true);
    expect(isSlotSelectable('continue', emptySlot)).toBe(false);
    expect(getSlotUnavailableMessage('continue', emptySlot)).toBe('이어하기는 진행 중인 슬롯에서만 가능합니다.');
  });

  it('describes new mode and only allows empty slots', () => {
    expect(getGameSaveTitle('new')).toBe('새로 시작할 슬롯 선택');
    expect(getSlotHelperText('new', emptySlot)).toBe('빈 슬롯 - 새 게임 시작');
    expect(isSlotSelectable('new', emptySlot)).toBe(true);
    expect(isSlotSelectable('new', activeSlot)).toBe(false);
    expect(getSlotHelperText('new', activeSlot)).toBe('진행 중 - 이어하기에서 선택 가능');
    expect(getSlotHelperText('new', endedSlot)).toBe('종료됨 - 새 게임 시작 불가');
  });

  it('keeps the mixed fallback when no entry mode is provided', () => {
    expect(getGameSaveTitle(null)).toBe('SAVE');
    expect(isSlotSelectable(null, emptySlot)).toBe(true);
    expect(isSlotSelectable(null, activeSlot)).toBe(true);
    expect(isSlotSelectable(null, endedSlot)).toBe(false);
  });
});
