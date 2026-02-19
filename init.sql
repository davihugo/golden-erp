-- Criação da tabela cliente
CREATE TABLE IF NOT EXISTS cliente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(255),
    bairro VARCHAR(255) NOT NULL,
    cidade VARCHAR(255) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cliente_email ON cliente(email);
CREATE INDEX IF NOT EXISTS idx_cliente_cpf ON cliente(cpf);

-- Criação da tabela produto
CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    preco_bruto DECIMAL(10, 2) NOT NULL,
    estoque INT NOT NULL,
    estoque_minimo INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_produto_sku ON produto(sku);
CREATE INDEX IF NOT EXISTS idx_produto_ativo ON produto(ativo);

-- Criação da tabela pedido
CREATE TABLE IF NOT EXISTS pedido (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    desconto_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE INDEX IF NOT EXISTS idx_pedido_cliente ON pedido(cliente_id);
CREATE INDEX IF NOT EXISTS idx_pedido_status ON pedido(status);

-- Criação da tabela pedido_item
CREATE TABLE IF NOT EXISTS pedido_item (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    desconto DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_pedido_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_pedido_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
);

CREATE INDEX IF NOT EXISTS idx_pedido_item_pedido ON pedido_item(pedido_id);
CREATE INDEX IF NOT EXISTS idx_pedido_item_produto ON pedido_item(produto_id);

-- Criação da tabela de controle do Liquibase (para evitar que o Liquibase tente criar as tabelas novamente)
CREATE TABLE IF NOT EXISTS databasechangelog (
    id VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    dateexecuted TIMESTAMP NOT NULL,
    orderexecuted INT NOT NULL,
    exectype VARCHAR(10) NOT NULL,
    md5sum VARCHAR(35),
    description VARCHAR(255),
    comments VARCHAR(255),
    tag VARCHAR(255),
    liquibase VARCHAR(20),
    contexts VARCHAR(255),
    labels VARCHAR(255),
    deployment_id VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS databasechangeloglock (
    id INT NOT NULL PRIMARY KEY,
    locked BOOLEAN NOT NULL,
    lockgranted TIMESTAMP,
    lockedby VARCHAR(255)
);

-- Inserir registros na tabela databasechangeloglock
INSERT INTO databasechangeloglock (id, locked) VALUES (1, FALSE) ON CONFLICT DO NOTHING;

-- Inserir registros na tabela databasechangelog para os changesets que já foram executados
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, liquibase)
VALUES 
('0001-create-cliente-table', 'golden', 'changes/0001-create-cliente-table.yaml', CURRENT_TIMESTAMP, 1, 'EXECUTED', '7:abcdef1234567890', 'createTable', '', '4.20.0'),
('0002-create-produto-table', 'golden', 'changes/0002-create-produto-table.yaml', CURRENT_TIMESTAMP, 2, 'EXECUTED', '7:1234567890abcdef', 'createTable', '', '4.20.0'),
('0003-create-pedido-tables', 'golden', 'changes/0003-create-pedido-tables.yaml', CURRENT_TIMESTAMP, 3, 'EXECUTED', '7:0987654321fedcba', 'createTable', '', '4.20.0')
ON CONFLICT DO NOTHING;
