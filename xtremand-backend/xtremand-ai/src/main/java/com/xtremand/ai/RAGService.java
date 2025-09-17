package com.xtremand.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.xtremand.config.repository.AiConfigRepository;
import com.xtremand.domain.dto.*;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.entity.RagSession;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.AiConfigType;
import com.xtremand.user.repository.UserRepository;

@Service
public class RAGService {

	@Autowired
	private AiConfigRepository aiConfigRepository;

	@Autowired
	private RagSessionRepository ragSessionRepository;

	@Autowired
	private UserRepository userRepository;

	private static final String REGISTER_URL = "https://rag.xamplify.co/rag/register/";
	private static final String LOGIN_URL = "https://rag.xamplify.co/rag/login/";

	private final HttpClient client = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public String registerUser(RAGRegisterRequest req, Authentication authentication) throws Exception {
		User user = userRepository.fetchByUsername(authentication.getName());
		String requestBody = objectMapper.writeValueAsString(req);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(REGISTER_URL))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			RAGLoginRequest loginReq = new RAGLoginRequest();
			loginReq.setUsername(req.getUsername());
			loginReq.setPassword(req.getPassword());
			return loginUser(loginReq, user);
		} else {
			return response.body();
		}
	}

	public String loginUser(RAGLoginRequest req, User user) throws Exception {
		String requestBody = objectMapper.writeValueAsString(req);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(LOGIN_URL))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			String email = req.getUsername();
			JsonNode respJson = objectMapper.readTree(response.body());
			String token = respJson.get("token").asText();
			createAiConfig(email, req.getPassword(), token);
			return "registered successfully";
		}
		return response.body();
	}

	private void createAiConfig(String email, String password, String token) {
		AiConfig config = new AiConfig();
		config.setEmail(email);
		config.setConfigType(AiConfigType.RAGITIFY);
		config.setApiKey(email);
		config.setApiSecret(password);
		config.setCreatedAt(LocalDateTime.now());
		config.setUpdatedAt(LocalDateTime.now());
		config.setToken(token);
		aiConfigRepository.save(config);
	}

	private String getToken(Authentication authentication) {
		User user = userRepository.fetchByUsername(authentication.getName());
		AiConfig config = aiConfigRepository.findByCreatedByAndConfigType(user, AiConfigType.RAGITIFY);
		return config.getToken();
	}

	public List<ThreadInfo> getThreadList(Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String url = "https://rag.xamplify.co/rag/thread/" + token + "/list/";
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json")
				.GET().build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), new TypeReference<List<ThreadInfo>>() {
		});
	}

	public JsonNode getThreadMessages(String threadId, Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String url = "https://rag.xamplify.co/rag/thread/" + token + "/" + threadId + "/messages/";
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json")
				.GET().build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readTree(response.body());
	}

	public ThreadRequest createVectorStore(Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String name = "My_Vector_Store_" + LocalDate.now();
		String uri = "https://rag.xamplify.co/rag/vector-store/" + token + "/";
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("name", name);
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(uri))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String vectorId = null;
			if (jsonNode.has("id")) {
				vectorId = jsonNode.get("id").asText();
				return createThread(vectorId, token);
			} else {
				throw new Exception("vector id not found in response");
			}

		}
		return null;
	}

	public ThreadRequest createThread(String vectorStoreId, String token) throws Exception {
		String url = "https://rag.xamplify.co/rag/thread/" + token + "/";
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("vector_store_d", vectorStoreId);
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String threadId = null;
			if (jsonNode.has("id")) {
				threadId = jsonNode.get("id").asText();
				AssistantRequest assisstant = new AssistantRequest();
				assisstant.setName(body);
				assisstant.setVector_store_id(vectorStoreId);
				return createAssistant(token, assisstant, threadId);
			} else {
				throw new Exception("threadId not found in response");
			}
		}
		return null;
	}

	private ThreadRequest createAssistant(String token, AssistantRequest request, String threadId) throws Exception {
		String url = "https://rag.xamplify.co/rag/assistant/" + token + "/";
		String body = objectMapper.writeValueAsString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String assisstantId = null;
			if (jsonNode.has("id")) {
				assisstantId = jsonNode.get("id").asText();
				RagSession session = new RagSession();
				session.setVectorStoreId(request.getVector_store_id());
				session.setThreadId(threadId);
				session.setAssistantId(assisstantId);
				session.setCreatedAt(LocalDateTime.now());
				ragSessionRepository.save(session);
				ThreadRequest req = new ThreadRequest();
				req.setThreadId(threadId);
				req.setVectorStoreId(request.getVector_store_id());
				req.setAssisstantId(assisstantId);
				return req;
			} else {
				throw new Exception("assistant id not found in response");
			}
		}
		return null;
	}

	public String sentRequest(Authentication authentication, InputRequest input) throws Exception {
		String token = getToken(authentication);
		String documentId = null;
		if (input.getFile() != null && input.getFile().getFile() != null) {
			try {
				documentId = ingestDocument(input.getFile(), token);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		sendMessage(input, token, documentId);
		return token;

	}

	public String ingestDocument(DocumentIngestRequest request, String token) throws Exception {
		String url = "https://rag.xamplify.co/rag/document/" + token + "/ingest/";
		String body = objectMapper.writeValueAsString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String documentIngestId = null;
			if (jsonNode.has("document_ingest_id")) {
				documentIngestId = jsonNode.get("document_ingest_id").asText();
				return documentIngestId;
			} else {
				throw new Exception("document_ingest_id not found in response");
			}
		}
		return response.body();
	}

	public String sendMessage(InputRequest request, String token, String documentId) throws Exception {
		String url = "https://rag.xamplify.co/rag/message/" + token + "/";
		ObjectMapper objectMapper = new ObjectMapper();
		String body = objectMapper.writeValueAsString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			RunRequest run = new RunRequest();
			if (request.getMessageRequest() != null && request.getMessageRequest().getThreadId() != null) {
				run.setThreadId(request.getMessageRequest().getThreadId());
				run.setAssistantId(request.getAssisstantId());
				return runAssistant(token, run, documentId);

			}
		}
		return response.body();
	}

	public String runAssistant(String token, RunRequest request, String documentId) throws Exception {
		String url = "https://rag.xamplify.co/rag/run/" + token + "/";
		ObjectMapper objectMapper = new ObjectMapper();
		String body = objectMapper.writeValueAsString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

}