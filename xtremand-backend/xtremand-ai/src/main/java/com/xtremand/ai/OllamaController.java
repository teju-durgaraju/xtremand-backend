package com.xtremand.ai;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.domain.dto.EmailRequest;

@RestController
@RequestMapping("/ai")
public class OllamaController {

	private final OllamaService ollamaService;
	
	private final PollinationsService pollinationsService;

	public OllamaController(OllamaService ollamaService,PollinationsService pollinationsService) {
		this.ollamaService = ollamaService;
		this.pollinationsService = pollinationsService;
	}

	@PostMapping("/generateEmail")
	public String generate(@RequestBody EmailRequest emailRequest) {
	    return ollamaService.getResponse(emailRequest);
	}


	@PostMapping("/generatePollinationsText")
	public String generatePollinationsText(@RequestBody Map<String, String> request) {
	    String prompt = request.getOrDefault("prompt", "Hello");
	    return pollinationsService.generateText(prompt);
	}
	
}
