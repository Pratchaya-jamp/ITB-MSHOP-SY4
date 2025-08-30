package intregrated.backend.controllers;

import intregrated.backend.dtos.authentications.MatchPasswordRequestDto;
import intregrated.backend.dtos.authentications.MatchPasswordResponseDto;
import intregrated.backend.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/users")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/authentications")
    public ResponseEntity<MatchPasswordResponseDto> login(@Validated @RequestBody MatchPasswordRequestDto request) {
        try {
            MatchPasswordResponseDto response = authenticationService.authenticateUser(request);
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or Password is incorrect.");
        }
    }
}
