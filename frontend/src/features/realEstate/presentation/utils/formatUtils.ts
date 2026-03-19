/** 가격(원)을 억/만 단위 문자열로 변환. amountInWon: 원 단위 */
export function formatPriceWon(amountInWon: number): string {
  const man = Math.round(amountInWon / 10000);
  return formatPrice(man);
}

/** 거래금액(만원)을 억 단위 문자열로 변환 */
export function formatPrice(amountInMan: number): string {
  if (amountInMan >= 10000) {
    const eok = Math.floor(amountInMan / 10000);
    const remainder = amountInMan % 10000;
    if (remainder === 0) return `${eok}억`;
    return `${eok}.${Math.round(remainder / 1000)}억`;
  }
  return `${amountInMan.toLocaleString()}만`;
}

/** ㎡ → 평 변환 */
export function toSquarePyeong(areaM2: number): number {
  return Math.round(areaM2 / 3.306);
}
