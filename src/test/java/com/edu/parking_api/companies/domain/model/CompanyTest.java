package com.edu.parking_api.companies.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    @Test
    void registerCreatesActiveCompanyWithNormalizedData() {
        Company company = Company.register("  Cocheras SA  ", "  20123456789 ", "  Info@Cocheras.COM ", " 999 ");

        assertEquals("Cocheras SA", company.getBusinessName());
        assertEquals("20123456789", company.getTaxId());
        assertEquals("info@cocheras.com", company.getEmail());
        assertEquals("999", company.getPhone());
        assertEquals(CompanyStatus.ACTIVE, company.getStatus());
    }

    @Test
    void registerTurnsBlankOptionalFieldsIntoNull() {
        Company company = Company.register("Cocheras SA", "   ", "", "  ");

        assertNull(company.getTaxId());
        assertNull(company.getEmail());
        assertNull(company.getPhone());
    }

    @Test
    void registerRejectsMissingOrBlankBusinessName() {
        assertThrows(NullPointerException.class, () -> Company.register(null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> Company.register("   ", null, null, null));
    }

    @Test
    void updateDetailsReplacesAllEditableFieldsAndClearsOptionalOnes() {
        Company company = Company.register("Cocheras SA", "123", "a@b.com", "111");

        company.updateDetails("Nueva SA", null, null, null);

        assertEquals("Nueva SA", company.getBusinessName());
        assertNull(company.getTaxId());
        assertNull(company.getEmail());
        assertNull(company.getPhone());
        assertEquals(CompanyStatus.ACTIVE, company.getStatus());
    }

    @Test
    void deactivateIsIdempotent() {
        Company company = Company.register("Cocheras SA", null, null, null);

        company.deactivate();
        company.deactivate();

        assertEquals(CompanyStatus.INACTIVE, company.getStatus());
    }

    @Test
    void updateDetailsKeepsInactiveStatus() {
        Company company = Company.register("Cocheras SA", null, null, null);
        company.deactivate();

        company.updateDetails("Otra SA", null, null, null);

        assertEquals(CompanyStatus.INACTIVE, company.getStatus());
    }
}
