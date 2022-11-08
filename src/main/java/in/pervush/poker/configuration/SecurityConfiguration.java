package in.pervush.poker.configuration;

import in.pervush.poker.controller.LoginController;
import in.pervush.poker.controller.RegistrationController;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.utils.auth.JwtTokenFilter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@Import({PasswordEncoderConfiguration.class, PostgresConfiguration.class})
public class SecurityConfiguration {

    private final String apiDocsPath;
    private final String swaggerUiPath;

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfiguration(@Value("${springdoc.api-docs.path:}") final String apiDocsPath,
                                 @Value("${springdoc.swagger-ui.path:}") final String swaggerUiPath,
                                 final JwtTokenFilter jwtTokenFilter) {
        this.apiDocsPath = apiDocsPath;
        this.swaggerUiPath = swaggerUiPath;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http, final AuthenticationProvider authenticationProvider)
            throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(
                        (request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                )

                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers(LoginController.PATH + "**", RegistrationController.PATH + "**")
                .permitAll()

                .anyRequest().authenticated();

        http.authenticationProvider(authenticationProvider);

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            if (Strings.isNotEmpty(apiDocsPath)) {
                web.ignoring().antMatchers(HttpMethod.GET, apiDocsPath + "/**");
                if (Strings.isNotEmpty(swaggerUiPath)) {
                    web.ignoring().antMatchers(HttpMethod.GET, swaggerUiPath + "/**");
                }
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(final PasswordEncoder passwordEncoder,
                                                            final UsersRepository usersRepository) {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(username -> {
            try {
                return UserDetailsImpl.of(usersRepository.getUser(username));
            } catch (UserNotFoundException ex) {
                throw new UsernameNotFoundException("");
            }
        });
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }
}
