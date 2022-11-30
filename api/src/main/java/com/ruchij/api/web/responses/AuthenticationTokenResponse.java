package com.ruchij.api.web.responses;

import com.ruchij.api.services.authentication.models.AuthenticationToken;

public record AuthenticationTokenResponse(String userId, String token) {
    public static AuthenticationTokenResponse from(AuthenticationToken authenticationToken) {
        return new AuthenticationTokenResponse(authenticationToken.userId(), authenticationToken.token());
    }
}
