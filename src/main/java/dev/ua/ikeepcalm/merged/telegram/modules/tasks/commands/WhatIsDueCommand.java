package dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands;

import dev.ua.ikeepcalm.merged.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WhatIsDueCommand extends CommandParent {
    public void execute(Message origin) {
        Long chatId = origin.getChatId();
        List<DueTask> tasks = taskService.getTasksForCurrentChat(chatService.find(chatId));

        if (!tasks.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDate maxDueDate = today.plusDays(14);
            List<DueTask> filteredTasks = tasks.stream()

                    .filter(task -> !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(maxDueDate)
                            && !(task.getDueDate().isEqual(today) && task.getDueTime().isBefore(LocalTime.now())))
                    .toList();

            Map<LocalDate, List<DueTask>> groupedTasks = filteredTasks.stream()
                    .collect(Collectors.groupingBy(DueTask::getDueDate));

            Map<LocalDate, List<DueTask>> sortedTasks = groupedTasks.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


            DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("uk"));
            StringBuilder messageBuilder = new StringBuilder();

            for (LocalDate dueDate : sortedTasks.keySet()) {
                List<DueTask> tasksForDate = groupedTasks.get(dueDate);
                LocalDate tomorrow = today.plusDays(1);

                if (dueDate.isEqual(today)) {
                    messageBuilder.append("\n*СЬОГОДНІ*\n");
                } else if (dueDate.isEqual(tomorrow)) {
                    messageBuilder.append("\n*ЗАВТРА*\n");
                } else {
                    messageBuilder.append("\n*").append(dayOfWeekFormatter.format(dueDate.getDayOfWeek()).toUpperCase()).append("*\n");
                }

                for (DueTask task : tasksForDate) {
                    messageBuilder.append("(ID:").append(task.getId()).append(") > ").append(task.getDueTime()).append(" - ").append("[").append(task.getTaskName()).append("](").append(task.getUrl()).append(")\n");
                }
            }

            sendMessage(origin, messageBuilder.toString(), true);
        } else {
            sendMessage(origin, "Нічого немає, можна відпочивати!");
        }
    }
}