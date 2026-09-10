package com.example.bookingsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {

    // READ_ONLY: server-generated, ignored if a client sends it in a request body.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "startTime is required")
    @FutureOrPresent(message = "startTime must be in the present or future")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    @FutureOrPresent(message = "endTime must be in the present or future")
    private LocalDateTime endTime;

    
    private String status;

    @NotNull(message = "price is required")
    @Positive(message = "price must be a positive amount")
    private BigDecimal price;
}
