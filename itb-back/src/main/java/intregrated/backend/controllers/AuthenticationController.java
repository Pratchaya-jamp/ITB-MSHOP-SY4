package intregrated.backend.controllers;

import intregrated.backend.dtos.authentications.MatchPasswordRequestDto;
import intregrated.backend.dtos.authentications.MatchpasswordResponseDto;
import intregrated.backend.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/users")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/authentications")
    public ResponseEntity<?> login(@Validated @RequestBody MatchPasswordRequestDto request) {
        try {
            MatchpasswordResponseDto response = authenticationService.authenticateUser(request);
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Username or Password is incorrect.\"}");
        }
    }
}
