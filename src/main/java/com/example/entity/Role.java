package com.example.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    HR, HeadOfDepartment, ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
