package ru.practicum.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.EventHitDto;
import ru.practicum.model.Stat;

@Mapper(componentModel = "spring")
public interface StatMapper {

    StatMapper INSTANCE = Mappers.getMapper(StatMapper.class);

    Stat toStat(EventHitDto statDto);

    EventHitDto toEventHitDto(Stat stat);

}
