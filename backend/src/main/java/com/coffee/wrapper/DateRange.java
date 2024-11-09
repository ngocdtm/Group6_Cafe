package com.coffee.wrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DateRange {
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
}
