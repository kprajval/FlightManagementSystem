package com.example.FlightManagementSystem.repository;

import com.example.FlightManagementSystem.model.Booking;
import com.example.FlightManagementSystem.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking(Booking booking);
    Optional<Payment> findByBoardingPassNumber(String boardingPassNumber);
}