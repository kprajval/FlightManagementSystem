package com.example.FlightManagementSystem.controller;

import com.example.FlightManagementSystem.model.Booking;
import com.example.FlightManagementSystem.model.Flight;
import com.example.FlightManagementSystem.repository.BookingRepository;
import com.example.FlightManagementSystem.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BookingRepository bookingRepository; // Inject BookingRepository

    @GetMapping("/")
    public String showSearchForm() {
        return "search";
    }

    @PostMapping("/search")
    public String searchFlights(@RequestParam String source,
                                 @RequestParam String destination,
                                 @RequestParam(name = "travelDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate travelDate,
                                 Model model) {
        try {
            ZoneId kolkataZoneId = ZoneId.of("Asia/Kolkata");
            LocalDateTime currentTimeBengaluru = LocalDateTime.now(kolkataZoneId);

            LocalDateTime searchStartTime = LocalDateTime.of(travelDate, currentTimeBengaluru.toLocalTime());
            LocalDateTime endOfDay = travelDate.atTime(LocalTime.MAX);

            List<Flight> flights = flightRepository.findFlights(
                    source.trim().toLowerCase(),
                    destination.trim().toLowerCase(),
                    searchStartTime,
                    endOfDay
            );
            model.addAttribute("flights", flights);
            return "results";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Invalid date format. Please use<ctrl3348>-MM-dd.");
            return "search";
        }
    }

    @GetMapping("/book/{flightId}")
    public String showBookingPage(@PathVariable Long flightId, Model model) {
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isPresent()) {
            model.addAttribute("flight", flightOptional.get());

            List<Booking> bookings = bookingRepository.findByFlight_FlightId(flightId);
            List<String> bookedSeats = bookings.stream()
                    .map(Booking::getSelectedSeat)
                    .collect(Collectors.toList());
            model.addAttribute("bookedSeats", bookedSeats);
            return "booking";
        } else {
            model.addAttribute("errorMessage", "Flight not found.");
            return "error";
        }
    }
}