package com.UserSchedule.UserSchedule.service;

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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AIService {
    AIConversationRepository aiConversationRepository;
    AIConversationMapper aiConversationMapper;
    UserRepository userRepository;
    public void askAI(String message, String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy số thứ tự message hiện tại của user
        int order = aiConversationRepository.countByUser(user) + 1;
        AIConversation userMessage = AIConversation.builder()
                .user(user)
                .order(order)
                .message(message)
                .role(AIConversationRoleType.USER.name())
                .build();
        aiConversationRepository.save(userMessage);
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
}
