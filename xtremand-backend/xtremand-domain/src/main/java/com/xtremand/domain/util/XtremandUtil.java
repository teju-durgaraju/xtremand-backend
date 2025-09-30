package com.xtremand.domain.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class XtremandUtil {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private Environment environment;
	
	public String getActiveProfile() {
		return environment.getProperty("spring.profiles.active");
	}

	public boolean isProfileActive() {
		boolean isActive = false;
		String activeProfile = getActiveProfile();
		if (StringUtils.hasText(activeProfile) && !"local".equalsIgnoreCase(activeProfile)) {
			isActive = true;
		}
		return isActive;
	}
	
	 
	    public static String decrypt(String encrypted) {
	        if (encrypted == null || encrypted.isEmpty()) {
	            return null;
	        }
	        try {
	            byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
	            return new String(decodedBytes, StandardCharsets.UTF_8);
	        } catch (IllegalArgumentException e) {
	            return null;
	        }
	    }

	    public static String formatResponse(String responseText) {
	        String subject = "No Subject";
	        String body = responseText;

	        int subjectIndex = responseText.toLowerCase().indexOf("subject:");
	        if (subjectIndex != -1) {
	            int endOfSubjectLine = responseText.indexOf('\n', subjectIndex);
	            if (endOfSubjectLine == -1) {
	                endOfSubjectLine = responseText.length();
	            }

	            String subjectLine = responseText.substring(subjectIndex, endOfSubjectLine);
	            subject = subjectLine.substring("subject:".length()).trim();

	            body = responseText.substring(endOfSubjectLine).trim();
	        }

	        Map<String, String> result = new LinkedHashMap<>();
	        result.put("subject", subject);
	        result.put("body", body);

	        try {
	            return objectMapper.writeValueAsString(result);
	        } catch (JsonProcessingException e) {
	            return "{\"error\": \"Failed to format response.\"}";
	        }
	    }

}
