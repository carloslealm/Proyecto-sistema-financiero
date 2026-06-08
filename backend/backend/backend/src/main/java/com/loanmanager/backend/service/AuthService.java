package com.loanmanager.backend.service;
 
import com.loanmanager.backend.dto.request.LoginRequestDTO;
import com.loanmanager.backend.dto.request.RegistroRequestDTO;
import com.loanmanager.backend.dto.response.AuthResponseDTO;
 
public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO registro(RegistroRequestDTO request);
}