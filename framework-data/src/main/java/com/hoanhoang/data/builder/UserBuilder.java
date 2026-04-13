package com.hoanhoang.data.builder;

import com.hoanhoang.data.model.UserDto;

public class UserBuilder {
    private String username;
    private String password;
    private String role;

    public UserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder withRole(String role) {
        this.role = role;
        return this;
    }

    public UserDto build() {
        UserDto user = new UserDto();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}