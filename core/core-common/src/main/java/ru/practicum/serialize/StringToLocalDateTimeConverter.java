package ru.practicum.serialize;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private DateTimeFormatter formatter;

    @Value("${explore-with-me.datetime.format}")
    public void setFormatter(String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public LocalDateTime convert(String source) {
        if (source == null || source.isEmpty()) return null;

        try {
            return LocalDateTime.parse(source, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Failed to convert string " + source + " to LocalDateTime");
        }
    }

}
