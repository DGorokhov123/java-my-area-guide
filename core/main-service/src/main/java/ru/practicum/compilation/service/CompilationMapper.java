package ru.practicum.compilation.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.compilation.dal.Compilation;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.service.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> eventShortDtoList = compilation.getEvents().stream()
                .map(event ->
                        EventMapper.toEventShortDto(event, 0L, 0L)
                ).collect(Collectors.toList());

        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(eventShortDtoList)
                .build();
    }

    public static List<CompilationDto> toCompilationDtoList(List<Compilation> compilations) {
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

}