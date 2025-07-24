package com.UserSchedule.UserSchedule.dto.request;

import com.UserSchedule.UserSchedule.dto.response.AIResponse;
import com.UserSchedule.UserSchedule.entity.AIConversation;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SendMessageToAgentRequest {
    String keycloakId;
    String message;
    String context;
}
