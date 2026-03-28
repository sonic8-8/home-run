export function formatMoney(amount: number): string {
  return amount.toLocaleString('ko-KR');
}

export function formatIsoDate(date: Date): string {
  return date.toLocaleDateString('sv-SE');
}

export function formatKoreanDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}년 ${month}월 ${day}일`;
}
