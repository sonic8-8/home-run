import { useAuthStore } from '@core/store/authStore';

export interface SseEvent {
  name: string;
  data: string;
}

type SseEventHandler = (event: SseEvent) => void;

/**
 * Authorization 헤더가 필요한 SSE 연결.
 * 브라우저 EventSource는 커스텀 헤더를 지원하지 않아 fetch + ReadableStream으로 구현.
 */
export function connectSse(
  url: string,
  onEvent: SseEventHandler,
  onError?: (e: unknown) => void,
): () => void {
  const controller = new AbortController();

  const connect = async () => {
    const { accessToken } = useAuthStore.getState();
    const baseUrl = import.meta.env.VITE_API_BASE_URL ?? '';

    try {
      const response = await fetch(`${baseUrl}${url}`, {
        headers: {
          Accept: 'text/event-stream',
          ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        },
        signal: controller.signal,
      });

      if (!response.ok || !response.body) {
        onError?.(new Error(`SSE 연결 실패: ${response.status}`));
        return;
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const messages = buffer.split('\n\n');
        buffer = messages.pop() ?? '';

        for (const message of messages) {
          const event = parseEvent(message);
          if (event) onEvent(event);
        }
      }
    } catch (e) {
      if ((e as Error).name !== 'AbortError') {
        onError?.(e);
      }
    }
  };

  connect();
  return () => controller.abort();
}

function parseEvent(raw: string): SseEvent | null {
  let name = 'message';
  let data = '';

  for (const line of raw.split('\n')) {
    if (line.startsWith('event:')) {
      name = line.slice(6).trim();
    } else if (line.startsWith('data:')) {
      data = line.slice(5).trim();
    }
  }

  if (!data) return null;
  return { name, data };
}
