package com.xtremand.email.verification.model.mapper;

import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.model.dto.BatchVerificationResultDto;
import com.xtremand.email.verification.model.dto.DistinctEmailVerificationResultDto;
import com.xtremand.email.verification.model.dto.EmailVerificationBatchDto;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository.DistinctLatestVerificationProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {EmailVerificationHistory.class})
public interface BatchResultMapper {

    @Mapping(source = "id", target = "batchId")
    EmailVerificationBatchDto toBatchDto(EmailVerificationBatch entity);

    @Mapping(source = "id", target = "batchId")
    BatchVerificationResultDto toBatchResultDto(EmailVerificationBatch entity);

    @Mapping(target = "status", expression = "java(EmailVerificationHistory.VerificationStatus.valueOf(projection.getStatus()))")
    @Mapping(target = "confidence", expression = "java(EmailVerificationHistory.Confidence.valueOf(projection.getConfidence()))")
    @Mapping(target = "checks.syntax_check", source = "syntax_check")
    @Mapping(target = "checks.mx_check", source = "mx_check")
    @Mapping(target = "checks.disposable_check", source = "disposable_check")
    @Mapping(target = "checks.role_based_check", source = "role_based_check")
    @Mapping(target = "checks.catch_all_check", source = "catch_all_check")
    @Mapping(target = "checks.blacklist_check", source = "blacklist_check")
    @Mapping(target = "checks.smtp_check", source = "smtp_check")
    @Mapping(target = "checks.smtp_ping", source = "smtp_ping")
    DistinctEmailVerificationResultDto toDistinctDto(DistinctLatestVerificationProjection projection);
}