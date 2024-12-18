package com.example.rideservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rides")
public class Ride {
	// Informacoes das corridas
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger; // Relacionamento com o passageiro

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver; // Relacionamento com o motorista

    private String origin; // Coordenadas de origem
    private String destination; // Coordenadas de destino

    @Enumerated(EnumType.STRING)
    private Status status; // Status da corrida: AGUARDANDO, EM_ANDAMENTO, FINALIZADA, CANCELADA

    private Double distance; // Distância em metros
    private Double price; // Preço calculado

    public enum Status {
        AGUARDANDO, EM_ANDAMENTO, FINALIZADA, CANCELADA
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
