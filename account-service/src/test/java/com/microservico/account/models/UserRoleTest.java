package com.microservico.account.models;

import com.microservico.account.models.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserRole Entity Tests")
class UserRoleTest {

    @Test
    @DisplayName("Should create UserRole with all fields set correctly")
    void shouldCreateUserRoleWithAllFields() {
        UserRole userRole = new UserRole(10L, Role.ROLE_ADMIN);

        assertEquals(10L, userRole.getAccountId());
        assertEquals(Role.ROLE_ADMIN, userRole.getRole());
    }

    @Test
    @DisplayName("Should allow setting and getting properties")
    void shouldSetAndGetProperties() {
        UserRole userRole = new UserRole();
        userRole.setId(1L);
        userRole.setAccountId(2L);
        userRole.setRole(Role.ROLE_ADMIN);

        assertAll(
                () -> assertEquals(1L, userRole.getId()),
                () -> assertEquals(2L, userRole.getAccountId()),
                () -> assertEquals(Role.ROLE_ADMIN, userRole.getRole())
        );
    }

    @Test
    @DisplayName("Equals and hashCode should depend only on id")
    void equalsAndHashCodeShouldDependOnId() {
        UserRole u1 = new UserRole();
        u1.setId(1L);
        u1.setAccountId(5L);
        u1.setRole(Role.ROLE_ADMIN);

        UserRole u2 = new UserRole();
        u2.setId(1L);
        u2.setAccountId(99L);
        u2.setRole(Role.ROLE_ADMIN);

        assertEquals(u1, u2, "UserRole equality should depend only on id");
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    @DisplayName("Equals should return false for different ids")
    void equalsShouldReturnFalseForDifferentIds() {
        UserRole u1 = new UserRole();
        u1.setId(1L);
        UserRole u2 = new UserRole();
        u2.setId(2L);

        assertNotEquals(u1, u2);
    }

    @Test
    @DisplayName("toString should include all fields")
    void toStringShouldIncludeAllFields() {
        UserRole userRole = new UserRole();
        userRole.setId(10L);
        userRole.setAccountId(20L);
        userRole.setRole(Role.ROLE_ADMIN);

        String result = userRole.toString();

        assertAll(
                () -> assertTrue(result.contains("10")),
                () -> assertTrue(result.contains("20")),
                () -> assertTrue(result.contains("ROLE_ADMIN")),
                () -> assertTrue(result.startsWith("UserRole{"))
        );
    }

    @Test
    @DisplayName("Should handle null role gracefully")
    void shouldHandleNullRoleGracefully() {
        UserRole userRole = new UserRole();
        userRole.setAccountId(50L);
        userRole.setRole(null);

        assertNull(userRole.getRole());
        assertEquals(50L, userRole.getAccountId());
    }
}
