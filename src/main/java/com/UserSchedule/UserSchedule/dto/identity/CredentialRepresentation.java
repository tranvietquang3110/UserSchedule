package com.UserSchedule.UserSchedule.dto.identity;

import lombok.Builder;
import lombok.Data;
import org.hibernate.grammars.hql.HqlParser;

@Data
@Builder
public class CredentialRepresentation {
    String type = "password";
    String value;
    boolean temporary = false;
}
