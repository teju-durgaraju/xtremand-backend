package com.xtremand.auth.login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.xtremand.auth.forgotpassword.dto.ResetPasswordRequest;
import com.xtremand.auth.forgotpassword.service.ForgotPasswordService;
import com.xtremand.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForgotPasswordService forgotPasswordService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private OAuth2AuthorizationService authorizationService;

    @MockBean
    private com.xtremand.auth.oauth2.customlogin.service.AuthenticationService authenticationService;

    @MockBean
    private com.xtremand.auth.oauth2.customlogin.service.OAuth2LoginComponents oAuth2LoginComponents;

    @MockBean
    private com.xtremand.user.repository.UserRepository userRepository;

    @Test
    @WithMockUser
    void forgotPassword_shouldReturnOk_whenRequestIsValid() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(forgotPasswordService).forgotPassword(any());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void forgotPassword_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturnOk_whenRequestIsValid() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("valid-token");
        request.setNewPassword("new-password");

        doNothing().when(forgotPasswordService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturnBadRequest_whenTokenIsMissing() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("new-password");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
