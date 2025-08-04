package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.request.SendMessageToAgentRequest;
import com.UserSchedule.UserSchedule.dto.response.AIResponse;
import com.UserSchedule.UserSchedule.entity.AIConversation;
import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.enum_type.AIConversationRoleType;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.AIConversationMapper;
import com.UserSchedule.UserSchedule.repository.AIConversationRepository;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIService {
    AIConversationRepository aiConversationRepository;
    AIConversationMapper aiConversationMapper;
    UserRepository userRepository;
    @Value("${ai-agent.url}")
    @NonFinal
    String agentUrl;

    public void askAI(String message, String keycloakId, String jwt) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        int order = aiConversationRepository.countByUser(user) + 1;
        AIConversation userMessage = AIConversation.builder()
                .user(user)
                .order(order)
                .message(message)
                .role(AIConversationRoleType.USER.name())
                .build();
        aiConversationRepository.save(userMessage);
        SendMessageToAgentRequest request = SendMessageToAgentRequest.builder()
                .keycloakId(keycloakId)
                .conversation(convertResponsesToString(getAIResponseByKeycloakId(keycloakId)))
                .build();
        callToAgent(request, jwt);
    }

    public List<AIResponse> getAIResponseByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return aiConversationMapper
                .toAIResponseList(aiConversationRepository.findByUserOrderByOrderAsc(user));
    }
    public void saveAIResponse(String message, String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        int order = aiConversationRepository.countByUser(user) + 1;
        AIConversation AIResponse = AIConversation.builder()
                .user(user)
                .order(order)
                .message(message)
                .role(AIConversationRoleType.AI.name())
                .build();
        aiConversationRepository.save(AIResponse);
    }

    @Transactional
    public void deleteConversationHistory(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        aiConversationRepository.deleteByUser(user);
    }

    public String convertResponsesToString(List<AIResponse> responses) {
        return responses.stream()
                .sorted(Comparator.comparing(AIResponse::getOrder))
                .map(r -> r.getRole() + ": " + r.getMessage())
                .collect(Collectors.joining("\n"));
    }

    @Async
    public void callToAgent(SendMessageToAgentRequest request, String jwtToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<SendMessageToAgentRequest> requestEntity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(agentUrl, requestEntity, Void.class);
        } catch (Exception ex) {
            // Ghi log lỗi
            log.error("Failed to call AI agent: {}", ex.getMessage());
        }
    }
}
