package com.tuandat.oceanfresh_backend.controllers;

import com.tuandat.oceanfresh_backend.components.JwtTokenUtils;
import com.tuandat.oceanfresh_backend.models.Token;
import com.tuandat.oceanfresh_backend.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/debug")
@RequiredArgsConstructor
public class DebugController {

    private final JwtTokenUtils jwtTokenUtils;
    private final TokenRepository tokenRepository;

    @GetMapping("/test-token")
    public ResponseEntity<?> testToken(@RequestParam String token) {
        try {
            // Test JWT parsing
            boolean isValid = jwtTokenUtils.validateToken(token);
            Long userId = jwtTokenUtils.getUserIdFromToken(token);
            String subject = jwtTokenUtils.getSubject(token);
            boolean isExpired = jwtTokenUtils.isTokenExpired(token);

            // Check if token exists in database
            Token dbToken = tokenRepository.findByToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", isValid,
                    "userId", userId,
                    "subject", subject,
                    "expired", isExpired,
                    "existsInDB", dbToken != null,
                    "isRevoked", dbToken != null ? dbToken.isRevoked() : "N/A",
                    "tokenLength", token.length()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "stackTrace", e.getStackTrace()[0].toString()));
        }
    }

    @GetMapping("/websocket-test")
    public ResponseEntity<?> testWebSocketAuth(@RequestParam String token) {
        try {
            boolean isValid = jwtTokenUtils.validateTokenForWebSocket(token);
            Long userId = jwtTokenUtils.getUserIdFromToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", isValid,
                    "userId", userId,
                    "message", isValid ? "Token hợp lệ cho WebSocket" : "Token không hợp lệ",
                    "websocketUrl", "ws://localhost:8088/ws/chat?token=" + token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "valid", false));
        }
    }
}
