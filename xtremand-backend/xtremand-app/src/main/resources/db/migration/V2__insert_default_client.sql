INSERT INTO oauth_clients (
    client_id,
    client_secret,
    client_name,
    grant_types,
    redirect_uris,
    scopes,
    access_token_validity,
    refresh_token_validity
) VALUES (
    'xAmplify-Web-Client',
    '$2a$10$x/B0bJ9z.Loy.J.b.bJ06.L3.9/3.bK9z/F.b.bJ06.L3.9/3.bK9z', -- This is a sample bcrypt hash, the actual secret is not important
    'xAmplify Web Client',
    'password,refresh_token',
    'http://localhost:4200/login/oauth2/code/okta',
    'openid,email,profile',
    900,
    86400
);
