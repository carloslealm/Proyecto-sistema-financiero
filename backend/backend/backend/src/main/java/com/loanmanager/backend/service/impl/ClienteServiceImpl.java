package com.loanmanager.backend.service.impl;
 
import com.loanmanager.backend.dto.request.ClienteRequestDTO;
import com.loanmanager.backend.dto.response.ClienteResponseDTO;
import com.loanmanager.backend.entity.Cliente;
import com.loanmanager.backend.exception.BadRequestException;
import com.loanmanager.backend.exception.ResourceNotFoundException;
import com.loanmanager.backend.repository.ClienteRepository;
import com.loanmanager.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
 
    private final ClienteRepository clienteRepository;
 
    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(String buscar, Pageable pageable) {
        Page<Cliente> clientes;
 
        // Si hay término de búsqueda, usar el query especial.
        // Si no, listar todos los activos con paginación normal.
        if (buscar != null && !buscar.trim().isEmpty()) {
            clientes = clienteRepository.buscarPorTermino(buscar.trim(), pageable);
        } else {
            clientes = clienteRepository.findByActivoTrue(pageable);
        }
 
        // Page.map() transforma cada elemento sin perder
        // la información de paginación (totalElements, totalPages, etc.)
        return clientes.map(this::toResponseDTO);
    }
 
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return toResponseDTO(cliente);
    }
 
    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO request) {
        // Validar unicidad de cédula
        if (clienteRepository.existsByCedula(request.getCedula())) {
            throw new BadRequestException(
                "Ya existe un cliente con la cédula: " + request.getCedula()
            );
        }
 
        // Validar unicidad de email si fue proporcionado
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && clienteRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                "Ya existe un cliente con el email: " + request.getEmail()
            );
        }
 
        Cliente cliente = toEntity(request);
        cliente = clienteRepository.save(cliente);
 
        log.info("Cliente creado: {} - {}", cliente.getId(), cliente.getCedula());
        return toResponseDTO(cliente);
    }
 
    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
 
        // Validar cédula si cambió
        if (!cliente.getCedula().equals(request.getCedula())
                && clienteRepository.existsByCedula(request.getCedula())) {
            throw new BadRequestException(
                "Ya existe un cliente con la cédula: " + request.getCedula()
            );
        }
 
        // Actualizar campos
        cliente.setCedula(request.getCedula());
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setCiudad(request.getCiudad());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setIngresoMensual(request.getIngresoMensual());
 
        cliente = clienteRepository.save(cliente);
 
        log.info("Cliente actualizado: {}", cliente.getId());
        return toResponseDTO(cliente);
    }
 
    @Override
    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
 
        // Soft delete: no borramos, solo desactivamos.
        // Los datos financieros deben ser auditables siempre.
        cliente.setActivo(false);
        clienteRepository.save(cliente);
 
        log.info("Cliente desactivado: {}", id);
    }
 
    // ── Métodos de conversión ─────────────────────────────────
    // En un proyecto más grande usaríamos MapStruct para esto.
    // Aquí lo hacemos manual para que entiendas el proceso.
 
    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
            .id(cliente.getId())
            .cedula(cliente.getCedula())
            .nombre(cliente.getNombre())
            .apellido(cliente.getApellido())
            .nombreCompleto(cliente.getNombreCompleto())
            .email(cliente.getEmail())
            .telefono(cliente.getTelefono())
            .direccion(cliente.getDireccion())
            .ciudad(cliente.getCiudad())
            .fechaNacimiento(cliente.getFechaNacimiento())
            .ingresoMensual(cliente.getIngresoMensual())
            .activo(cliente.getActivo())
            // size() en la lista lazy solo funciona dentro de @Transactional
            .totalPrestamos(cliente.getPrestamos().size())
            .createdAt(cliente.getCreatedAt())
            .build();
    }
 
    private Cliente toEntity(ClienteRequestDTO dto) {
        return Cliente.builder()
            .cedula(dto.getCedula())
            .nombre(dto.getNombre())
            .apellido(dto.getApellido())
            .email(dto.getEmail())
            .telefono(dto.getTelefono())
            .direccion(dto.getDireccion())
            .ciudad(dto.getCiudad())
            .fechaNacimiento(dto.getFechaNacimiento())
            .ingresoMensual(dto.getIngresoMensual())
            .activo(true)
            .build();
    }
}