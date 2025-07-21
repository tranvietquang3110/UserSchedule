package com.UserSchedule.UserSchedule.mapper;

import com.UserSchedule.UserSchedule.dto.response.AIResponse;
import com.UserSchedule.UserSchedule.entity.AIConversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AIConversationMapper {
    AIResponse toAIResponse(AIConversation conversation);

    List<AIResponse> toAIResponseList(List<AIConversation> conversations);
}