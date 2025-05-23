package com.example.FlightManagementSystem.repository;

import com.example.FlightManagementSystem.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBoardingPassNumber(String boardingPassNumber);

    List<Booking> findByFlight_FlightId(Long flightId);
}