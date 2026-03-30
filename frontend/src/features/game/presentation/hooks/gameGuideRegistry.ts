import { matchPath } from 'react-router-dom';
import { ROUTES } from '@app/routes';

export type GameGuideFlowId = 'start' | 'main' | 'property' | 'propertyLoan';

export interface GameGuideStep {
  readonly id: string;
  readonly flowId: GameGuideFlowId;
  readonly path: string;
  readonly anchor: string;
  readonly targetLabel: string;
  readonly title: string;
  readonly description: string;
  readonly hint: string;
  readonly missingDescription?: string;
  readonly allowAdvanceWithoutTarget?: boolean;
}

export interface GameGuideFlow {
  readonly id: GameGuideFlowId;
  readonly entryPath: string;
  readonly label: string;
  readonly launcherEyebrow: string;
  readonly launcherTitle: string;
  readonly launcherDescription: string;
  readonly steps: readonly GameGuideStep[];
}

const START_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'start-new-game',
    flowId: 'start',
    path: ROUTES.GAME_START,
    anchor: 'game-start-new',
    targetLabel: '새로하기 버튼',
    title: '새로하기부터 시작',
    description: '처음 플레이를 시작할 때는 새로하기에서 새 세션을 엽니다.',
    hint: '가이드를 닫아도 이어보기가 남으니, 준비되면 강조된 버튼을 눌러 다음 화면으로 넘어가세요.',
  },
  {
    id: 'start-save-slot',
    flowId: 'start',
    path: ROUTES.GAME_SAVE,
    anchor: 'game-save-slots',
    targetLabel: '저장 슬롯 목록',
    title: '저장 슬롯 고르기',
    description: '빈 슬롯은 새 게임 시작, 진행 중인 슬롯은 이어하기에 사용합니다.',
    hint: '지금은 새로하기 기준 흐름이므로 빈 슬롯을 골라 다음 단계로 넘어가면 됩니다.',
  },
  {
    id: 'start-select-character',
    flowId: 'start',
    path: ROUTES.GAME_SELECT_CHARACTER,
    anchor: 'game-character-options',
    targetLabel: '캐릭터 선택 영역',
    title: '캐릭터 선택',
    description: '플레이할 캐릭터를 고른 뒤 NEXT 버튼으로 확정합니다.',
    hint: '이 화면에서는 어떤 캐릭터로 시작할지 빠르게 결정하면 충분합니다.',
  },
  {
    id: 'start-set-nickname',
    flowId: 'start',
    path: ROUTES.GAME_SET_NICKNAME,
    anchor: 'game-nickname-input',
    targetLabel: '닉네임 입력칸',
    title: '닉네임 입력',
    description: '닉네임을 입력해야 다음 단계로 진행할 수 있습니다.',
    hint: '가이드는 입력 규칙 전체보다 지금 눌러야 할 순서를 먼저 잡아줍니다.',
  },
  {
    id: 'start-select-method',
    flowId: 'start',
    path: ROUTES.GAME_SELECT_START_METHOD,
    anchor: 'game-start-method-options',
    targetLabel: '시작 방식 선택 카드',
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
    targetLabel: '이달의 뉴스 버튼',
    title: '뉴스로 이번 달 흐름 읽기',
    description: '이달의 뉴스는 주식, 부동산, 소비 선택을 해석하는 출발점입니다.',
    hint: '먼저 뉴스를 열고 기사 내용을 본 뒤 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-news-modal',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-news-modal',
    targetLabel: '이달의 뉴스 창',
    title: '기사에서 행동 힌트 찾기',
    description: '기사 본문과 경기 배지를 함께 보면 어떤 메뉴를 먼저 볼지 판단하기 쉬워집니다.',
    hint: '지난 뉴스 보기도 열어보면 최근 흐름을 비교할 수 있습니다. 이제 뉴스를 닫고 메뉴로 돌아가세요.',
    missingDescription: '먼저 메인 화면에서 이달의 뉴스를 열어주세요. 뉴스 창이 열리면 강조가 바로 붙습니다.',
  },
  {
    id: 'main-news-archive',
    flowId: 'main',
    path: ROUTES.GAME_NEWS_PATTERN,
    anchor: 'game-news-archive',
    targetLabel: '지난 턴 헤드라인 영역',
    title: '지난 뉴스 흐름 복기',
    description: '지난 턴 헤드라인을 보면 최근 경제 흐름이 누적해서 어떤 신호를 주는지 읽을 수 있습니다.',
    hint: '과거 헤드라인까지 확인했다면 돌아가기로 메인 화면에 복귀해 다음 메뉴를 보세요.',
  },
  {
    id: 'main-status-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-status',
    targetLabel: '자산/스탯 보기 버튼',
    title: '자산/스탯 리포트 열기',
    description: '메인 화면에서는 최근 정산 기준 자산과 최근 턴 스탯 변화를 별도 패널로 모아 볼 수 있습니다.',
    hint: '자산/스탯 보기 버튼을 눌러 왼쪽 리포트 패널을 연 뒤 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-status-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-status-panel',
    targetLabel: '자산/스탯 리포트 패널',
    title: '최근 정산 기준으로 읽기',
    description: '이 패널은 최근 정산이 끝난 시점의 자산 요약과 최근 턴 스탯 변화를 한 번에 보여줍니다.',
    hint: '순자산과 대출 규모를 먼저 보고, 아래 스탯 변화로 다음 달 활동 방향을 잡으면 됩니다.',
    missingDescription: '먼저 메인 화면에서 자산/스탯 보기 버튼을 눌러 리포트 패널을 열어주세요.',
  },
  {
    id: 'main-stock-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-stock-launch',
    targetLabel: '주식 투자 버튼',
    title: '주식 투자 메뉴 열기',
    description: '주식은 시장 시세를 보고 매수·매도 주문을 넣어 다음 턴 결과를 노리는 기능입니다.',
    hint: '메뉴에서 주식 투자를 눌러 왼쪽 패널을 바꾼 뒤 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-stock-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-stock-panel',
    targetLabel: '주식 패널',
    title: '시세와 보유 현황 확인',
    description: '시장 시세, 주문 입력, 보유 현황을 한 화면에서 확인하고 주문을 접수합니다.',
    hint: '종목을 고른 뒤 주문을 넣으면 체결 예정 턴이 기록됩니다.',
    missingDescription: '먼저 메인 화면에서 주식 투자 메뉴를 열어주세요. 왼쪽 패널이 바뀌면 이 단계가 이어집니다.',
  },
  {
    id: 'main-loan-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-loan',
    targetLabel: '대출/상환 버튼',
    title: '대출과 상환 상태 점검',
    description: '현금이 부족하거나 부동산 계획을 세울 때는 대출/상환 메뉴부터 확인합니다.',
    hint: '버튼을 눌러 왼쪽 패널에 대출 상품과 상환 정보가 보이게 하세요.',
  },
  {
    id: 'main-loan-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-loan-panel',
    targetLabel: '대출 상품 패널',
    title: '상품 비교와 중도 상환',
    description: '확정된 대출의 남은 원금과 월 납입금을 보고, 필요하면 새 상품을 비교하거나 상환합니다.',
    hint: '부동산 계약 전에 어떤 한도와 상환 부담을 감당할지 여기서 감을 잡습니다.',
    missingDescription: '먼저 메인 화면에서 대출/상환 메뉴를 열어주세요. 대출 패널이 보이면 강조가 이동합니다.',
  },
  {
    id: 'main-card-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-card',
    targetLabel: '카드 추천 버튼',
    title: '카드 추천 열기',
    description: '카드는 소비 전략을 미세 조정하는 보조 수단으로, 추천 카드와 전체 카드를 비교할 수 있습니다.',
    hint: '카드 추천 버튼을 눌러 왼쪽 패널을 열고 다음 단계로 넘어가세요.',
  },
  {
    id: 'main-card-panel',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-card-panel',
    targetLabel: '카드 추천 패널',
    title: '추천 카드 비교',
    description: '추천, 전체, 내 카드 탭으로 카드 정보를 읽고 현재 소비 성향에 맞는 선택지를 파악합니다.',
    hint: '이번 범위에서는 조회 중심이므로 혜택과 설명을 비교해두는 정도면 충분합니다.',
    missingDescription: '먼저 메인 화면에서 카드 추천 메뉴를 열어주세요. 카드 패널이 열리면 이어서 설명합니다.',
  },
  {
    id: 'main-property-launch',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-property',
    targetLabel: '부동산 알아보기 버튼',
    title: '부동산 화면으로 이동',
    description: '부동산 지도와 등기부등본 미니게임은 별도 화면에서 이어집니다.',
    hint: '부동산 알아보기 버튼을 누르면 `/property` 화면에서 전용 가이드가 다시 이어집니다.',
  },
  {
    id: 'main-turn-action',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-turn-action',
    targetLabel: '이번 달 활동 진행 버튼',
    title: '이번 달 활동 진행',
    description: '뉴스와 메뉴 확인이 끝났다면 이번 달에 수행할 행동을 고르고 턴을 진행합니다.',
    hint: '활동 선택 모달을 열면 스케줄 관리와 결과 확인까지 한 흐름으로 이어집니다.',
  },
  {
    id: 'main-schedule-modal',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-schedule-modal',
    targetLabel: '스케줄 관리 모달',
    title: '스케줄 3칸 채우기',
    description: '쇼핑과 활동 카드에서 3칸을 채우고, 미리보기에서 비용과 예상 변화를 확인한 뒤 턴을 확정합니다.',
    hint: '선택 단계와 미리보기 단계 모두 이 모달 안에서 이어집니다.',
    missingDescription: '먼저 이번 달 활동 진행을 눌러 스케줄 모달을 열어주세요. 모달이 열리면 강조가 붙습니다.',
  },
  {
    id: 'main-turn-result-stats',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-turn-result-stats',
    targetLabel: '턴 정산 결과의 자산·스탯 영역',
    title: '자산과 스탯 변화 읽기',
    description: '턴 정산 뒤에는 현금, 대출, 부동산 자산과 함께 체력, 지식, 행복 같은 스탯 변화가 표시됩니다.',
    hint: '정산 결과를 보고 다음 달 전략을 조정하세요. 확인 뒤에는 이벤트가 이어질 수 있습니다.',
    missingDescription: '먼저 이번 달 활동을 확정해 턴 정산 결과를 열어주세요. 결과 카드가 열리면 이 단계가 활성화됩니다.',
  },
  {
    id: 'main-event-modal',
    flowId: 'main',
    path: ROUTES.GAME,
    anchor: 'game-event-modal',
    targetLabel: '이벤트 선택 모달',
    title: '이벤트 선택 처리',
    description: '턴 정산 뒤 발생하는 이벤트는 선택지에 따라 자산, 경력, 다음 턴 상황이 달라집니다.',
    hint: '통화, 편지, 선물, 이직 제안 같은 이벤트를 여기서 처리합니다.',
    missingDescription: '턴 정산 뒤 이벤트가 발생하면 이 단계가 이어집니다. 아직 이벤트가 없다면 다음 턴에서 다시 볼 수 있습니다.',
    allowAdvanceWithoutTarget: true,
  },
];

const PROPERTY_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'property-map-stage',
    flowId: 'property',
    path: ROUTES.REAL_ESTATE,
    anchor: 'property-map-interaction',
    targetLabel: '부동산 지도 영역',
    title: '지역과 구역부터 좁혀 보기',
    description: '부동산 화면은 지역 선택에서 시작해 구 단위 지도와 실제 매물 목록까지 단계적으로 좁혀집니다.',
    hint: '오른쪽 안내 카드의 현재 단계와 배지를 보면 지금 어디를 보고 있는지 바로 파악할 수 있습니다.',
  },
  {
    id: 'property-detail-panel',
    flowId: 'property',
    path: ROUTES.REAL_ESTATE,
    anchor: 'property-detail-actions',
    targetLabel: '매물 상세 패널의 행동 버튼',
    title: '매물 상세와 다음 행동',
    description: '마커나 카드 목록을 누르면 상세 패널에서 가격, 면적, 대출 신청, 구매하기 같은 다음 행동을 고를 수 있습니다.',
    hint: '대출 신청은 대출 심사 흐름으로, 구매하기는 등기부등본 미니게임으로 이어집니다.',
    missingDescription: '먼저 지도 마커나 매물 카드를 눌러 상세 패널을 열어주세요. 행동 버튼이 보여야 다음 단계로 넘어갈 수 있습니다.',
  },
  {
    id: 'property-registry-modal',
    flowId: 'property',
    path: ROUTES.REAL_ESTATE,
    anchor: 'property-registry-checklist',
    targetLabel: '등기부등본 체크리스트',
    title: '등기부등본 미니게임',
    description: '체크리스트로 위험 요소를 골라 제출하면 계약 판정과 해설이 나오는 미니게임입니다.',
    hint: '갑구와 을구를 읽고 체크한 뒤 검토 제출로 결과를 확인하세요.',
    missingDescription: '먼저 매물 상세에서 구매하기를 눌러 등기부등본 미니게임을 열어주세요.',
  },
];

const PROPERTY_LOAN_FLOW_STEPS: readonly GameGuideStep[] = [
  {
    id: 'property-loan-review-action',
    flowId: 'propertyLoan',
    path: ROUTES.REAL_ESTATE,
    anchor: 'property-loan-review-action',
    targetLabel: '부동산 계약하러 가기 버튼',
    title: '대출 심사 결과 읽기',
    description: '심사 모달에서는 최대 가능 금액과 심사 결과를 보고 실제 계약 단계로 넘어갈 수 있습니다.',
    hint: '가능 금액을 확인한 뒤 부동산 계약하러 가기를 눌러 다음 모달로 이동하세요.',
    missingDescription: '먼저 매물 상세에서 대출 신청을 진행해 심사 결과 모달을 열어주세요.',
  },
  {
    id: 'property-loan-confirm-amount',
    flowId: 'propertyLoan',
    path: ROUTES.REAL_ESTATE,
    anchor: 'property-loan-confirm-amount',
    targetLabel: '대출 신청 금액 입력칸',
    title: '대출 신청 금액 확정',
    description: '최대 한도 안에서 실제 신청 금액을 입력하고 계약서를 확인한 뒤 확정합니다.',
    hint: '가능 금액 안에서 신청 금액을 먼저 입력한 뒤 마지막 확인 버튼으로 제출하면 됩니다.',
    missingDescription: '먼저 심사 결과 모달에서 부동산 계약하러 가기를 눌러 신청 금액 입력 단계로 이동하세요.',
  },
  {
    id: 'property-loan-confirm-action',
    flowId: 'propertyLoan',
    path: ROUTES.REAL_ESTATE,
    anchor: 'property-loan-confirm-action',
    targetLabel: '대출 신청 확정 버튼',
    title: '신청 금액 제출',
    description: '금액 입력이 끝나면 확정 버튼으로 신청을 제출하고 메인 화면으로 돌아갑니다.',
    hint: '제출이 끝나면 확정된 대출이 메인 게임에 반영됩니다.',
    missingDescription: '먼저 대출 신청 금액을 입력해 확인 버튼을 활성화해주세요.',
  },
];

export const GAME_GUIDE_FLOWS: Record<GameGuideFlowId, GameGuideFlow> = {
  start: {
    id: 'start',
    entryPath: ROUTES.GAME_START,
    label: '온보딩 가이드',
    launcherEyebrow: '첫 플레이',
    launcherTitle: '캐릭터 생성 흐름을 짧게 안내합니다',
    launcherDescription: '새로하기부터 저장 슬롯, 캐릭터, 닉네임, 시작 방식까지 실제 버튼 순서대로 따라갑니다.',
    steps: START_FLOW_STEPS,
  },
  main: {
    id: 'main',
    entryPath: ROUTES.GAME,
    label: '메인 루프 가이드',
    launcherEyebrow: '메인 화면',
    launcherTitle: '이번 달 플레이 루프를 화면 위에서 안내합니다',
    launcherDescription: '뉴스 확인, 투자 메뉴, 스케줄 진행, 결과 읽기까지 지금 화면에서 바로 이어서 볼 수 있습니다.',
    steps: MAIN_FLOW_STEPS,
  },
  property: {
    id: 'property',
    entryPath: ROUTES.REAL_ESTATE,
    label: '부동산 탐색 가이드',
    launcherEyebrow: '부동산 화면',
    launcherTitle: '지도 탐색과 등기부등본 흐름을 따라갑니다',
    launcherDescription: '지역 선택, 매물 상세, 등기부등본 미니게임까지 부동산 전용 동선을 짧게 보여줍니다.',
    steps: PROPERTY_FLOW_STEPS,
  },
  propertyLoan: {
    id: 'propertyLoan',
    entryPath: ROUTES.REAL_ESTATE,
    label: '부동산 대출 가이드',
    launcherEyebrow: '대출 심사',
    launcherTitle: '대출 심사와 신청 단계를 순서대로 안내합니다',
    launcherDescription: '심사 결과 확인부터 신청 금액 입력, 최종 제출까지 부동산 계약 직전 흐름을 이어서 봅니다.',
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
