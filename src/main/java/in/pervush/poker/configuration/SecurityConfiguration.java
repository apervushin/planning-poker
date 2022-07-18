package in.pervush.poker.configuration;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class SecurityConfiguration {

    private final String apiDocsPath;
    private final String swaggerUiPath;

    public SecurityConfiguration(@Value("${springdoc.api-docs.path:}") String apiDocsPath,
                                 @Value("${springdoc.swagger-ui.path:}") String swaggerUiPath) {
        this.apiDocsPath = apiDocsPath;
        this.swaggerUiPath = swaggerUiPath;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring().antMatchers("/**");
            if (Strings.isNotEmpty(apiDocsPath)) {
                web.ignoring().antMatchers(HttpMethod.GET, apiDocsPath + "/**");
                if (Strings.isNotEmpty(swaggerUiPath)) {
                    web.ignoring().antMatchers(HttpMethod.GET, swaggerUiPath + "/**");
                }
            }
        };
    }
}
