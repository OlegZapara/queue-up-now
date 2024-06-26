package dev.ua.ikeepcalm.lumios.database.dal.interfaces;


import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

import java.util.List;

public interface TimetableService {

    void save(TimetableEntry timetableEntry);

    void delete(TimetableEntry timetableEntry);

    void deleteAll(List<TimetableEntry> timetableEntries);

    TimetableEntry findByChatIdAndWeekType(Long chatId, WeekType weekType) throws NoSuchEntityException;

    List<TimetableEntry> findAllByChatId(Long chatId) throws NoSuchEntityException;
}

