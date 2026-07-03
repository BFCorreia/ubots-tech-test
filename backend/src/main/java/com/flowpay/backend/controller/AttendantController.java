package com.flowpay.backend.controller;

import com.flowpay.backend.dto.AttendantCreateRequest;
import com.flowpay.backend.dto.AttendantResponse;
import com.flowpay.backend.service.AttendantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendants")
public class AttendantController {

    private final AttendantService attendantService;

    public AttendantController(AttendantService attendantService) {
        this.attendantService = attendantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendantResponse createAttendant(@RequestBody @Valid AttendantCreateRequest req) {
        return attendantService.createAttendant(req);
    }

    @GetMapping
    public List<AttendantResponse> getAttendants() {
        return attendantService.getAllAttendants();
    }
}
