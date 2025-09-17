package com.xtremand.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.domain.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RAGController {

    private final RAGService ragService;
    private final AuthenticationFacade authenticationFacade;

    @Autowired
    public RAGController(RAGService ragService, AuthenticationFacade authenticationFacade) {
        this.ragService = ragService;
        this.authenticationFacade = authenticationFacade;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RAGRegisterRequest req) {
        Authentication authentication = authenticationFacade.getAuthentication();
        try {
            String response = ragService.registerUser(req, authentication);
            if (response == null) {
                return ResponseEntity.status(400).body("{\"error\":\"Registration failed\"}");
            }
            return ResponseEntity.ok("{\"sessionId\":" + response + "}");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }


    @GetMapping("/thread/list")
    public ResponseEntity<List<ThreadInfo>> getThreadList() {
        Authentication authentication = authenticationFacade.getAuthentication();
        try {
            List<ThreadInfo> response = ragService.getThreadList(authentication);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/thread/{threadId}/messages")
    public ResponseEntity<JsonNode> getThreadMessages(@PathVariable String threadId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        try {
            JsonNode messages = ragService.getThreadMessages(threadId, authentication);
            return ResponseEntity.ok(messages);
        } catch (Exception ex) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/session")
    public ThreadRequest createSession() {
        Authentication authentication = authenticationFacade.getAuthentication();
        try {
            return ragService.createVectorStore(authentication);
        } catch (Exception ex) {
            return null ;
        }
    }
    
    
    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestBody InputRequest req) {
        Authentication authentication = authenticationFacade.getAuthentication();
        try {
            String response = ragService.sentRequest(authentication,req);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }
}
