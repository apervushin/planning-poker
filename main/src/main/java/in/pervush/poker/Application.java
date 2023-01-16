package in.pervush.poker;

import in.pervush.poker.utils.auth.RequestHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = RequestHelper.SESSION_COOKIE_NAME
)
@OpenAPIDefinition(
        servers = {@Server(url = "https://estimate.pervush.in/"), @Server(url = "http://localhost:8080/")},
        info = @Info(title = "Planing poker API", contact = @Contact(url = "https://github.com/apervushin/planning-poker"))

)
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
