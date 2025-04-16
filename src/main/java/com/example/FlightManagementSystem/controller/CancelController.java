package com.example.FlightManagementSystem.controller;

import com.example.FlightManagementSystem.model.Booking;
import com.example.FlightManagementSystem.model.Flight;
import com.example.FlightManagementSystem.model.Payment;
import com.example.FlightManagementSystem.model.BankAccount;
import com.example.FlightManagementSystem.repository.BookingRepository;
import com.example.FlightManagementSystem.repository.FlightRepository;
import com.example.FlightManagementSystem.repository.PaymentRepository;
import com.example.FlightManagementSystem.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class CancelController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @GetMapping("/cancel-ticket")
    public String showCancelForm() {
        return "cancel";
    }

    @PostMapping("/cancel-ticket")
    public String cancelTicket(@RequestParam String boardingPassNumber, Model model) {
        try {
            Optional<Booking> bookingOptional = bookingRepository.findByBoardingPassNumber(boardingPassNumber);

            if (bookingOptional.isPresent()) {
                Booking booking = bookingOptional.get();
                Flight flightDetails = booking.getFlight();

                Payment payment = paymentRepository.findByBoardingPassNumber(boardingPassNumber).orElse(null);

                if (payment != null && payment.getAccountId() != null) {
                    BankAccount account = bankAccountRepository.findById(payment.getAccountId()).orElse(null);
                    if (account != null) {
                        account.setBalance(account.getBalance() + payment.getAmount());
                        bankAccountRepository.save(account);
                    }
                    paymentRepository.delete(payment);
                }

                bookingRepository.delete(booking);

                int numberOfSeats = booking.getSelectedSeat().split(",").length;
                flightDetails.setAvailableSeats(flightDetails.getAvailableSeats() + numberOfSeats);
                flightRepository.save(flightDetails);

                model.addAttribute("cancellationMessage", "Your ticket with boarding pass number " + boardingPassNumber + " has been cancelled and refunded.");

            } else {
                model.addAttribute("cancellationMessage", "Invalid boarding pass number.");
            }
        } catch (Exception e) {
            model.addAttribute("cancellationMessage", "An error occurred during cancellation.");
        }
        return "cancel";
    }
}