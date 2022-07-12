package in.pervush.poker;

import in.pervush.poker.utils.RequestHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = RequestHelper.USER_UUID_COOKIE_NAME
)
@OpenAPIDefinition(
        servers = {@Server(url = "https://poker.pervush.in/"), @Server(url = "http://localhost:8080/")},
        info = @Info(title = "Planing poker API", contact = @Contact(url = "https://github.com/apervushin/planing-poker"))

)
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
