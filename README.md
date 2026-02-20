# Mini-ERP de Pedidos

Sistema de gerenciamento de pedidos desenvolvido com Spring Boot, seguindo princípios de arquitetura em camadas, DDD e SOLID.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 4.0.2
- Spring Data JPA
- PostgreSQL
- OpenFeign
- Bean Validation
- Liquibase
- MapStruct
- Docker e Docker Compose
- JUnit 5 para testes unitários

## Arquitetura

O projeto segue uma arquitetura em camadas com separação clara de responsabilidades:

### Camadas

- **Controller**: Responsável por expor as APIs REST e validar as entradas.
- **Service**: Contém a lógica de negócio da aplicação.
- **Repository**: Responsável pela persistência dos dados.
- **Domain**: Contém as entidades de domínio.
- **DTO**: Objetos de transferência de dados para entrada e saída.
- **Mapper**: Conversão entre entidades e DTOs.

### Domínios Principais

- **Cliente**: Gerenciamento de clientes com integração ao ViaCEP.
- **Produto**: Gerenciamento de produtos e controle de estoque.
- **Pedido**: Gerenciamento de pedidos com regras de negócio específicas.

### Fluxo de Dados

1. As requisições chegam nos Controllers
2. Os Controllers validam as entradas e delegam para os Services
3. Os Services implementam a lógica de negócio e utilizam os Repositories
4. Os Repositories interagem com o banco de dados
5. Os dados são convertidos para DTOs pelos Mappers antes de retornar ao cliente

## Funcionalidades

### Clientes
- CRUD completo
- Integração com ViaCEP para preenchimento automático de endereço
- Validação de CPF e e-mail únicos

### Produtos
- CRUD completo
- Controle de estoque
- Filtros por produtos ativos

### Pedidos
- Criação de pedidos com múltiplos itens
- Cálculo automático de subtotal, desconto e total
- Controle de estoque (baixa ao criar pedido)
- Fluxo de status: CREATED → PAID → CANCELLED ou LATE
- Devolução de estoque ao cancelar pedido não pago

### Tarefas Agendadas
- Verificação de pedidos atrasados (a cada 1 hora)
- Verificação de produtos com estoque abaixo do mínimo (diariamente às 03:00)

## Estrutura do Projeto

```
com.golden.erp
├── config                  # Configurações da aplicação
│   ├── security            # Configurações de segurança
│   ├── feign               # Configurações do OpenFeign
│   └── scheduler           # Configurações de tarefas agendadas
├── controller              # Controladores REST
├── domain                  # Entidades de domínio
├── dto                     # DTOs para entrada e saída
│   ├── request             # DTOs de entrada
│   └── response            # DTOs de saída
├── exception               # Exceções personalizadas e handler global
├── mapper                  # Mapeadores entre entidades e DTOs
├── repository              # Repositórios JPA
├── service                 # Interfaces de serviço
│   └── impl                # Implementações de serviço
├── client                  # Clientes Feign
└── scheduler               # Tarefas agendadas
```

## Executando o Projeto

### Pré-requisitos
- Docker e Docker Compose
- JDK 17 (para desenvolvimento)
- Maven (para desenvolvimento)

### Com Docker Compose

1. Clone o repositório:
```bash
git clone https://github.com/-usuario/erp.git
cd erp
```

2. Execute o Docker Compose:
```bash
docker compose up
```

A aplicação estará disponível em http://localhost:8080

### Sem Docker (Desenvolvimento)

1. Clone o repositório:
```bash
git clone https://github.com/davihugo/erp.git
cd erp
```

2. Configure um banco de dados PostgreSQL local ou ajuste as configurações em `application.properties`.

3. Execute a aplicação:
```bash
./mvnw spring-boot:run
```

## Endpoints da API

### Clientes

- `GET /api/clientes` - Listar todos os clientes (paginado)
- `GET /api/clientes/{id}` - Buscar cliente por ID
- `POST /api/clientes` - Criar novo cliente
- `PUT /api/clientes/{id}` - Atualizar cliente
- `DELETE /api/clientes/{id}` - Excluir cliente
- `GET /api/clientes/por-nome?nome={nome}` - Buscar clientes por nome
- `GET /api/clientes/por-email?email={email}` - Buscar clientes por email

### Produtos

- `GET /api/produtos` - Listar todos os produtos (paginado)
- `GET /api/produtos/ativos` - Listar produtos ativos (paginado)
- `GET /api/produtos/{id}` - Buscar produto por ID
- `POST /api/produtos` - Criar novo produto
- `PUT /api/produtos/{id}` - Atualizar produto
- `DELETE /api/produtos/{id}` - Excluir produto
- `GET /api/produtos/por-nome?nome={nome}` - Buscar produtos por nome
- `GET /api/produtos/ativos/por-nome?nome={nome}` - Buscar produtos ativos por nome
- `GET /api/produtos/estoque-baixo` - Listar produtos com estoque abaixo do mínimo

### Pedidos

- `GET /api/pedidos` - Listar todos os pedidos (paginado)
- `GET /api/pedidos/{id}` - Buscar pedido por ID
- `POST /api/pedidos` - Criar novo pedido
- `GET /api/pedidos/por-status?status={status}` - Listar pedidos por status
- `GET /api/pedidos/por-cliente/{clienteId}` - Listar pedidos de um cliente
- `POST /api/pedidos/{id}/pagar` - Pagar pedido
- `POST /api/pedidos/{id}/cancelar` - Cancelar pedido

## Estratégia de Versionamento Git

O projeto utiliza a seguinte estratégia de branches:

- `main` → Produção
- `homo` → Homologação
- `dev` → Desenvolvimento
- `feature/*` → Desenvolvimento de novas funcionalidades
- `bugfix/*` → Correção de bugs

### Regras:

- Nunca commitar diretamente na `main`
- Desenvolvimento sempre em `dev` ou em branches `feature/*`
- Merge de `dev` para `homo` para homologação
- Merge de `homo` para `main` para produção

## Testes

O projeto inclui testes unitários para as regras de negócio, com foco em:

- Validação de regras de negócio
- Criação de pedidos
- Baixa de estoque
- Cancelamento de pedidos
- Cálculo de totais

Para executar os testes:

```bash
./mvnw test
```

Para gerar relatório de cobertura:

```bash
./mvnw verify
```

O relatório de cobertura estará disponível em `target/site/jacoco/index.html`.

## Melhorias Futuras

- Implementação de autenticação JWT
- Cache da cotação USD para conversão de valores
- Métricas de pedidos criados por hora
- Logs estruturados
- Implementação de API Gateway
- Implementação de microsserviços
