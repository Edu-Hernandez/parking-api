package com.edu.parking_api.parkings.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParkingJpaRepository extends JpaRepository<ParkingJpaEntity, UUID> {

    Optional<ParkingJpaEntity> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<ParkingJpaEntity> findByCompanyId(UUID companyId, Pageable pageable);

    Page<ParkingJpaEntity> findByCompanyIdAndStatus(UUID companyId, String status, Pageable pageable);
}
