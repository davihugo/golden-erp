package com.golden.erp.service.impl;

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
import com.golden.erp.service.ClienteService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);
    
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ViaCepClient viaCepClient;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper, ViaCepClient viaCepClient) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.viaCepClient = viaCepClient;
    }

    @Override
    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        // Verificar se já existe cliente com o mesmo email ou CPF
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Cliente", "email", request.getEmail());
        }
        
        if (clienteRepository.existsByCpf(request.getCpf())) {
            throw new ResourceAlreadyExistsException("Cliente", "cpf", request.getCpf());
        }
        
        // Consultar o CEP e preencher os dados de endereço
        preencherEndereco(request);
        
        // Converter para entidade e salvar
        Cliente cliente = clienteMapper.toEntity(request);
        cliente = clienteRepository.save(cliente);
        
        return clienteMapper.toResponse(cliente);
    }

    @Override
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        
        // Verificar se já existe outro cliente com o mesmo email ou CPF
        clienteRepository.findByEmail(request.getEmail())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException("Cliente", "email", request.getEmail());
                    }
                });
        
        clienteRepository.findByCpf(request.getCpf())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException("Cliente", "cpf", request.getCpf());
                    }
                });
        
        // Consultar o CEP e preencher os dados de endereço se o CEP foi alterado
        if (!cliente.getCep().equals(request.getCep())) {
            preencherEndereco(request);
        }
        
        // Atualizar a entidade e salvar
        clienteMapper.updateEntityFromRequest(request, cliente);
        cliente = clienteRepository.save(cliente);
        
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", "id", id);
        }
        
        clienteRepository.deleteById(id);
    }

    @Override
    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable)
                .map(clienteMapper::toResponse);
    }

    @Override
    public Page<ClienteResponse> buscarPorNome(String nome, Pageable pageable) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(clienteMapper::toResponse);
    }

    @Override
    public Page<ClienteResponse> buscarPorEmail(String email, Pageable pageable) {
        return clienteRepository.findByEmailContainingIgnoreCase(email, pageable)
                .map(clienteMapper::toResponse);
    }
    
    private void preencherEndereco(ClienteRequest request) {
        String cep = request.getCep().replaceAll("\\D", "");
        
        try {
            logger.info("Consultando CEP: {}", cep);
            ViaCepResponse viaCepResponse = viaCepClient.consultarCep(cep);
            
            if (viaCepResponse.isErro()) {
                logger.error("CEP não encontrado: {}", cep);
                throw new CepNotFoundException(cep);
            }
            
            // Preencher os dados de endereço apenas se não foram fornecidos
            if (request.getLogradouro() == null || request.getLogradouro().isEmpty()) {
                request.setLogradouro(viaCepResponse.getLogradouro());
            }
            
            if (request.getBairro() == null || request.getBairro().isEmpty()) {
                request.setBairro(viaCepResponse.getBairro());
            }
            
            if (request.getCidade() == null || request.getCidade().isEmpty()) {
                request.setCidade(viaCepResponse.getLocalidade());
            }
            
            if (request.getUf() == null || request.getUf().isEmpty()) {
                request.setUf(viaCepResponse.getUf());
            }
            
            logger.info("CEP consultado com sucesso: {}", cep);
        } catch (FeignException e) {
            logger.error("Erro ao consultar CEP: {}", cep, e);
            throw new CepNotFoundException(cep);
        }
    }
}
