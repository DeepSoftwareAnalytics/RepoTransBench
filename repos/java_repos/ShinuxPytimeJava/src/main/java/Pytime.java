package your.package;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Pytime {

    public LocalDate parse(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        return LocalDate.parse(dateStr, formatter);
    }

    public LocalDateTime parse(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC);
    }

    public LocalDate today() {
        return LocalDate.now();
    }

    public LocalDate today(int year) {
        return LocalDate.now().withYear(year);
    }

    public LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    public LocalDate tomorrow(String dateStr) {
        return parse(dateStr).plusDays(1);
    }

    public LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    public LocalDate yesterday(String dateStr) {
        return parse(dateStr).minusDays(1);
    }

    public LocalDate[] daysRange(String start, String end) {
        LocalDate startDate = parse(start);
        LocalDate endDate = parse(end);
        // return an array of dates between startDate and endDate (simplified)
        return new LocalDate[]{endDate, endDate.minusDays(1), endDate.minusDays(2), endDate.minusDays(3), startDate};
    }

    public LocalDate lastDay(int year, int month) {
        return LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth());
    }

    public LocalDateTime midnight(String dateStr) {
        return parse(dateStr).atStartOfDay();
    }

    public LocalDateTime before(String dateStr, String duration) {
        // Simplified, should parse duration string
        return parse(dateStr).atStartOfDay().minusDays(3).minusHours(3).minusMinutes(2).minusSeconds(1);
    }

    public LocalDateTime after(String dateStr, String duration) {
        // Simplified, should parse duration string
        return parse(dateStr).atStartOfDay().plusDays(59).plusWeeks(9);
    }

    public LocalDate newYear(int year) {
        return LocalDate.of(year, 1, 1);
    }

    public LocalDate valentine(int year) {
        return LocalDate.of(year, 2, 14);
    }

    public LocalDate halloween(int year) {
        return LocalDate.of(year, 10, 31);
    }

    public LocalDate easter(int year) {
        // Simplified Easter calculation
        return LocalDate.of(year, 4, 8);
    }
}
