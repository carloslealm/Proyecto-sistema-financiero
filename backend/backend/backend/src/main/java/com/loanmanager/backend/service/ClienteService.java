package com.loanmanager.backend.service;
 
import com.loanmanager.backend.dto.request.ClienteRequestDTO;
import com.loanmanager.backend.dto.response.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
public interface ClienteService {
    Page<ClienteResponseDTO> listar(String buscar, Pageable pageable);
    ClienteResponseDTO obtenerPorId(Long id);
    ClienteResponseDTO crear(ClienteRequestDTO request);
    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO request);
    void desactivar(Long id);
}