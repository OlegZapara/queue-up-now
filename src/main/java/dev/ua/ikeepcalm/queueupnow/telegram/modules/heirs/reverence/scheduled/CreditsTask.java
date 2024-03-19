package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.scheduled;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CreditsTask {

    private final UserService userService;

    public CreditsTask(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void executeUpdateTask() {
        this.userService.updateAll();
    }

    @Scheduled(cron = "0 22 */2 * * *")
    public void executeIncreaseTask() {
        this.userService.increaseAll();
    }
}