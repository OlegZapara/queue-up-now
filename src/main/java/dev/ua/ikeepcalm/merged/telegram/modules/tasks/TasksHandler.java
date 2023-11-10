package dev.ua.ikeepcalm.merged.telegram.modules.tasks;

import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands.TaskCreationCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands.TaskEditingCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands.WhatIsDueCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TasksHandler implements HandlerParent {

    private final TaskCreationCommand taskCreationCommand;
    private final TaskEditingCommand taskEditingCommand;
    private final WhatIsDueCommand whatIsDueCommand;

    public TasksHandler(TaskCreationCommand taskCreationCommand, TaskEditingCommand taskEditingCommand, WhatIsDueCommand whatIsDueCommand) {
        this.taskCreationCommand = taskCreationCommand;
        this.taskEditingCommand = taskEditingCommand;
        this.whatIsDueCommand = whatIsDueCommand;
    }


    public void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 2);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            switch (command) {
                case "/task" -> taskCreationCommand.processUpdate(message);
                case "/edit" -> taskEditingCommand.processUpdate(message);
                case "/due" -> whatIsDueCommand.processUpdate(message);
            }
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        manageCommands(update);
    }

    @Override
    public boolean supports(Update update) {
        if (update != null) {
            if (update.hasMessage() && update.getMessage() != null) {
                if (update.getMessage().hasText() && !update.getMessage().getText().isEmpty()) {
                    return update.getMessage().getText().startsWith("/");
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}

