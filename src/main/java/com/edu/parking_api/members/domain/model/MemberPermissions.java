package com.edu.parking_api.members.domain.model;

/**
 * Códigos de permiso que el código necesita nombrar.
 */
public final class MemberPermissions {

    public static final String VIEW = "MEMBERS_VIEW";
    public static final String ADD = "MEMBERS_ADD";
    public static final String CHANGE_ROLE = "MEMBERS_CHANGE_ROLE";
    public static final String REMOVE = "MEMBERS_REMOVE";
    public static final String MANAGE_PRIVILEGED = "MEMBERS_MANAGE_PRIVILEGED";

    private MemberPermissions() {}
}
