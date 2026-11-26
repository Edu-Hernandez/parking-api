package com.edu.parking_api.parkings.domain.model;

/**
 * Códigos de permiso que el código necesita nombrar.
 */
public final class ParkingPermissions {

    public static final String VIEW = "PARKINGS_VIEW";
    public static final String CREATE = "PARKINGS_CREATE";
    public static final String UPDATE = "PARKINGS_UPDATE";
    public static final String CHANGE_STATUS = "PARKINGS_CHANGE_STATUS";

    private ParkingPermissions() {}
}
