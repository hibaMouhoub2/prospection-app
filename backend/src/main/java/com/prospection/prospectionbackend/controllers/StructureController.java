package com.prospection.prospectionbackend.controllers;

import com.prospection.prospectionbackend.entities.Region;
import com.prospection.prospectionbackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/structure")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class StructureController {

    @Autowired private RegionRepository regionRepository;
    @Autowired private SupervisionRepository supervisionRepository;
    @Autowired private BrancheRepository brancheRepository;

    @GetMapping("/regions")
    public ResponseEntity<List<Map<String, Object>>> getAllRegions() {
        System.out.println("=== APPEL GET /regions ===");

        List<Region> allRegions = regionRepository.findAll();
        System.out.println("Nombre de régions trouvées: " + allRegions.size());

        for (Region region : allRegions) {
            System.out.println("Région: ID=" + region.getId() + ", Nom=" + region.getNom() + ", Code=" + region.getCode());
        }

        List<Map<String, Object>> regions = allRegions.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId());
                    map.put("nom", r.getNom());
                    map.put("code", r.getCode());
                    System.out.println("Map créée: " + map);
                    return map;
                })
                .collect(Collectors.toList());

        System.out.println("Réponse finale: " + regions);
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/supervisions")
    public ResponseEntity<List<Map<String, Object>>> getSupervisionsByRegion(@RequestParam Long regionId) {
        List<Map<String, Object>> supervisions = supervisionRepository.findByRegionId(regionId).stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId());
                    map.put("nom", s.getNom());
                    map.put("code", s.getCode());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(supervisions);
    }

    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranchesBySupervision(@RequestParam Long supervisionId) {
        List<Map<String, Object>> branches = brancheRepository.findBySupervisionId(supervisionId).stream()
                .map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", b.getId());
                    map.put("nom", b.getNom());
                    map.put("code", b.getCode());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Structure endpoint accessible !");
    }
}