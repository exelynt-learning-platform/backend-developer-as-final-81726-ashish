package com.example.bookingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceDTO {

    private Long id;

    @NotBlank(message = "Resource name is required")
    private String name;

    @NotBlank(message = "Resource type is required")
    private String type;

    @NotNull(message = "Availability flag is required")
    private Boolean isAvailable;
}
