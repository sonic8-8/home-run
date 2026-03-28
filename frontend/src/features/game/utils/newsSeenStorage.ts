import { formatIsoDate } from '@shared/utils/formatter';

const NEWS_SEEN_DATE_STORAGE_KEY_PREFIX = 'news_seen_date';

export function getNewsSeenDateStorageKey(sessionId: number | null): string | null {
  return sessionId === null ? null : `${NEWS_SEEN_DATE_STORAGE_KEY_PREFIX}_${sessionId}`;
}

export function getNewsSeenDateValue(date: Date | null): string | null {
  return date === null ? null : formatIsoDate(date);
}
