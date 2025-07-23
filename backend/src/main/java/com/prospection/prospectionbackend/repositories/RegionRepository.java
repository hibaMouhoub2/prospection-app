package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCode(String code);
    Boolean existsByCode(String code);

}
