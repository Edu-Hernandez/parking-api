package com.edu.parking_api.parkings.infrastructure.persistence;

import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;

final class ParkingPersistenceMapper {

    private ParkingPersistenceMapper() {}

    static ParkingJpaEntity toJpaEntity(Parking parking) {
        ParkingJpaEntity entity = new ParkingJpaEntity();
        entity.setId(parking.getId());
        entity.setCompanyId(parking.getCompanyId());
        entity.setName(parking.getName());
        entity.setDescription(parking.getDescription());
        entity.setAddress(parking.getAddress());
        entity.setLatitude(parking.getLatitude());
        entity.setLongitude(parking.getLongitude());
        entity.setPhone(parking.getPhone());
        entity.setTimezone(parking.getTimezone());
        entity.setStatus(parking.getStatus().name());
        entity.setCreatedAt(parking.getCreatedAt());
        entity.setUpdatedAt(parking.getUpdatedAt());
        return entity;
    }

    static Parking toDomain(ParkingJpaEntity entity) {
        return Parking.reconstitute(
                entity.getId(),
                entity.getCompanyId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getPhone(),
                entity.getTimezone(),
                ParkingStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
