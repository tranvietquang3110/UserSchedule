package com.UserSchedule.UserSchedule.dto.identity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenExchangeResponse {
     String accessToken;
     String tokenType;
     int expiresIn;
     int refreshExpiresIn;
     String refreshToken;
     String scope;
}
