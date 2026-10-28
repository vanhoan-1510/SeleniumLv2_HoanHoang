package com.hoanhoang.data.model;

/**
 * Login account mapped by Gson from local credentials or the fictional test fixture. The password
 * never appears in toString().
 */
public final class UserDto {
    private final String username;
    private final String password;

    public UserDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "UserDto[username=" + username + ", password=REDACTED]";
    }
}
