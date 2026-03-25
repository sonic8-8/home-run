package io.ssafy.p.j14c103.homerun.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 사용자별 SSE 연결을 관리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    private final ObjectMapper objectMapper;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(final Long userId) {
        removeEmitter(userId);

        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE 연결 종료. userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE 타임아웃. userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onError(e -> {
            log.debug("SSE 에러. userId={}", userId);
            emitters.remove(userId);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결 완료"));
        } catch (final IOException e) {
            log.warn("SSE 초기 이벤트 전송 실패. userId={}", userId, e);
            emitters.remove(userId);
        }

        return emitter;
    }

    public void send(final Long userId, final String eventName, final Object data) {
        final SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }

        try {
            final String jsonData = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(jsonData));
        } catch (final IOException e) {
            log.warn("SSE 이벤트 전송 실패. userId={}, event={}", userId, eventName, e);
            emitters.remove(userId);
        }
    }

    public void removeEmitter(final Long userId) {
        final SseEmitter existing = emitters.remove(userId);
        if (existing != null) {
            existing.complete();
        }
    }

    public boolean hasEmitter(final Long userId) {
        return emitters.containsKey(userId);
    }
}
