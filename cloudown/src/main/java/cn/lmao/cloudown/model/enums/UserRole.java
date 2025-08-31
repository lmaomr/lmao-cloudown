package cn.lmao.cloudown.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("用户"),
    ADMIN("管理员"),
    //游客
    GUEST("游客"),
    //超级管理员
    SUPER_ADMIN("超级管理员");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }
    
}
