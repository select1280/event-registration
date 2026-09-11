package event_registration.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception{
        http.csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR)
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/csrf"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/members"
                        ).permitAll()

                        // 登入者可替自己報名，必須放在較廣泛的活動管理規則之前。
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/events/*/registrations"
                        ).authenticated()

                        // 其他活動 POST 操作仍限管理員，例如新增與發布。
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/events",
                                "/api/events/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/events/**"
                        ).hasRole("ADMIN")


                        .anyRequest().authenticated()

                )

                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(
                                (request, response, authentication) ->
                                            response.setStatus(
                                                HttpServletResponse.SC_NO_CONTENT
                                             )
                                )
                                .failureHandler(
                                        (request, response, exception) ->
                                            response.setStatus(
                                                    HttpServletResponse.SC_UNAUTHORIZED
                                            )
                                )
                                .permitAll()
                        )

                        .logout(logout -> logout
                                .logoutUrl("/api/auth/logout")
                                .logoutSuccessHandler(
                                        (request, response, authentication) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_NO_CONTENT
                                                )
                                )
                        )

                        .exceptionHandling(exceptions -> exceptions
                                .authenticationEntryPoint(
                                        (request, response, exception) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_UNAUTHORIZED
                                                )
                                )

                                .accessDeniedHandler(
                                        (request, response, exception) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_FORBIDDEN
                                                )
                                )
                        )

                        .requestCache(cache -> cache.disable());

                return http.build();

    }
}
