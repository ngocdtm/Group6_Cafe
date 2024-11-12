package com.coffee.utils;

import com.coffee.wrapper.DateRange;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

@UtilityClass
public class DateUtils {

    public static DateRange calculateDateRange(String timeFrame, LocalDate specificDate,
                                               LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return new DateRange(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        LocalDateTime now = LocalDateTime.now();

        if (specificDate != null) {
            return switch (timeFrame.toLowerCase()) {
                case "daily" -> new DateRange(
                        specificDate.atStartOfDay(),
                        specificDate.atTime(LocalTime.MAX)
                );
                case "weekly" -> {
                    LocalDate startOfWeek = specificDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate endOfWeek = specificDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    yield new DateRange(startOfWeek.atStartOfDay(), endOfWeek.atTime(LocalTime.MAX));
                }
                case "monthly" -> {
                    LocalDate startOfMonth = specificDate.withDayOfMonth(1);
                    LocalDate endOfMonth = specificDate.with(TemporalAdjusters.lastDayOfMonth());
                    yield new DateRange(startOfMonth.atStartOfDay(), endOfMonth.atTime(LocalTime.MAX));
                }
                case "yearly" -> {
                    LocalDate startOfYear = specificDate.withDayOfYear(1);
                    LocalDate endOfYear = specificDate.with(TemporalAdjusters.lastDayOfYear());
                    yield new DateRange(startOfYear.atStartOfDay(), endOfYear.atTime(LocalTime.MAX));
                }
                default -> new DateRange(
                        specificDate.atStartOfDay(),
                        specificDate.atTime(LocalTime.MAX)
                );
            };
        }

        // Default ranges for current period
        return switch (timeFrame.toLowerCase()) {
            case "daily" -> new DateRange(
                    now.truncatedTo(ChronoUnit.DAYS),
                    now.with(LocalTime.MAX)
            );
            case "weekly" -> {
                LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate();
                LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate();
                yield new DateRange(startOfWeek.atStartOfDay(), endOfWeek.atTime(LocalTime.MAX));
            }
            case "monthly" -> {
                LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate();
                LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate();
                yield new DateRange(startOfMonth.atStartOfDay(), endOfMonth.atTime(LocalTime.MAX));
            }
            case "yearly" -> {
                LocalDate startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate();
                LocalDate endOfYear = now.with(TemporalAdjusters.lastDayOfYear()).toLocalDate();
                yield new DateRange(startOfYear.atStartOfDay(), endOfYear.atTime(LocalTime.MAX));
            }
            default -> {
                // Default to current month
                LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate();
                LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate();
                yield new DateRange(startOfMonth.atStartOfDay(), endOfMonth.atTime(LocalTime.MAX));
            }
        };
    }

    public static String formatPeriod(LocalDateTime date, String timeFrame) {
        return switch (timeFrame.toLowerCase()) {
            case "daily" -> date.format(DateTimeFormatter.ISO_DATE);
            case "weekly" -> {
                LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate();
                LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate();
                yield startOfWeek.format(DateTimeFormatter.ISO_DATE) + " to " +
                        endOfWeek.format(DateTimeFormatter.ISO_DATE);
            }
            case "monthly" -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "yearly" -> date.format(DateTimeFormatter.ofPattern("yyyy"));
            default -> date.format(DateTimeFormatter.ISO_DATE);
        };
    }
}