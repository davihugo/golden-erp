package com.golden.erp.service;

import com.golden.erp.client.ViaCepClient;
import com.golden.erp.client.ViaCepResponse;
import com.golden.erp.domain.Cliente;
import com.golden.erp.dto.request.ClienteRequest;
import com.golden.erp.dto.response.ClienteResponse;
import com.golden.erp.exception.CepNotFoundException;
import com.golden.erp.exception.ResourceAlreadyExistsException;
import com.golden.erp.exception.ResourceNotFoundException;
import com.golden.erp.mapper.ClienteMapper;
import com.golden.erp.repository.ClienteRepository;
import com.golden.erp.service.impl.ClienteServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequest clienteRequest;
    private Cliente cliente;
    private ClienteResponse clienteResponse;
    private ViaCepResponse viaCepResponse;

    @BeforeEach
    void setUp() {
        clienteRequest = new ClienteRequest();
        clienteRequest.setNome("João Silva");
        clienteRequest.setEmail("joao@email.com");
        clienteRequest.setCpf("123.456.789-00");
        clienteRequest.setCep("01001-000");
        clienteRequest.setNumero("123");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("123.456.789-00");
        cliente.setCep("01001-000");
        cliente.setLogradouro("Praça da Sé");
        cliente.setNumero("123");
        cliente.setBairro("Sé");
        cliente.setCidade("São Paulo");
        cliente.setUf("SP");

        clienteResponse = new ClienteResponse();
        clienteResponse.setId(1L);
        clienteResponse.setNome("João Silva");
        clienteResponse.setEmail("joao@email.com");
        clienteResponse.setCpf("123.456.789-00");
        clienteResponse.setCep("01001-000");
        clienteResponse.setLogradouro("Praça da Sé");
        clienteResponse.setNumero("123");
        clienteResponse.setBairro("Sé");
        clienteResponse.setCidade("São Paulo");
        clienteResponse.setUf("SP");

        viaCepResponse = new ViaCepResponse();
        viaCepResponse.setCep("01001-000");
        viaCepResponse.setLogradouro("Praça da Sé");
        viaCepResponse.setBairro("Sé");
        viaCepResponse.setLocalidade("São Paulo");
        viaCepResponse.setUf("SP");
        viaCepResponse.setErro(false);
    }

    @Test
    void criar_DeveRetornarClienteResponse_QuandoDadosValidos() {
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(viaCepClient.consultarCep(anyString())).thenReturn(viaCepResponse);
        when(clienteMapper.toEntity(any(ClienteRequest.class))).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        ClienteResponse result = clienteService.criar(clienteRequest);

        assertNotNull(result);
        assertEquals(clienteResponse.getId(), result.getId());
        assertEquals(clienteResponse.getNome(), result.getNome());
        assertEquals(clienteResponse.getEmail(), result.getEmail());
        
        verify(clienteRepository).existsByEmail(clienteRequest.getEmail());
        verify(clienteRepository).existsByCpf(clienteRequest.getCpf());
        verify(viaCepClient).consultarCep(anyString());
        verify(clienteMapper).toEntity(clienteRequest);
        verify(clienteRepository).save(cliente);
        verify(clienteMapper).toResponse(cliente);
    }

    @Test
    void criar_DeveLancarResourceAlreadyExistsException_QuandoEmailJaExiste() {
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            clienteService.criar(clienteRequest);
        });
        
        verify(clienteRepository).existsByEmail(clienteRequest.getEmail());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void criar_DeveLancarResourceAlreadyExistsException_QuandoCpfJaExiste() {
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            clienteService.criar(clienteRequest);
        });
        
        verify(clienteRepository).existsByEmail(clienteRequest.getEmail());
        verify(clienteRepository).existsByCpf(clienteRequest.getCpf());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void criar_DeveLancarCepNotFoundException_QuandoCepInvalido() {
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        
        ViaCepResponse errorResponse = new ViaCepResponse();
        errorResponse.setErro(true);
        when(viaCepClient.consultarCep(anyString())).thenReturn(errorResponse);

        assertThrows(CepNotFoundException.class, () -> {
            clienteService.criar(clienteRequest);
        });
        
        verify(clienteRepository).existsByEmail(clienteRequest.getEmail());
        verify(clienteRepository).existsByCpf(clienteRequest.getCpf());
        verify(viaCepClient).consultarCep(anyString());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void criar_DeveLancarCepNotFoundException_QuandoErroNaConsultaViaCep() {
        
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(viaCepClient.consultarCep(anyString())).thenThrow(FeignException.class);

        assertThrows(CepNotFoundException.class, () -> {
            clienteService.criar(clienteRequest);
        });
        
        verify(clienteRepository).existsByEmail(clienteRequest.getEmail());
        verify(clienteRepository).existsByCpf(clienteRequest.getCpf());
        verify(viaCepClient).consultarCep(anyString());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void buscarPorId_DeveRetornarClienteResponse_QuandoClienteExiste() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        ClienteResponse result = clienteService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(clienteResponse.getId(), result.getId());
        assertEquals(clienteResponse.getNome(), result.getNome());
        
        verify(clienteRepository).findById(1L);
        verify(clienteMapper).toResponse(cliente);
    }

    @Test
    void buscarPorId_DeveLancarResourceNotFoundException_QuandoClienteNaoExiste() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.buscarPorId(1L);
        });
        
        verify(clienteRepository).findById(1L);
        verify(clienteMapper, never()).toResponse(any(Cliente.class));
    }

    @Test
    void listar_DeveRetornarPaginaDeClienteResponse() {
        
        Page<Cliente> clientePage = new PageImpl<>(Collections.singletonList(cliente));
        when(clienteRepository.findAll(any(Pageable.class))).thenReturn(clientePage);
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        Page<ClienteResponse> result = clienteService.listar(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(clienteResponse.getId(), result.getContent().get(0).getId());
        
        verify(clienteRepository).findAll(any(Pageable.class));
        verify(clienteMapper).toResponse(cliente);
    }
}
