package com.example.bookingsystem.service;

import com.example.bookingsystem.dto.ResourceDTO;
import com.example.bookingsystem.entity.Resource;
import com.example.bookingsystem.exception.ResourceNotFoundException;
import com.example.bookingsystem.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;

    // Read access: any authenticated user (ADMIN or USER).
    @Transactional(readOnly = true)
    public Page<ResourceDTO> getAllResources(Pageable pageable) {
        return resourceRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ResourceDTO getResourceById(Long id) {
        return toDTO(findEntityById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResourceDTO createResource(ResourceDTO dto) {
        Resource resource = Resource.builder()
                .name(dto.getName())
                .type(dto.getType())
                .isAvailable(Boolean.TRUE.equals(dto.getIsAvailable()))
                .build();
        return toDTO(resourceRepository.save(resource));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResourceDTO updateResource(Long id, ResourceDTO dto) {
        Resource resource = findEntityById(id);
        resource.setName(dto.getName());
        resource.setType(dto.getType());
        resource.setAvailable(Boolean.TRUE.equals(dto.getIsAvailable()));
        return toDTO(resourceRepository.save(resource));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteResource(Long id) {
        Resource resource = findEntityById(id);
        resourceRepository.delete(resource);
    }

    Resource findEntityById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Resource", id));
    }

    private ResourceDTO toDTO(Resource resource) {
        return ResourceDTO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .isAvailable(resource.isAvailable())
                .build();
    }
}
