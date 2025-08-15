package com.tuandat.oceanfresh_backend.services.token;

import org.springframework.stereotype.Service;

import com.tuandat.oceanfresh_backend.models.Token;
import com.tuandat.oceanfresh_backend.models.User;

@Service

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user) throws Exception;
}
