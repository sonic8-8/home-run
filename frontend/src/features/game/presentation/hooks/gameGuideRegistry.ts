import { ROUTES } from '@app/routes';

export type GameGuideFlowId = 'start' | 'main';

export interface GameGuideStep {
  readonly id: string;
  readonly flowId: GameGuideFlowId;
  readonly path: string;
  readonly anchor: string;
  readonly title: string;
  readonly description: string;
  readonly hint: string;
}

export interface GameGuideFlow {
  readonly id: GameGuideFlowId;
  readonly entryPath: string;
  readonly steps: readonly GameGuideStep[];
}

const START_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'start-new-game',
    flowId: 'start',
    path: ROUTES.GAME_START,
    anchor: 'game-start-new',
    title: '새로하기부터 시작',
    description: '처음 플레이를 시작할 때는 새로하기에서 새 세션을 엽니다.',
    hint: '가이드를 닫아도 이어보기가 남으니, 준비되면 강조된 버튼을 눌러 다음 화면으로 넘어가세요.',
  },
  {
    id: 'start-save-slot',
    flowId: 'start',
    path: ROUTES.GAME_SAVE,
    anchor: 'game-save-slots',
    title: '저장 슬롯 고르기',
    description: '빈 슬롯은 새 게임 시작, 진행 중인 슬롯은 이어하기에 사용합니다.',
    hint: '지금은 새로하기 기준 흐름이므로 빈 슬롯을 골라 다음 단계로 넘어가면 됩니다.',
  },
  {
    id: 'start-select-character',
    flowId: 'start',
    path: ROUTES.GAME_SELECT_CHARACTER,
    anchor: 'game-character-options',
    title: '캐릭터 선택',
    description: '플레이할 캐릭터를 고른 뒤 NEXT 버튼으로 확정합니다.',
    hint: '이 화면에서는 어떤 캐릭터로 시작할지 빠르게 결정하면 충분합니다.',
  },
  {
    id: 'start-set-nickname',
    flowId: 'start',
    path: ROUTES.GAME_SET_NICKNAME,
    anchor: 'game-nickname-input',
    title: '닉네임 입력',
    description: '닉네임을 입력해야 다음 단계로 진행할 수 있습니다.',
    hint: '가이드는 입력 규칙 전체보다 지금 눌러야 할 순서를 먼저 잡아줍니다.',
  },
  {
    id: 'start-select-method',
    flowId: 'start',
    path: ROUTES.GAME_SELECT_START_METHOD,
    anchor: 'game-start-method-options',
    title: '시작 방식 선택',
    description: '자산 연결 또는 직업 선택 중 시작 방식을 정하면 온보딩 흐름이 마무리됩니다.',
    hint: '이 단계 이후에는 실제 플레이 준비가 끝나고 메인 게임 루프로 이어집니다.',
  },
];

const MAIN_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'main-news',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-news',
    title: '뉴스부터 확인',
    description: '이달의 뉴스는 가격 변화와 행동 선택의 해석 힌트입니다.',
    hint: '뉴스를 먼저 보면 이번 턴에 무엇을 볼지 기준을 세우기 쉽습니다.',
  },
  {
    id: 'main-loan',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-loan',
    title: '대출/상환 메뉴',
    description: '현금이 부족하거나 상환 계획을 점검할 때 가장 먼저 보는 진입점입니다.',
    hint: '대출 상황을 먼저 확인하면 이후 부동산이나 소비 선택의 범위를 잡기 쉬워집니다.',
  },
  {
    id: 'main-property',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-property',
    title: '부동산 알아보기',
    description: '목표 주거와 가격 흐름을 확인하는 화면으로 이동하는 버튼입니다.',
    hint: '뉴스와 연결해서 지역이나 가격 변화를 읽는 흐름으로 이어집니다.',
  },
  {
    id: 'main-card',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-card',
    title: '카드 추천 보기',
    description: '이번 플레이에서 소비와 혜택을 조정할 카드 선택지를 확인합니다.',
    hint: '필수 행동은 아니지만, 생활비와 소비 전략을 다듬는 보조 수단입니다.',
  },
  {
    id: 'main-turn-action',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-turn-action',
    title: '이번 달 활동 진행',
    description: '뉴스와 메뉴 확인이 끝났다면 이번 달 활동을 선택하고 턴을 진행합니다.',
    hint: '가이드의 목적은 뉴스 확인에서 행동 선택과 턴 진행까지 자연스럽게 연결하는 것입니다.',
  },
];

export const GAME_GUIDE_FLOWS: Record<GameGuideFlowId, GameGuideFlow> = {
  start: {
    id: 'start',
    entryPath: ROUTES.GAME_START,
    steps: START_FLOW_STEPS,
  },
  main: {
    id: 'main',
    entryPath: ROUTES.GAME,
    steps: MAIN_FLOW_STEPS,
  },
};

export function getGuideFlow(flowId: GameGuideFlowId): GameGuideFlow {
  return GAME_GUIDE_FLOWS[flowId];
}

export function getGuideSteps(flowId: GameGuideFlowId): readonly GameGuideStep[] {
  return GAME_GUIDE_FLOWS[flowId].steps;
}

export function getGuideFlowByPath(pathname: string): GameGuideFlowId | null {
  const matchingFlow = Object.values(GAME_GUIDE_FLOWS).find((flow) =>
    flow.steps.some((step) => step.path === pathname),
  );

  return matchingFlow?.id ?? null;
}
