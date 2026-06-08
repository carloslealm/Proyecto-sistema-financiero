package com.loanmanager.backend.service;
 
import com.loanmanager.backend.dto.request.PrestamoRequestDTO;
import com.loanmanager.backend.dto.response.CuotaResponseDTO;
import com.loanmanager.backend.dto.response.DashboardResponseDTO;
import com.loanmanager.backend.dto.response.PrestamoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
import java.util.List;
 
public interface PrestamoService {
    Page<PrestamoResponseDTO> listar(Pageable pageable);
    Page<PrestamoResponseDTO> listarPorEstado(String estado, Pageable pageable);
    PrestamoResponseDTO       obtenerPorId(Long id);
    PrestamoResponseDTO       solicitar(PrestamoRequestDTO request, Long asesorId);
    PrestamoResponseDTO       aprobar(Long id, Long asesorId);
    PrestamoResponseDTO       rechazar(Long id, String motivo);
    PrestamoResponseDTO       desembolsar(Long id);
    List<CuotaResponseDTO>    obtenerPlanDePagos(Long prestamoId);
    DashboardResponseDTO      obtenerDashboard();
    void                      detectarYActualizarMora();
}