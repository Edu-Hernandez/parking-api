package com.edu.parking_api.parkings.infrastructure.persistence;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ParkingRepositoryAdapter implements ParkingRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ParkingJpaRepository parkingJpaRepository;

    public ParkingRepositoryAdapter(ParkingJpaRepository parkingJpaRepository) {
        this.parkingJpaRepository = parkingJpaRepository;
    }

    @Override
    public Parking save(Parking parking) {
        ParkingJpaEntity saved = parkingJpaRepository.save(ParkingPersistenceMapper.toJpaEntity(parking));
        return ParkingPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Parking> findByIdAndCompanyId(UUID id, UUID companyId) {
        return parkingJpaRepository.findByIdAndCompanyId(id, companyId)
                .map(ParkingPersistenceMapper::toDomain);
    }

    @Override
    public PageResult<Parking> findByCompanyId(UUID companyId, ParkingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);
        Page<ParkingJpaEntity> result = status == null
                ? parkingJpaRepository.findByCompanyId(companyId, pageable)
                : parkingJpaRepository.findByCompanyIdAndStatus(companyId, status.name(), pageable);

        return new PageResult<>(
                result.getContent().stream().map(ParkingPersistenceMapper::toDomain).toList(),
                page,
                size,
                result.getTotalElements()
        );
    }
}
