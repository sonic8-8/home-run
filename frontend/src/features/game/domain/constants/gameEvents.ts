import type { GameEvent } from '../entities/GameEvent';
import type { GameEventId } from '../entities/GameEvent';

export const GAME_EVENTS: Record<GameEventId, GameEvent> = {
  SANTA_GIFT: {
    id: 'SANTA_GIFT',
    title: '착한아이에게는 선물을',
    description: '산타 할아버지는 우는아이에게 선물을 안주신대요.\n한해동안 착실하게 보낸 당신\n산타할아버지의 깜짝 선물이 도착했어요.',
    imageSrc: '/assets/images/santa-gift.png',
    cardColor: '#f5f0e8',
    buttons: [
      { label: '수령하기', variant: 'primary', actionId: 'RECEIVE' },
    ],
  },

  GOVERNMENT_SUBSIDY: {
    id: 'GOVERNMENT_SUBSIDY',
    title: '호외요 호외',
    description: '정부에서 3차 상생지원금을 지원한다고 합니다.\n링크를 타고 가서 신청만 하면\n소득분위 상관없이 한 명당 300,000원 지원!',
    imageSrc: '/assets/images/government-subsidy.png',
    cardColor: '#eaf0fb',
    buttons: [
      { label: '신청하기', variant: 'primary',   actionId: 'APPLY'  },
      { label: '거절하기', variant: 'secondary', actionId: 'REJECT' },
    ],
  },

  STOCK_CRASH: {
    id: 'STOCK_CRASH',
    title: '주식 폭락',
    description: '주식이 폭락했습니다.',
    imageSrc: '/assets/images/stock-crash.png',
    cardColor: '#fff5f5',
    buttons: [
      { label: '확인', variant: 'primary', actionId: 'CONFIRM' },
    ],
  },

  BONUS_SALARY: {
    id: 'BONUS_SALARY',
    title: '보너스 급여',
    description: '보너스 급여를 받았습니다.',
    imageSrc: '/assets/images/bonus-salary.png',
    cardColor: '#f0fdf4',
    buttons: [
      { label: '수령하기', variant: 'primary', actionId: 'RECEIVE' },
    ],
  },

  PROPERTY_TAX: {
    id: 'PROPERTY_TAX',
    title: '재산세 고지',
    description: '재산세 고지서가 도착했습니다.',
    imageSrc: '/assets/images/property-tax.png',
    cardColor: '#fffaeb',
    buttons: [
      { label: '납부하기', variant: 'primary', actionId: 'PAY' },
    ],
  },
};
