package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Supervision;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisionRepository {
    Optional<Supervision> findByCode(String code);
    boolean existsByCode(String code);
    List<Supervision> findByRegionId(Long regionId);
}
