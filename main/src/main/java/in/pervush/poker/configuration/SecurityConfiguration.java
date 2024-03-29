package in.pervush.poker.configuration;

import in.pervush.poker.controller.LoginController;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.utils.auth.JwtTokenFilter;
import in.pervush.poker.utils.auth.RequestHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@Import({PostgresConfiguration.class})
public class SecurityConfiguration {

    public final String apiDocsPath;
    public final String swaggerUiPath;
    public final RequestHelper requestHelper;
    public final UsersRepository usersRepository;

    public SecurityConfiguration(@Value("${springdoc.api-docs.path:}") final String apiDocsPath,
                                 @Value("${springdoc.swagger-ui.path:}") final String swaggerUiPath,
                                 final RequestHelper requestHelper,
                                 final UsersRepository usersRepository) {
        this.apiDocsPath = apiDocsPath;
        this.swaggerUiPath = swaggerUiPath;
        this.requestHelper = requestHelper;
        this.usersRepository = usersRepository;
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
                .authorizeHttpRequests()
                .requestMatchers(
                        LoginController.PATH + "/**",
                        LoginController.PATH + "**"
                )
                .permitAll()

                .anyRequest().authenticated();

        http.authenticationProvider(authenticationProvider);

        // Manually create JwtTokenFilter due to https://github.com/spring-projects/spring-security/issues/3958
        http.addFilterBefore(new JwtTokenFilter(usersRepository, requestHelper), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            if (Strings.isNotEmpty(apiDocsPath)) {
                web.ignoring().requestMatchers(HttpMethod.GET, apiDocsPath + "/**");
                if (Strings.isNotEmpty(swaggerUiPath)) {
                    web.ignoring().requestMatchers(HttpMethod.GET, swaggerUiPath + "/**");
                }
            }
        };
    }

    @Bean
    public AuthenticationManagerBeanDefinitionParser.NullAuthenticationProvider authenticationProvider() {
        return new AuthenticationManagerBeanDefinitionParser.NullAuthenticationProvider();

    }

}
