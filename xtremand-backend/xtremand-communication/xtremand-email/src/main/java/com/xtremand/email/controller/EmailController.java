package com.xtremand.email.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.exception.RecordNotFoundException;
import com.xtremand.domain.dto.EmailAnalyticsSummaryDto;
import com.xtremand.domain.dto.EmailHistoryDto;
import com.xtremand.domain.dto.EmailTemplateRequest;
import com.xtremand.domain.dto.Response;
import com.xtremand.domain.dto.SendEmailRequest;
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.domain.entity.EmailTemplate;
import com.xtremand.domain.enums.EmailStatus;
import com.xtremand.email.service.EmailAnalyticsService;
import com.xtremand.email.service.EmailService;
import com.xtremand.email.service.EmailTemplateService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
@Tag(name = "Email Sending and Tracking", description = "APIs for sending emails and tracking their engagement")
public class EmailController {

	private final EmailService emailService;

	private final EmailTemplateService templateService;

	private final EmailAnalyticsService analyticsService;

	public EmailController(EmailService emailService, EmailTemplateService templateService,
			EmailAnalyticsService analyticsService) {
		this.emailService = emailService;
		this.templateService = templateService;
		this.analyticsService = analyticsService;
	}

	@Operation(summary = "Send a bulk email", description = "Sends an email to a list of contacts.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Mail sent successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found - A contact was not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
	@PostMapping("/send")
	public ResponseEntity<Response> sendEmail(@RequestBody SendEmailRequest request) {
		emailService.sendBulkEmail(request, null);
		return ResponseEntity.ok(new Response("Mail sent successfully"));
	}

	@Hidden
	@GetMapping("/track/click/{trackingId}")
	public void trackClick(@PathVariable UUID trackingId, @RequestParam("redirect") String redirect,
			HttpServletResponse response) throws IOException {
		analyticsService.logClicked(trackingId);
		response.sendRedirect(redirect);
	}

	@Operation(summary = "Get email analytics summary", description = "Retrieves a summary of email performance analytics.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved analytics summary")
	@GetMapping("/analytics/summary")
	public ResponseEntity<EmailAnalyticsSummaryDto> getEmailAnalyticsSummary() {
		EmailAnalyticsSummaryDto summary = analyticsService.getEmailAnalyticsSummary();
		return ResponseEntity.ok(summary);
	}

	@Operation(summary = "Get email history", description = "Retrieves a history of all sent emails.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved email history")
	@GetMapping("/history")
	public ResponseEntity<List<EmailHistoryDto>> getEmailHistory() {
		return ResponseEntity.ok(analyticsService.getAllEmailHistory());
	}
	
	@Operation(summary = "Email Preview", description = "Retrieves a history of sent email By its id.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved email history")
	@GetMapping("/email-history/{id}")
	public ResponseEntity<EmailHistoryDto> getEmailHistoryById(@PathVariable Long id) {
	    EmailHistoryDto dto = analyticsService.getEmailHistoryById(id);
	    if (dto != null) {
	        return ResponseEntity.ok(dto);
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}


	@Tag(name = "Email Template Management", description = "APIs for managing reusable email templates")
	@Operation(summary = "Create an email template", description = "Creates a new reusable email template.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Template created successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Invalid category or other input", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
	@PostMapping("/email-templates")
	public ResponseEntity<Response> createTemplate(@RequestBody EmailTemplateRequest request) {
		templateService.saveTemplate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new Response("Template created successfully"));
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Get an email template by ID", description = "Retrieves a single email template by its ID.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Successfully retrieved template"),
			@ApiResponse(responseCode = "404", description = "Not Found - Template not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
	@GetMapping("/email-templates/{id}")
	public ResponseEntity<EmailTemplate> getTemplateById(
			@Parameter(description = "ID of the template to retrieve") @PathVariable Long id) {
		EmailTemplate template = templateService.getTemplateById(id);
		if (template == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(template);
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Delete an email template", description = "Deletes an existing email template by its ID.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found - Template not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
	@DeleteMapping("/email-templates/{id}")
	public ResponseEntity<Response> deleteTemplate(
			@Parameter(description = "ID of the template to delete") @PathVariable Long id) {
		templateService.deleteTemplateById(id);
		return ResponseEntity.ok(new Response("Template deleted successfully"));
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Update an email template", description = "Updates an existing email template.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Template updated successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Invalid category or other input", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not Found - Template not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
	@PutMapping("/email-templates/{id}")
	public ResponseEntity<Response> updateTemplate(
			@Parameter(description = "ID of the template to update") @PathVariable Long id,
			@RequestBody EmailTemplateRequest request) {
		EmailTemplate updated = templateService.updateTemplate(id, request);
		if (updated == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new Response("Updated successfully"));
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Get all email templates", description = "Retrieves a list of all email templates, with optional filtering by category and search term.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved templates")
	@GetMapping("/email-templates")
	public ResponseEntity<Map<String, Object>> getAllTemplates(
			@Parameter(description = "Filter templates by category") @RequestParam(required = false) String category,
			@Parameter(description = "Search term for template name or content") @RequestParam(required = false) String search) {
		Map<String, Object> result = templateService.getTemplatesWithStats(category, search);
		return ResponseEntity.ok(result);
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Get all template categories", description = "Retrieves a list of all available email template categories.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
	@GetMapping("/email-templates/categories")
	public ResponseEntity<List<String>> getAllCategories() {
		List<String> categories = templateService.getAllCategories();
		return ResponseEntity.ok(categories);
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Get all campaign types", description = "Retrieves a list of all available campaign types for templates.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved campaign types")
	@GetMapping("/email-templates/campaignTypes")
	public ResponseEntity<List<String>> getAllCampaignTypes() {
		List<String> categories = templateService.getAllCampaignTypes();
		return ResponseEntity.ok(categories);
	}

	@Tag(name = "Email Template Management")
	@Operation(summary = "Get all template tones", description = "Retrieves a list of all available tones for templates.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved tones")
	@GetMapping("/email-templates/tones")
	public ResponseEntity<List<String>> getAllTones() {
		List<String> categories = templateService.getAllTones();
		return ResponseEntity.ok(categories);
	}

}
