    package intregrated.backend.utils;

//    import intregrated.backend.filters.JwtAuthFilter;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

    @Configuration
    public class SecurityConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new Argon2PasswordEncoder(16, 16,
                    8, 1024 * 64, 2);
        }

//        @Bean
//        public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter filter) throws Exception {
//            http
//                    .csrf(csrf -> csrf.disable())
//                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                    .authorizeHttpRequests(auth -> auth
//                            .requestMatchers("/v2/users/authentications", "/v2/users/register", "/sy4/itb-mshop/v2/users/verify-email").permitAll() // allow login & register
//                            .anyRequest().authenticated()
//                    )
//                    .authorizeHttpRequests(auth -> auth
//                            .anyRequest().permitAll() // ทุก request เข้าได้
//                    );
//                    .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
//
//            return http.build();
//        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()   // ทุก request เข้าได้
                    );
            return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
            return config.getAuthenticationManager();
        }
    }
