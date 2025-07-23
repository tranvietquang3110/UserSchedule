package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.AiMessageRequest;
import com.UserSchedule.UserSchedule.dto.response.AIResponse;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.service.AIService;
import com.UserSchedule.UserSchedule.service.SseEmitterManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/ai-agent")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI Agent SSE", description = "Streaming AI responses via SSE to user")
public class AIAgentController {
    SseEmitterManager emitterManager;
    AIService aiConversationService;
    @Operation(summary = "Kết nối với SSE", description = "Client mở kết nối SSE để nhận phản hồi từ AI Agent theo userId.")
    @GetMapping("/sse/connect/{keycloakId}")
    public SseEmitter connect(
            @Parameter(description = "ID của người dùng", example = "123") @PathVariable String keycloakId) {
        return emitterManager.createEmitter(keycloakId);
    }

    @Operation(summary = "Gửi dữ liệu từ AI về cho user", description = "AI service gọi endpoint này để gửi thông điệp về client thông qua SSE.")
    @PostMapping("/sse/callback")
    public ResponseEntity<?> handleAICallback(
            @Parameter(description = "ID của người dùng nhận thông điệp", example = "123") @RequestParam String keycloakId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nội dung phản hồi từ AI",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AiMessageRequest.class))
            )
            @RequestBody AiMessageRequest aiMessage) {
        emitterManager.send(keycloakId, aiMessage.getAiMessage());
        aiConversationService.saveAIResponse(aiMessage.getAiMessage(), keycloakId);
        return ResponseEntity.ok("Sent to user via SSE");
    }


    @Operation(summary = "Gửi câu hỏi cho AI", description = "Người dùng gửi một câu hỏi để lưu lại và bắt đầu cuộc hội thoại với AI.")
    @PostMapping("/conversation/ask")
    public ResponseEntity<?> askAI(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông điệp người dùng gửi cho AI",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AiMessageRequest.class))
            )
            @RequestBody AiMessageRequest request,
            @Parameter(description = "ID của người dùng", example = "123") @RequestParam String keycloakId,
            HttpServletRequest httpRequest
    ) {
        String bearerToken = httpRequest.getHeader("Authorization");
        String jwt = null;
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            jwt = bearerToken.substring(7);
        }
        aiConversationService.askAI(request.getAiMessage(), keycloakId, jwt);
        return ResponseEntity.ok("Message saved");
    }

    @Operation(summary = "Lấy lịch sử hội thoại AI", description = "Lấy tất cả câu hỏi và phản hồi theo thứ tự cho một người dùng.")
    @GetMapping("/conversation/history")
    public ApiResponse<List<AIResponse>> getConversationHistory(
            @Parameter(description = "ID của người dùng", example = "123") @RequestParam String keycloakId
    ) {
        return ApiResponse.<List<AIResponse>>builder()
                .message("Get ai conversation history successfully!")
                .data(aiConversationService.getAIResponseByKeycloakId(keycloakId))
                .build();
    }

    @DeleteMapping("/conversation/{keycloakId}")
    public ApiResponse deleteConversation(@PathVariable String keycloakId) {
        aiConversationService.deleteConversationHistory(keycloakId);
        return ApiResponse.builder().message("Delete conversation successfully").build();
    }
}
