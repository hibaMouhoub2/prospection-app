package com.prospection.prospectionbackend.repositories;

import com.prospection.prospectionbackend.entities.Branche;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrancheRepository {
    Optional<Branche> findByCode(String code);
    boolean existsByCode(String code);
    List<Branche> findBySupervisionId(Long supervisionId);
}
