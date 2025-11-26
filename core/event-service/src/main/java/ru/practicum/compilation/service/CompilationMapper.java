package ru.practicum.compilation.service;

import ru.practicum.compilation.dal.Compilation;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.service.EventMapper;

import java.util.List;
import java.util.Map;

public class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation, Map<Long, UserShortDto> userMap) {
        List<EventShortDto> eventShortDtoList = compilation.getEvents().stream()
                .map(e -> EventMapper.toEventShortDto(e, userMap.get(e.getInitiatorId()), 0L, 0.0))
                .toList();

        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(eventShortDtoList)
                .build();
    }

}