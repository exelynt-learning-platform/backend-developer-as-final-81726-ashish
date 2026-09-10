package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.ResourceDTO;
import com.example.bookingsystem.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Resources")
public class ResourceController {

    private final ResourceService resourceService;

    // ADMIN and USER both have read access.
    @Operation(summary = "List all resources (paginated)")
    @GetMapping
    public ResponseEntity<Page<ResourceDTO>> getAllResources(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(resourceService.getAllResources(pageable));
    }

    @Operation(summary = "Get a single resource by id")
    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    
    @Operation(summary = "Create a new resource (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(@Valid @RequestBody ResourceDTO dto) {
        ResourceDTO created = resourceService.createResource(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing resource (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResourceDTO> updateResource(@PathVariable Long id,
                                                       @Valid @RequestBody ResourceDTO dto) {
        return ResponseEntity.ok(resourceService.updateResource(id, dto));
    }

    @Operation(summary = "Delete a resource (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
