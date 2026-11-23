package com.edu.parking_api.members.domain.model;

import java.util.Locale;
import java.util.UUID;

/**
 * Rol de empresa tal como está en la tabla roles. Un rol privilegiado solo puede
 * asignarse, cambiarse o retirarse por quien tenga MEMBERS_MANAGE_PRIVILEGED.
 */
public record Role(
        UUID id,
        String name,
        String description,
        boolean privileged
) {

    // Los nombres de rol se guardan en mayúsculas.
    public static String normalizeName(String name) {
        return name == null ? null : name.trim().toUpperCase(Locale.ROOT);
    }
}
