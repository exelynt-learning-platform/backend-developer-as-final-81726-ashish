package com.example.bookingsystem.service;

import com.example.bookingsystem.dto.ReservationDTO;
import com.example.bookingsystem.entity.Reservation;
import com.example.bookingsystem.entity.Resource;
import com.example.bookingsystem.entity.Status;
import com.example.bookingsystem.entity.User;
import com.example.bookingsystem.exception.ResourceNotFoundException;
import com.example.bookingsystem.exception.UnauthorizedAccessException;
import com.example.bookingsystem.repository.ReservationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;
    private final UserService userService;

   
    @PreAuthorize("isAuthenticated()")
    public ReservationDTO createReservation(ReservationDTO dto) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Resource resource = resourceService.findEntityById(dto.getResourceId());

        Reservation reservation = Reservation.builder()
                .user(currentUser)
                .resource(resource)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(Status.PENDING)
                .price(dto.getPrice())
                .build();

        return toDTO(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(Long id) {
        Reservation reservation = findEntityById(id);
        enforceOwnership(reservation);
        return toDTO(reservation);
    }

    /**
     * Dynamic, context-aware search:
     *  - status / minPrice / maxPrice are optional filters.
     *  - USER callers are silently restricted to their own reservations,
     *    regardless of what filters they pass - this cannot be bypassed
     *    from the request because the owner predicate is appended here,
     *    server-side, based on the SecurityContext.
     *  - ADMIN callers see everything matching the filters.
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO> searchReservations(Status status,
                                                    BigDecimal minPrice,
                                                    BigDecimal maxPrice,
                                                    Pageable pageable) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        boolean isAdmin = userService.isAdmin(currentUser);

        Specification<Reservation> spec = buildSpecification(
                status, minPrice, maxPrice, isAdmin ? null : currentUser.getId());

        return reservationRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @PreAuthorize("isAuthenticated()")
    public ReservationDTO updateReservation(Long id, ReservationDTO dto) {
        Reservation reservation = findEntityById(id);
        enforceOwnership(reservation);

        if (dto.getResourceId() != null) {
            reservation.setResource(resourceService.findEntityById(dto.getResourceId()));
        }
        if (dto.getStartTime() != null) {
            reservation.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            reservation.setEndTime(dto.getEndTime());
        }
        if (dto.getPrice() != null) {
            reservation.setPrice(dto.getPrice());
        }
        if (dto.getStatus() != null) {
            reservation.setStatus(Status.valueOf(dto.getStatus().toUpperCase()));
        }

        return toDTO(reservationRepository.save(reservation));
    }

    @PreAuthorize("isAuthenticated()")
    public void deleteReservation(Long id) {
        Reservation reservation = findEntityById(id);
        enforceOwnership(reservation);
        reservationRepository.delete(reservation);
    }

    // --- internal helpers -------------------------------------------------

    private Reservation findEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", id));
    }

    /**
     * ADMIN can access any reservation. A USER may only access a
     * reservation they own; any other attempt is rejected with 403,
     * regardless of whether the reservation exists, so ownership is never
     * leaked via a different error code.
     */
    private void enforceOwnership(Reservation reservation) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (userService.isAdmin(currentUser)) {
            return;
        }
        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "You do not have permission to access this reservation");
        }
    }

    private Specification<Reservation> buildSpecification(Status status,
                                                            BigDecimal minPrice,
                                                            BigDecimal maxPrice,
                                                            Long restrictToUserId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (restrictToUserId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), restrictToUserId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ReservationDTO toDTO(Reservation reservation) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .resourceId(reservation.getResource().getId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus().name())
                .price(reservation.getPrice())
                .build();
    }
}
