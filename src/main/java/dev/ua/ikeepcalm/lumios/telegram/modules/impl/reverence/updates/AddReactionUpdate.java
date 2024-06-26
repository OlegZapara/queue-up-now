package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.updates;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.ReverenceReaction;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.UpdateParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

@Component
public class AddReactionUpdate extends UpdateParent {

    @Override
    public void processUpdate(Update update) {
        try {
            MessageReactionUpdated message = update.getMessageReaction();
            ReverenceReaction newReaction = findNewReaction(message.getOldReaction(), message.getNewReaction());
            int reactionValue = ReverenceReaction.determineReactionValue(newReaction);

            if (reverenceUser.getCredits() > reactionValue) {
                ReverenceUser onUser = recordService.findByMessageIdAndChatId(Long.valueOf(message.getMessageId()), message.getChat().getId()).getUser();
                if (!reverenceUser.getUsername().equals(onUser.getUsername())) {
                    reverenceUser.setCredits(reverenceUser.getCredits() - Math.abs(reactionValue));
                    onUser.setReverence(onUser.getReverence() + reactionValue);
                    userService.save(reverenceUser);
                    userService.save(onUser);
                }
            }
        } catch (NoSuchEntityException ignored) {
        }
    }

}

