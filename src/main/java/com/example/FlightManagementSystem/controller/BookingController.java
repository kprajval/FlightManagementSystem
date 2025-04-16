package com.example.FlightManagementSystem.controller;

import com.example.FlightManagementSystem.model.Booking;
import com.example.FlightManagementSystem.model.Flight;
import com.example.FlightManagementSystem.model.Payment;
import com.example.FlightManagementSystem.model.BankAccount;
import com.example.FlightManagementSystem.repository.BookingRepository;
import com.example.FlightManagementSystem.repository.FlightRepository;
import com.example.FlightManagementSystem.repository.PaymentRepository;
import com.example.FlightManagementSystem.repository.BankAccountRepository;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @PostMapping("/select-seat")
    public String selectSeat(@RequestParam Long flightId, @RequestParam String selectedSeats, @RequestParam String phno, Model model) {
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();
            String[] seats = selectedSeats.split(",");
            if (flight.getAvailableSeats() >= seats.length) {
                model.addAttribute("flightId", flightId);
                model.addAttribute("selectedSeats", selectedSeats);
                int numberOfSeatsSelected = seats.length;
                int totalPrice = flight.getPrice() * numberOfSeatsSelected;
                model.addAttribute("totalPrice", totalPrice);
                model.addAttribute("phno", phno);
                return "payment";
            } else {
                model.addAttribute("errorMessage", "Not enough available seats on this flight.");
                return "error";
            }
        } else {
            model.addAttribute("errorMessage", "Flight not found.");
            return "error";
        }
    }

    @PostMapping("/confirm-payment")
    public String confirmPayment(@RequestParam Long flightId, @RequestParam String selectedSeats, @RequestParam String phno, @RequestParam String paymentMethod, @RequestParam String transactionReference, @RequestParam(required = false) String accountName, Model model) {
        if (phno == null || phno.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Phone number is required.");
            return "error";
        }

        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();
            String[] seats = selectedSeats.split(",");
            int totalPrice = flight.getPrice() * (seats.length);

            Integer accountId = null;

            if (paymentMethod.equals("Bank Account")) {
                if (accountName == null || accountName.trim().isEmpty()) {
                    model.addAttribute("errorMessage", "Account Name is required for Bank Account payments.");
                    return "error";
                }
                BankAccount account = bankAccountRepository.findByAccountName(accountName).orElse(null);
                if (account != null) {
                    accountId = account.getAccountId();
                    if (account.getBalance() < totalPrice) {
                        model.addAttribute("errorMessage", "Insufficient funds in selected account.");
                        return "error";
                    } else {
                        //Move the balance deduction here.
                        account.setBalance(account.getBalance() - totalPrice);
                        bankAccountRepository.save(account);
                    }
                } else {
                    model.addAttribute("errorMessage", "Account not found");
                    return "error";
                }
            }

            if (flight.getAvailableSeats() >= seats.length) {
                List<Booking> bookings = new ArrayList<>();
                for (String seat : seats) {
                    Booking booking = new Booking(flight, seat);
                    sendBoardingPassSMS(phno, flight, seat, booking.getBoardingPassNumber());
                    bookingRepository.save(booking);
                    bookings.add(booking);
                    flight.setAvailableSeats(flight.getAvailableSeats() - 1);
                    Payment payment = new Payment(booking, paymentMethod, flight.getPrice(), "USD", LocalDateTime.now(), "Successful", transactionReference, accountId); //price of one seat
                    payment.setBoardingPassNumber(booking.getBoardingPassNumber());
                    paymentRepository.save(payment);
                }
                flightRepository.save(flight);
                model.addAttribute("bookings", bookings);
                model.addAttribute("flight", flight);
                return "boarding-pass";
            } else {
                model.addAttribute("errorMessage", "Seats are no longer available.");
                return "error";
            }
        } else {
            model.addAttribute("errorMessage", "Flight not found.");
            return "error";
        }
    }

    private void sendBoardingPassSMS(String phoneNumber, Flight flight, String selectedSeat, String boardingPassNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.error("Phone number is null or empty, cannot send SMS.");
            return;
        }
        Twilio.init(accountSid, authToken);
        String messageBody = "Your boarding pass details:\n" +
                "Boarding Pass Number: " + boardingPassNumber + "\n" +
                "Flight Number: " + flight.getFlightNumber() + "\n" +
                "Source: " + flight.getSource() + "\n" +
                "Destination: " + flight.getDestination() + "\n" +
                "Departure Time: " + flight.getDepartureDateTime() + "\n" +
                "Selected Seat: " + selectedSeat + "\n" +
                "Total Price: " + flight.getPrice();
        try {
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();
            logger.info("SMS sent: {}", message.getSid());
        } catch (ApiException e) {
            logger.error("Failed to send SMS: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("General error when sending SMS: {}", e.getMessage());
        }
    }
}