/*
 * package com.xtremand.auth.login.provider;
 * 
 * import static org.junit.jupiter.api.Assertions.assertThrows; import static
 * org.mockito.Mockito.when;
 * 
 * import java.util.Optional;
 * 
 * import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test;
 * import org.mockito.InjectMocks; import org.mockito.Mock; import
 * org.mockito.MockitoAnnotations; import
 * org.springframework.security.authentication.BadCredentialsException; import
 * org.springframework.security.authentication.
 * UsernamePasswordAuthenticationToken; import
 * org.springframework.security.crypto.password.PasswordEncoder;
 * 
 * import com.xtremand.auth.login.exception.AccountDeletedException; import
 * com.xtremand.auth.login.exception.AccountSuspendedException; import
 * com.xtremand.auth.login.exception.AccountUnapprovedException; import
 * com.xtremand.common.identity.AuthUserDto; import
 * com.xtremand.common.identity.UserLookupService; import
 * com.xtremand.domain.enums.UserStatus;
 * 
 * public class CustomLoginAuthenticationProviderTest {
 * 
 * @Mock private UserLookupService userLookupService;
 * 
 * @Mock private PasswordEncoder passwordEncoder;
 * 
 * @InjectMocks private CustomLoginAuthenticationProvider
 * authenticationProvider;
 * 
 * @BeforeEach public void setUp() { MockitoAnnotations.openMocks(this); }
 * 
 * @Test public void
 * testAuthenticate_whenUserIsUnapproved_throwsAccountUnapprovedException() {
 * AuthUserDto user = AuthUserDto.builder() .email("test@example.com")
 * .passwordHash("password") .status(UserStatus.UNAPPROVED) .build();
 * 
 * when(userLookupService.findByEmail("test@example.com")).thenReturn(Optional.
 * of(user));
 * 
 * UsernamePasswordAuthenticationToken authentication = new
 * UsernamePasswordAuthenticationToken("test@example.com", "password");
 * 
 * assertThrows(AccountUnapprovedException.class, () -> {
 * authenticationProvider.authenticate(authentication); }); }
 * 
 * @Test public void
 * testAuthenticate_whenUserIsSuspended_throwsAccountSuspendedException() {
 * AuthUserDto user = AuthUserDto.builder() .email("test@example.com")
 * .passwordHash("password") .status(UserStatus.SUSPENDED) .build();
 * 
 * when(userLookupService.findByEmail("test@example.com")).thenReturn(Optional.
 * of(user));
 * 
 * UsernamePasswordAuthenticationToken authentication = new
 * UsernamePasswordAuthenticationToken("test@example.com", "password");
 * 
 * assertThrows(AccountSuspendedException.class, () -> {
 * authenticationProvider.authenticate(authentication); }); }
 * 
 * @Test public void
 * testAuthenticate_whenUserIsDeactivated_throwsAccountDeletedException() {
 * AuthUserDto user = AuthUserDto.builder() .email("test@example.com")
 * .passwordHash("password") .status(UserStatus.DEACTIVATED) .build();
 * 
 * when(userLookupService.findByEmail("test@example.com")).thenReturn(Optional.
 * of(user));
 * 
 * UsernamePasswordAuthenticationToken authentication = new
 * UsernamePasswordAuthenticationToken("test@example.com", "password");
 * 
 * assertThrows(AccountDeletedException.class, () -> {
 * authenticationProvider.authenticate(authentication); }); }
 * 
 * @Test public void
 * testAuthenticate_whenPasswordIsInvalid_throwsBadCredentialsException() {
 * AuthUserDto user = AuthUserDto.builder() .email("test@example.com")
 * .passwordHash("password") .status(UserStatus.APPROVED) .build();
 * 
 * when(userLookupService.findByEmail("test@example.com")).thenReturn(Optional.
 * of(user)); when(passwordEncoder.matches("wrong_password",
 * "password")).thenReturn(false);
 * 
 * UsernamePasswordAuthenticationToken authentication = new
 * UsernamePasswordAuthenticationToken("test@example.com", "wrong_password");
 * 
 * assertThrows(BadCredentialsException.class, () -> {
 * authenticationProvider.authenticate(authentication); }); } }
 */