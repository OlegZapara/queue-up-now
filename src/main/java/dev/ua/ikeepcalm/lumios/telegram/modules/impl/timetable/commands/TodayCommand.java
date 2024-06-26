package dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.commands;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.utils.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class TodayCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());
            DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

            StringBuilder messageBuilder = new StringBuilder("\uD83D\uDCC5> *РОЗКЛАД НА СЬОГОДНІ* <\uD83D\uDCC5 \n\n");
            messageBuilder.append("``` \uD83D\uDD35 - ЛЕКЦІЯ\n \uD83D\uDFE0 - ПРАКТИКА\n \uD83D\uDFE2 - ЛАБОРАТОРНА```\n\n");

            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                        messageBuilder.append("*").append(classEntry.getStartTime()).append(" - ").append(classEntry.getEndTime()).append("*\n");
                        messageBuilder.append(TimetableParser.parseClassEmoji(classEntry.getClassType())).append(" [").append(classEntry.getName()).append("]");
                        messageBuilder.append("(").append(classEntry.getUrl()).append(")\n\n");
                    }
                }
            }
            sendMessage(messageBuilder.toString(), ParseMode.MARKDOWN);
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розкладу на сьогодні! Ви точно все налаштували?");
        }
    }


}
