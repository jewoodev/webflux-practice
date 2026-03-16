package com.heri2go.chat.domain.user;

import com.heri2go.chat.validator.EnumClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EnumClass
public enum Role {
    ADMIN("ROLE_ADMIN", "관리자"),
    DENTAL("ROLE_DENTAL", "치과관계자"),
    LAB("ROLE_LAB", "기공소관계자");

    private final String key;
    private final String description;
}