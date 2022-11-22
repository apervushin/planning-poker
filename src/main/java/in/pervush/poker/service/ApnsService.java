package in.pervush.poker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApnsService {

    public void sendPush(final String token, final String message, final int notTasksCount) {
        log.info("Send push. Token: {}, message: {}, notTasksCount: {}", token, message, notTasksCount);
    }
}
