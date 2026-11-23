package com.edu.parking_api.members.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompanyMemberTest {

    @Test
    void addKeepsTheGivenReferences() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        CompanyMember member = CompanyMember.add(companyId, userId, roleId);

        assertEquals(companyId, member.getCompanyId());
        assertEquals(userId, member.getUserId());
        assertEquals(roleId, member.getRoleId());
    }

    @Test
    void addRequiresAllReferences() {
        UUID id = UUID.randomUUID();

        assertThrows(NullPointerException.class, () -> CompanyMember.add(null, id, id));
        assertThrows(NullPointerException.class, () -> CompanyMember.add(id, null, id));
        assertThrows(NullPointerException.class, () -> CompanyMember.add(id, id, null));
    }

    @Test
    void changeRoleReplacesOnlyTheRole() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID newRoleId = UUID.randomUUID();
        CompanyMember member = CompanyMember.add(companyId, userId, UUID.randomUUID());

        member.changeRole(newRoleId);

        assertEquals(newRoleId, member.getRoleId());
        assertEquals(companyId, member.getCompanyId());
        assertEquals(userId, member.getUserId());
    }

    @Test
    void normalizeNameTrimsAndUppercases() {
        assertEquals("ADMIN", Role.normalizeName("  admin "));
        assertNull(Role.normalizeName(null));
    }
}
