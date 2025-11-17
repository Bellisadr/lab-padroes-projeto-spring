package one.digitalinnovation.gof.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 * 
 * @author falvojr
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    // Singleton: Injetar os componentes do Spring com @Autowired.
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private ViaCepService viaCepService;

    // Strategy: Implementar os métodos definidos na interface.
    // Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

    @Override
    public Iterable<Cliente> buscarTodos() {
        // Buscar todos os Clientes.
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        // Buscar Cliente por ID.
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.get();
    }

    @Override
    public void inserir(Cliente cliente) {
        salvarClienteComCep(cliente);
    }

    @Override
    public void atualizar(Long id, Cliente cliente) {
        // Buscar Cliente por ID, caso exista:
        Optional<Cliente> clienteBd = clienteRepository.findById(id);
        if (clienteBd.isPresent()) {
            salvarClienteComCep(cliente);
        }
    }

    @Override
    public void deletar(Long id) {
        // Deletar Cliente por ID.
        clienteRepository.deleteById(id);
    }

    private void salvarClienteComCep(Cliente cliente) {
        // 1. Pegamos o CEP que o usuário digitou
        String cep = cliente.getEndereco().getCep();

        // 2. Buscamos o Endereco no nosso banco de dados.
        //    Se ele não existir, o 'orElseGet()' será executado.
        Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {

            // 3. Buscamos o CEP na API externa (ViaCep)
            Endereco novoEndereco = viaCepService.consultarCep(cep);

            // 4. --- A CORREÇÃO ---
            //    Verificamos se o CEP retornado pelo ViaCep é válido.
            //    Se o CEP for inválido, a API retorna um objeto com 'cep' nulo.
            if (novoEndereco.getCep() == null) {
                // Em vez de salvar e quebrar, nós lançamos nossa própria exceção.
                // (Você pode criar uma exceção customizada depois)
                throw new RuntimeException("CEP inválido ou não encontrado: " + cep);
            }

            // 5. Só salvamos no banco se o CEP for válido!
            enderecoRepository.save(novoEndereco);
            return novoEndereco;
        });

        // 6. Vinculamos o endereço (novo ou existente) ao Cliente
        cliente.setEndereco(endereco);

        // 7. Salvamos o Cliente
        clienteRepository.save(cliente);
    }
}