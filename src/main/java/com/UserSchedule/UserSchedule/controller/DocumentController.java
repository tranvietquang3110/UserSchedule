package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {
    @GetMapping(value = "/company-culture", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lấy thông tin công ty"
    )
    public ApiResponse<String> getCompanyCultureFromWordAsString() throws Exception {
        ClassPathResource resource = new ClassPathResource("documents/QuyDinhCongTy.docx");

        try (InputStream is = resource.getInputStream(); XWPFDocument document = new XWPFDocument(is)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder content = new StringBuilder();

            for (XWPFParagraph paragraph : paragraphs) {
                content.append(paragraph.getText()).append("\n");
            }

            return ApiResponse.<String>builder()
                    .data(content.toString().trim())
                    .message("Get data successfully").build();
        }
    }

}
