package com.example.rideservice.controller;

import com.example.rideservice.dto.LoginRequestDTO;
import com.example.rideservice.dto.UserRequestDTO;
import com.example.rideservice.model.User;
import com.example.rideservice.repository.UserRepository;
import com.example.rideservice.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody UserRequestDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        user.setVehiclePlate(userDTO.getVehiclePlate());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        userRepository.save(user);
        return "Usuário cadastrado com sucesso!";
    }
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDTO loginRequest) {
        // Buscar usuário pelo e-mail
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        // Validar a senha
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getEmail());
        return "Bearer " + token;
    }
}
