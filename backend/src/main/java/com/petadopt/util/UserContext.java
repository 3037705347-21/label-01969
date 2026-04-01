package com.petadopt.util;

import com.petadopt.entity.User;

public class UserContext {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    public static User getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    public static Integer getRoleType() {
        User user = getUser();
        return user != null ? user.getRoleType() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
