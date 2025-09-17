package com.xtremand.email.verification.controller;

import com.xtremand.email.verification.dto.EmailVerifierInput;
import com.xtremand.email.verification.dto.EmailVerifierOutput;
import com.xtremand.email.verification.dto.KpiDto;
import com.xtremand.email.verification.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-verifier")
@Tag(name = "Email verifier", description = "Operations related to email verification.")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping
    @Operation(summary = "Verify email", responses = {
            @ApiResponse(responseCode = "200", description = "Email verification result"),
            @ApiResponse(responseCode = "400", description = "Invalid input")})
    public EmailVerifierOutput validateEmail(@Valid @RequestBody EmailVerifierInput input) {
        return emailVerificationService.verify(input.getEmail());
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get email verification KPIs", responses = {
            @ApiResponse(responseCode = "200", description = "KPI data")})
    public KpiDto getKpis() {
        return emailVerificationService.getKpis();
    }
}
