package org.quark;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String filePath = "src/main/resources/tickets.json";

        try {
            ObjectMapper mapper = new ObjectMapper();
            TicketsWrapper ticketsWrapper = mapper.readValue(new File(filePath), TicketsWrapper.class);
            List<Ticket> tickets = ticketsWrapper.tickets;

            // Список билетов для рейсов из Владивостока в Тель-Авив
            List<Ticket> filteredTickets = tickets.stream()
                    .filter(ticket -> "VVO".equals(ticket.origin) && "TLV".equals(ticket.destination))
                    .collect(Collectors.toList());

            // Минимальное время полета для каждого перевозчика
            Map<String, Duration> minFlightTimes = new HashMap<>();
            for (Ticket ticket : filteredTickets) {
                Duration flightDuration = calculateDuration(ticket.departure_time, ticket.arrival_time);
                if (minFlightTimes.containsKey(ticket.carrier)) {
                    if (flightDuration.compareTo(minFlightTimes.get(ticket.carrier)) < 0) {
                        minFlightTimes.put(ticket.carrier, flightDuration);
                    }
                } else {
                    minFlightTimes.put(ticket.carrier, flightDuration);
                }
            }

            System.out.println("Минимальное время полета для каждого авиаперевозчика:");
            for (Map.Entry<String, Duration> entry : minFlightTimes.entrySet()) {
                System.out.println("Авиаперевозчик: " + entry.getKey() + ", Минимальное время полета: " + entry.getValue().toHoursPart() + " ч " + entry.getValue().toMinutesPart() + " мин");
            }

            // Расчет средней цены и медианы
            List<Integer> prices = filteredTickets.stream().map(ticket -> ticket.price).sorted().collect(Collectors.toList());
            double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
            double medianPrice = calculateMedian(prices);

            // Вывод разницы между средней ценой и медианой
            System.out.println("Разница между средней ценой и медианой: " + Math.abs(averagePrice - medianPrice));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Метод для расчета времени полета
    private static Duration calculateDuration(String departureTime, String arrivalTime) {
        departureTime = formatTime(departureTime);
        arrivalTime = formatTime(arrivalTime);

        LocalTime departure = LocalTime.parse(departureTime);
        LocalTime arrival = LocalTime.parse(arrivalTime);
        return Duration.between(departure, arrival);
    }

    // Приведение формата времени с 0:00 к 00:00
    private static String formatTime(String time) {
        if (time.length() < 5) {
            time = "0" + time;
        }
        return time;
    }

    // Метод для расчета медианы
    private static double calculateMedian(List<Integer> prices) {
        int size = prices.size();
        if (size % 2 == 0) {
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            return prices.get(size / 2);
        }
    }
}
