package in.pervush.poker.model.login;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public record LoginState(String email, AtomicBoolean confirmed, String confirmationCode, AtomicInteger attemptsCount) {

}