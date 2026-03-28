const MAN_WON = 10_000;
const EOK_MAN = 10_000;
const EOK_WON = MAN_WON * EOK_MAN;

const normalizeAmount = (value: number) => Math.max(0, Math.round(Math.abs(value)));

export const stripNonDigits = (value: string) => value.replace(/[^0-9]/g, '');

export const formatNumericInput = (value: string) => {
  const digits = stripNonDigits(value);
  if (!digits) {
    return '';
  }
  return Number(digits).toLocaleString('ko-KR');
};

export const parseNumericInput = (value: string) => {
  const digits = stripNonDigits(value);
  if (!digits) {
    return 0;
  }
  return Number(digits);
};

export const formatWon = (value: number) => `${Math.round(value).toLocaleString('ko-KR')}원`;

export const formatKoreanCompactManWon = (value: number) => {
  const absoluteValue = normalizeAmount(value);
  const totalMan = Math.round(absoluteValue / MAN_WON);

  if (absoluteValue < EOK_WON) {
    return `${totalMan.toLocaleString('ko-KR')}만원`;
  }

  const eok = Math.floor(totalMan / EOK_MAN);
  const man = totalMan % EOK_MAN;

  if (man === 0) {
    return `${eok.toLocaleString('ko-KR')}억원`;
  }

  return `${eok.toLocaleString('ko-KR')}억 ${man.toLocaleString('ko-KR')}만원`;
};
