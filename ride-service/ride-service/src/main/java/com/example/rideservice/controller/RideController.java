package com.example.rideservice.controller;

import com.example.rideservice.dto.RideRequestDTO;
import com.example.rideservice.model.Ride;
import com.example.rideservice.model.User;
import com.example.rideservice.model.Ride.Status;
import com.example.rideservice.repository.RideRepository;
import com.example.rideservice.repository.UserRepository;
import com.example.rideservice.util.JwtUtil;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/request")
    public String requestRide(@RequestBody RideRequestDTO rideRequest) {
        User passenger = userRepository.findById(rideRequest.getPassengerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passageiro não encontrado"));

        // Validar se o passageiro já possui corrida aguardando ou em andamento
        boolean hasActiveRide = rideRepository.existsByPassengerAndStatusIn(passenger,
                List.of(Status.AGUARDANDO, Status.EM_ANDAMENTO));

        if (hasActiveRide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passageiro já possui uma corrida ativa");
        }

        Ride ride = new Ride();
        ride.setPassenger(passenger);
        ride.setOrigin(rideRequest.getOrigin());
        ride.setDestination(rideRequest.getDestination());
        ride.setStatus(Status.AGUARDANDO);

        rideRepository.save(ride);
        return "Corrida solicitada com sucesso!";
    }

    @GetMapping("/available")
    public Iterable<Ride> getAvailableRides() {
        // Retornar apenas corridas com status "AGUARDANDO"
        return rideRepository.findByStatus(Status.AGUARDANDO);
    }
    
    @PatchMapping("/{rideId}/accept")
    public String acceptRide(@PathVariable Long rideId, @RequestParam Long driverId) {
        // Buscar corrida pelo ID
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Corrida não encontrada"));

        // Verificar se a corrida está com status AGUARDANDO
        if (!ride.getStatus().equals(Status.AGUARDANDO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corrida não está disponível para aceitação");
        }

        // Buscar motorista pelo ID
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motorista não encontrado"));

        // Verificar se o motorista já possui corrida em andamento
        boolean hasOngoingRide = rideRepository.existsByDriverAndStatus(driver, Status.EM_ANDAMENTO);
        if (hasOngoingRide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motorista já possui uma corrida em andamento");
        }

        // Aceitar a corrida
        ride.setDriver(driver);
        ride.setStatus(Status.EM_ANDAMENTO);
        rideRepository.save(ride);

        return "Corrida aceita pelo motorista " + driver.getName();
    }
    
    @PatchMapping("/{rideId}/finish")
    public String finishRide(@PathVariable Long rideId, @RequestParam double distance) {
        // Buscar corrida pelo ID
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Corrida não encontrada"));

        // Verificar se a corrida está com status EM_ANDAMENTO
        if (!ride.getStatus().equals(Status.EM_ANDAMENTO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A corrida não está em andamento");
        }

        // Calcular o preço da corrida (R$ 2,00 por km)
        double distanceInKm = distance / 1000; // Convertendo metros para km
        double price = distanceInKm * 2.0;

        // Encerrar a corrida
        ride.setDistance(distance);
        ride.setPrice(price);
        ride.setStatus(Status.FINALIZADA);
        rideRepository.save(ride);

        return "Corrida finalizada! Distância: " + distanceInKm + " km, Preço: R$ " + price;
    }
    
    
    // Listar todas as corridas de um passageiro
    @GetMapping
    public List<Ride> getRidesByPassenger(@RequestParam Long passengerId) {
        // Verificar se o passageiro existe
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passageiro não encontrado"));

        // Buscar corridas associadas ao passageiro
        return rideRepository.findByPassenger(passenger);
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findByEmail(email) // Buscar email fornecido
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        // Comparar a senha fornecida com a senha armazenada
        if (!passwordEncoder.matches(password, user.getPassword())) { 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        String token = jwtUtil.generateToken(email); // Gerar um token JWT para o usuário autenticado
        return "Bearer " + token;
    }
    
    @PatchMapping("/{rideId}/cancel")
    public String cancelRide(@PathVariable Long rideId) {
        Ride ride = rideRepository.findById(rideId) // Buscar a corrida pelo ID fornecido
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Corrida não encontrada"));

        if (!ride.getStatus().equals(Status.AGUARDANDO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente corridas aguardando podem ser canceladas");
        }

        ride.setStatus(Status.CANCELADA); // Alterar o status da corrida para "CANCELADA"
        rideRepository.save(ride);

        return "Corrida cancelada com sucesso!";
    }

    
}   
