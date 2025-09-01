package intregrated.backend.controllers;

import intregrated.backend.dtos.authentications.MatchPasswordRequestDto;
import intregrated.backend.dtos.authentications.MatchPasswordResponseDto;
import intregrated.backend.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<MatchPasswordResponseDto> login(@Validated @RequestBody MatchPasswordRequestDto request) {
            MatchPasswordResponseDto response = authenticationService.authenticateUser(request);
            return ResponseEntity.ok().body(response);
    }
}
