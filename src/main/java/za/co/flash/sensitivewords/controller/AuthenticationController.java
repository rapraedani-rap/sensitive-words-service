package za.co.flash.sensitivewords.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {


    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Logs out the authenticated user and clears the security context"
    )
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {

        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.ok().build();
    }
}