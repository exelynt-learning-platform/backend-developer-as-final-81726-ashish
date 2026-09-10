package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.ReservationDTO;
import com.example.bookingsystem.entity.Status;
import com.example.bookingsystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
public class ReservationController {

    private final ReservationService reservationService;
    @Operation(summary = "Search reservations with optional filters, pagination and sorting")
    @GetMapping
    public ResponseEntity<Page<ReservationDTO>> searchReservations(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
        return ResponseEntity.ok(reservationService.searchReservations(status, minPrice, maxPrice, pageable));
    }

    @Operation(summary = "Get a reservation by id (owner or ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @Operation(summary = "Create a reservation - the owner is always the authenticated caller")
    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(@Valid @RequestBody ReservationDTO dto) {
        ReservationDTO created = reservationService.createReservation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a reservation (owner or ADMIN only)")
    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id,
                                                             @Valid @RequestBody ReservationDTO dto) {
        return ResponseEntity.ok(reservationService.updateReservation(id, dto));
    }

    @Operation(summary = "Delete/cancel a reservation (owner or ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
