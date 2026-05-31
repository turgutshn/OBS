package com.turggut.sms.iam.domain;

public enum Role {
    ADMIN,
    TEACHER,
    STUDENT;

    public String authority() {
        return "ROLE_" + name();
    }
}
