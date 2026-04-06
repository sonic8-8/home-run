import { matchPath } from 'react-router-dom';
import { ROUTES } from '@app/routes';

export type GameGuideFlowId = 'start' | 'main' | 'property' | 'propertyLoan';

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
    id: 'main-news-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-news',
    title: '뉴스로 이번 달 흐름 읽기',
    description: '이달의 뉴스는 주식, 부동산, 소비 선택을 해석하는 출발점입니다.',
    hint: '먼저 뉴스를 열고 기사 내용을 본 뒤 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-news-modal',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-news-modal',
    title: '기사에서 행동 힌트 찾기',
    description: '기사 본문과 경기 배지를 함께 보면 어떤 메뉴를 먼저 볼지 판단하기 쉬워집니다.',
    hint: '지난 뉴스 보기도 열어보면 최근 흐름을 비교할 수 있습니다. 이제 뉴스를 닫고 메뉴로 돌아가세요.',
  },
  {
    id: 'main-news-archive',
    flowId: 'main',
    path: ROUTES.GAME_NEWS_PATTERN,
    anchor: 'game-news-archive',
    title: '지난 뉴스 흐름 복기',
    description: '지난 턴 헤드라인을 보면 최근 경제 흐름이 누적해서 어떤 신호를 주는지 읽을 수 있습니다.',
    hint: '과거 헤드라인까지 확인했다면 돌아가기로 메인 화면에 복귀해 다음 메뉴를 보세요.',
  },
  {
    id: 'main-stock-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-stock-launch',
    title: '주식 투자 메뉴 열기',
    description: '주식은 시장 시세를 보고 매수·매도 주문을 넣어 다음 턴 결과를 노리는 기능입니다.',
    hint: '메뉴에서 주식 투자를 눌러 왼쪽 패널을 바꾼 뒤 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-stock-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-stock-panel',
    title: '시세와 보유 현황 확인',
    description: '시장 시세, 주문 입력, 보유 현황을 한 화면에서 확인하고 주문을 접수합니다.',
    hint: '종목을 고른 뒤 주문을 넣으면 체결 예정 턴이 기록됩니다.',
  },
  {
    id: 'main-loan-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-loan',
    title: '대출과 상환 상태 점검',
    description: '현금이 부족하거나 부동산 계획을 세울 때는 대출/상환 메뉴부터 확인합니다.',
    hint: '버튼을 눌러 왼쪽 패널에 대출 상품과 상환 정보가 보이게 하세요.',
  },
  {
    id: 'main-loan-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-loan-panel',
    title: '상품 비교와 중도 상환',
    description: '확정된 대출의 남은 원금과 월 납입금을 보고, 필요하면 새 상품을 비교하거나 상환합니다.',
    hint: '부동산 계약 전에 어떤 한도와 상환 부담을 감당할지 여기서 감을 잡습니다.',
  },
  {
    id: 'main-card-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-card',
    title: '카드 추천 열기',
    description: '카드는 소비 전략을 미세 조정하는 보조 수단으로, 추천 카드와 전체 카드를 비교할 수 있습니다.',
    hint: '카드 추천 버튼을 눌러 왼쪽 패널을 열고 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-card-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-card-panel',
    title: '추천 카드 비교',
    description: '추천, 전체, 내 카드 탭으로 카드 정보를 읽고 현재 소비 성향에 맞는 선택지를 파악합니다.',
    hint: '이번 범위에서는 조회 중심이므로 혜택과 설명을 비교해두는 정도면 충분합니다.',
  },
  {
    id: 'main-property-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-property',
    title: '부동산 화면으로 이동',
    description: '부동산 지도와 등기부등본 미니게임은 별도 화면에서 이어집니다.',
    hint: '부동산 알아보기 버튼을 누르면 `/property` 화면에서 전용 가이드가 다시 이어집니다.',
  },
  {
    id: 'main-turn-action',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-turn-action',
    title: '이번 달 활동 진행',
    description: '뉴스와 메뉴 확인이 끝났다면 이번 달에 수행할 행동을 고르고 턴을 진행합니다.',
    hint: '활동 선택 모달을 열면 스케줄 관리와 결과 확인까지 한 흐름으로 이어집니다.',
  },
  {
    id: 'main-schedule-modal',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-schedule-modal',
    title: '스케줄 3칸 채우기',
    description: '쇼핑과 활동 카드에서 3칸을 채우고, 미리보기에서 비용과 예상 변화를 확인한 뒤 턴을 확정합니다.',
    hint: '선택 단계와 미리보기 단계 모두 이 모달 안에서 이어집니다.',
  },
  {
    id: 'main-turn-result-stats',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-turn-result-stats',
    title: '자산과 스탯 변화 읽기',
    description: '턴 정산 뒤에는 현금, 대출, 부동산 자산과 함께 체력, 지식, 행복 같은 스탯 변화가 표시됩니다.',
    hint: '정산 결과를 보고 다음 달 전략을 조정하세요. 확인 뒤에는 이벤트가 이어질 수 있습니다.',
  },
  {
    id: 'main-event-modal',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-event-modal',
    title: '이벤트 선택 처리',
    description: '턴 정산 뒤 발생하는 이벤트는 선택지에 따라 자산, 경력, 다음 턴 상황이 달라집니다.',
    hint: '통화, 편지, 선물, 이직 제안 같은 이벤트를 여기서 처리합니다.',
  },
];

const PROPERTY_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'property-map-stage',
    flowId: 'property',
    path: ROUTES.REAL_ESTATE_PATTERN,
    anchor: 'property-map-stage',
    title: '지역과 구역부터 좁혀 보기',
    description: '부동산 화면은 지역 선택에서 시작해 구 단위 지도와 실제 매물 목록까지 단계적으로 좁혀집니다.',
    hint: '오른쪽 안내 카드의 현재 단계와 배지를 보면 지금 어디를 보고 있는지 바로 파악할 수 있습니다.',
  },
  {
    id: 'property-detail-panel',
    flowId: 'property',
    path: ROUTES.REAL_ESTATE_PATTERN,
    anchor: 'property-detail-panel',
    title: '매물 상세와 다음 행동',
    description: '마커나 카드 목록을 누르면 상세 패널에서 가격, 면적, 대출 신청, 구매하기 같은 다음 행동을 고를 수 있습니다.',
    hint: '대출 신청은 대출 심사 흐름으로, 구매하기는 등기부등본 미니게임으로 이어집니다.',
  },
  {
    id: 'property-registry-modal',
    flowId: 'property',
    path: ROUTES.REAL_ESTATE_PATTERN,
    anchor: 'property-registry-modal',
    title: '등기부등본 미니게임',
    description: '체크리스트로 위험 요소를 골라 제출하면 계약 판정과 해설이 나오는 미니게임입니다.',
    hint: '갑구와 을구를 읽고 체크한 뒤 검토 제출로 결과를 확인하세요.',
  },
];

const PROPERTY_LOAN_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'property-loan-review',
    flowId: 'propertyLoan',
    path: ROUTES.REAL_ESTATE_PATTERN,
    anchor: 'property-loan-review',
    title: '대출 심사 결과 읽기',
    description: '심사 모달에서는 최대 가능 금액과 심사 결과를 보고 실제 계약 단계로 넘어갈 수 있습니다.',
    hint: '가능 금액을 확인한 뒤 부동산 계약하러 가기를 눌러 다음 모달로 이동하세요.',
  },
  {
    id: 'property-loan-confirm',
    flowId: 'propertyLoan',
    path: ROUTES.REAL_ESTATE_PATTERN,
    anchor: 'property-loan-confirm',
    title: '대출 신청 금액 확정',
    description: '최대 한도 안에서 실제 신청 금액을 입력하고 계약서를 확인한 뒤 확정합니다.',
    hint: '신청이 끝나면 게임 메인으로 돌아가 확정된 대출이 반영됩니다.',
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
  property: {
    id: 'property',
    entryPath: ROUTES.REAL_ESTATE,
    steps: PROPERTY_FLOW_STEPS,
  },
  propertyLoan: {
    id: 'propertyLoan',
    entryPath: ROUTES.REAL_ESTATE_LOAN_APPLY,
    steps: PROPERTY_LOAN_FLOW_STEPS,
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
    flow.steps.some((step) => matchesGuideStepPath(step, pathname)),
  );

  return matchingFlow?.id ?? null;
}

export function matchesGuideStepPath(step: GameGuideStep, pathname: string): boolean {
  return matchPath(
    {
      path: step.path,
      end: true,
    },
    pathname,
  ) !== null;
}
