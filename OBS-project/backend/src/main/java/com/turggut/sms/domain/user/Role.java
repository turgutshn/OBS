package com.turggut.sms.domain.user;

public enum Role {
    ADMIN,
    TEACHER,
    STUDENT;

    public String authority() {
        return "ROLE_" + name();
    }
}
