package com.santos.spring_auth.enumeration;

public enum Role {
    ADMIN,
    USER,
    OPERATOR;

    public String authority() {
        return "ROLE_" + this.name();
    }
}
