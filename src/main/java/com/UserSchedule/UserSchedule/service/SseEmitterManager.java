package com.UserSchedule.UserSchedule.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    public SseEmitter createEmitter(String keycloakId) {
        SseEmitter emitter = new SseEmitter(0L); // Không timeout
        emitters.put(keycloakId, emitter);

        emitter.onCompletion(() -> emitters.remove(keycloakId));
        emitter.onTimeout(() -> emitters.remove(keycloakId));
        emitter.onError(e -> emitters.remove(keycloakId));

        return emitter;
    }

    public void send(String userId, String data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ai-message")
                        .data(data));
                System.out.println("Sending to userId: " + userId + ", data: " + data);
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}
