# Padrões de Projeto com Spring Boot
Este projeto é uma solução para o desafio "Lab - Padrões de Projeto com Spring" da Digital Innovation One (DIO).
O objetivo foi explorar como os Padrões de Projeto (Design Patterns) GoF (Gang of Four) são implementados de forma moderna utilizando o Spring Framework.

O projeto base, fornecido pela DIO, implementa uma API RESTful para um cadastro de Clientes que consome uma API externa (ViaCep) para preenchimento de endereços.

Os padrões explorados são:

Singleton: Todas as classes de serviço (@Service, @RestController) são gerenciadas pelo Spring como Singletons (uma única instância) por padrão.

Strategy: O projeto utiliza diferentes "estratégias" de serviço, como o ClienteService e o ViaCepService, que podem ser injetados e trocados.

Facade: A classe ClienteServiceImpl atua como uma "Fachada", escondendo a complexidade de múltiplos subsistemas (consultar o ViaCepService, salvar o EnderecoRepository, salvar o ClienteRepository) por trás de um método simples como inserir(cliente).

## Minhas Melhorias e Correções (Além do Desafio)
Durante a análise e teste do projeto original, identifiquei e corrigi bugs críticos relacionados ao tratamento de erros, tornando a API mais robusta e profissional.

1. Correção de Bug Crítico: 500 Internal Server Error em CEPs Inválidos
O Problema (Bug): Ao tentar criar um cliente com um CEP inválido (mas bem formatado, ex: 09000100), a API externa ViaCep retornava um JSON com {"erro": "true"}.
O ClienteServiceImpl não verificava isso e tentava salvar um objeto Endereco com cep = null no banco de dados. Como cep é o @Id, isso causava uma IdentifierGenerationException e quebrava a aplicação (Erro 500).

A Correção (em ClienteServiceImpl): Adicionei uma verificação de nulidade. O serviço agora inspeciona a resposta do ViaCepService antes de tentar salvar no banco.
Se novoEndereco.getCep() for null, ele lança uma exceção customizada (RuntimeException) e impede que dados inválidos corrompam o banco.

A Solução (Padrão de API REST): Criei a classe GlobalExceptionHandler.java anotada com @ControllerAdvice.

Resultado:

Erros de FeignException.NotFound (CEP que o ViaCep retorna 404) agora são interceptados e retornam um 404 Not Found limpo com a mensagem "CEP não encontrado.".

Erros de RuntimeException (como o "CEP inválido" que criamos) agora retornam um 400 Bad Request com uma mensagem clara, em vez de quebrar o servidor.
