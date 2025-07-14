package com.UserSchedule.UserSchedule.configuration;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomAuthoritiesConverter  implements Converter<Jwt, Collection<GrantedAuthority>>{
    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> realmAccessMap = source.getClaimAsMap(REALM_ACCESS);
        Object roles = realmAccessMap.get(ROLES);
        if (roles instanceof List stringRoles){
            return ((List<String>) stringRoles)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
