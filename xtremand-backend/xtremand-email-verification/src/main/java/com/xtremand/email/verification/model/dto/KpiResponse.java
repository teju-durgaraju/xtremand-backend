package com.xtremand.email.verification.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KpiResponse {
    private AccountKpi account;
}