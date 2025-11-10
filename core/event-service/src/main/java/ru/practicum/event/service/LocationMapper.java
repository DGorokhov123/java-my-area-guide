package ru.practicum.event.service;

import ru.practicum.dto.event.LocationDto;
import ru.practicum.event.dal.Location;

public class LocationMapper {

    public static Location toEntity(LocationDto dto) {
        if (dto == null) return null;
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public static LocationDto toDto(Location location) {
        if (location == null) return null;
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

}
