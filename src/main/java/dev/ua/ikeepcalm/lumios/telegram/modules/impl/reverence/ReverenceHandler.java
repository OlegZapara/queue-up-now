package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence;

import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.commands.MeCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.commands.ResetCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.commands.StatsCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.updates.AddReactionUpdate;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.updates.RemoveReactionUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

@Component
public class ReverenceHandler implements HandlerParent {

    private final AddReactionUpdate addReactionUpdate;
    private final RemoveReactionUpdate removeReactionUpdate;
    private final MeCommand meCommand;
    private final StatsCommand statsCommand;
    private final ResetCommand resetCommand;

    public ReverenceHandler(AddReactionUpdate addReactionUpdate,
                            RemoveReactionUpdate removeReactionUpdate,
                            MeCommand meCommand, StatsCommand statsCommand, ResetCommand resetCommand) {
        this.addReactionUpdate = addReactionUpdate;
        this.removeReactionUpdate = removeReactionUpdate;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
        this.resetCommand = resetCommand;
    }


    @Override
    public void dispatchUpdate(Update update) {
        if (update.getMessageReaction() != null) {
            MessageReactionUpdated reactionUpdated = update.getMessageReaction();
            int oldCount = reactionUpdated.getOldReaction().size();
            int newCount = reactionUpdated.getNewReaction().size();
            if (oldCount < newCount) {
                addReactionUpdate.handleUpdate(update);
            } else if (oldCount > newCount) {
                removeReactionUpdate.handleUpdate(update);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getChat().getType().equals("private")) {
                return;
            }
            String commandText = update.getMessage().getText();
            String[] parts = commandText.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            command = command.replace("@lumios_bot", "");
            switch (command) {
                case "/me" -> meCommand.handleUpdate(update.getMessage());
                case "/stats", "/rating" -> statsCommand.handleUpdate(update.getMessage());
                case "/reset" -> resetCommand.handleUpdate(update.getMessage());
            }
        }
    }

    @Override
    public boolean supports(Update update) {
        if (update.getMessageReaction() != null) {
            return true;
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                return update.getMessage().getText().startsWith("/");
            } else {
                return update.hasCallbackQuery();
            }
        }
    }
}
