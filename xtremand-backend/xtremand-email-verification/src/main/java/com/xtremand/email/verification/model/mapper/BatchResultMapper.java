package com.xtremand.email.verification.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.model.dto.BatchVerificationResultDto;
import com.xtremand.email.verification.model.dto.DistinctEmailVerificationResultDto;
import com.xtremand.email.verification.model.dto.EmailVerificationBatchDto;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository.DistinctLatestVerificationProjection;

@Mapper(componentModel = "spring", imports = { EmailVerificationHistory.class })
public interface BatchResultMapper {

	@Mapping(source = "id", target = "batchId")
	@Mapping(source = "userId", target = "userId")
	EmailVerificationBatchDto toBatchDto(EmailVerificationBatch entity);

	@Mapping(source = "id", target = "batchId")
	BatchVerificationResultDto toBatchResultDto(EmailVerificationBatch entity);

	@Mapping(target = "status", expression = "java(EmailVerificationHistory.VerificationStatus.valueOf(projection.getStatus()))")
	@Mapping(target = "confidence", expression = "java(EmailVerificationHistory.Confidence.valueOf(projection.getConfidence()))")
	@Mapping(target = "checks.syntaxCheck", source = "syntaxCheck")
	@Mapping(target = "checks.mxCheck", source = "disposableCheck")
	@Mapping(target = "checks.roleBasedCheck", source = "roleBasedCheck")
	@Mapping(target = "checks.catchAllCheck", source = "catchAllCheck")
	@Mapping(target = "checks.blacklistCheck", source = "blacklistCheck")
	@Mapping(target = "checks.smtpCheck", source = "smtpCheck")
	@Mapping(target = "checks.smtpPing", source = "smtpPing")
	DistinctEmailVerificationResultDto toDistinctDto(DistinctLatestVerificationProjection projection);
}