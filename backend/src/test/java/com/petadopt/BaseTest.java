package com.petadopt;

import com.petadopt.entity.User;
import com.petadopt.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class BaseTest {

    protected static final Long ADMIN_USER_ID = 1L;
    protected static final Integer ADMIN_ROLE = 1;
    protected static final Long RESCUER_USER_ID = 2L;
    protected static final Integer RESCUER_ROLE = 2;
    protected static final Long ADOPTER_USER_ID = 3L;
    protected static final Integer ADOPTER_ROLE = 3;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    protected void setAdminContext() {
        User user = new User();
        user.setId(ADMIN_USER_ID);
        user.setUsername("admin");
        user.setRoleType(ADMIN_ROLE);
        UserContext.setUser(user);
    }

    protected void setRescuerContext() {
        User user = new User();
        user.setId(RESCUER_USER_ID);
        user.setUsername("rescuer");
        user.setRoleType(RESCUER_ROLE);
        UserContext.setUser(user);
    }

    protected void setAdopterContext() {
        User user = new User();
        user.setId(ADOPTER_USER_ID);
        user.setUsername("adopter");
        user.setRoleType(ADOPTER_ROLE);
        UserContext.setUser(user);
    }

    protected void setCustomContext(Long userId, String username, Integer roleType) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setRoleType(roleType);
        UserContext.setUser(user);
    }
}
