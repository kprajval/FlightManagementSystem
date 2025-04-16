package com.example.FlightManagementSystem.repository;

import com.example.FlightManagementSystem.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f " +
            "WHERE LOWER(f.source) = :source " +
            "AND LOWER(f.destination) = :destination " +
            "AND f.departureDateTime >= :startTime " +
            "AND f.departureDateTime <= :endTime")
    List<Flight> findFlights(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}