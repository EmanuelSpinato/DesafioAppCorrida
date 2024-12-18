package com.example.rideservice.repository;

import com.example.rideservice.model.Ride;
import com.example.rideservice.model.Ride.Status;
import com.example.rideservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    // Método para buscar corridas por status
    List<Ride> findByStatus(Status status);

    // Verifica se o motorista possui corrida em andamento
    boolean existsByDriverAndStatus(User driver, Status status);
    
    // Buscar todas as corridas de um passageiro
    List<Ride> findByPassenger(User passenger);
    
    // existe pelo menos uma corrida associada a um passageiro específico em um conjunto de status
    boolean existsByPassengerAndStatusIn(User passenger, List<Ride.Status> statuses);

}
