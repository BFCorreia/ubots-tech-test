package com.flowpay.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
    @NotBlank(message = "Client name is required")
    String clientName,
    
    @NotBlank(message = "Subject is required")
    String subject,

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description
) {}
