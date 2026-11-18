-- Base de dados RUE (Residência Universitária) — MariaDB
-- Corre:  mariadb -u root -p < rue.sql

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS rue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rue;

CREATE TABLE IF NOT EXISTS Categoria (
    idCategoria   INT AUTO_INCREMENT PRIMARY KEY,
    nomeCategoria VARCHAR(50)    NOT NULL,
    capacidade    INT            NOT NULL CHECK (capacidade > 0),
    preco         DECIMAL(10,2)  NOT NULL CHECK (preco >= 0)          -- preço mensal por cama
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Residencia (
    idResidencia INT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    tipo         VARCHAR(30)  NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Quarto (
    idQuarto     INT AUTO_INCREMENT PRIMARY KEY,
    numero       INT NOT NULL CHECK (numero > 0),
    idResidencia INT NOT NULL,
    idCategoria  INT NOT NULL,
    CONSTRAINT uq_quarto_numero UNIQUE (idResidencia, numero),         -- único só dentro da residência
    CONSTRAINT fk_quarto_residencia FOREIGN KEY (idResidencia) REFERENCES Residencia (idResidencia),
    CONSTRAINT fk_quarto_categoria  FOREIGN KEY (idCategoria)  REFERENCES Categoria (idCategoria)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Cama (
    idCama   INT AUTO_INCREMENT PRIMARY KEY,
    estado   ENUM('DISPONIVEL','RESERVADA','OCUPADA','MANUTENCAO') NOT NULL DEFAULT 'DISPONIVEL',
    idQuarto INT NOT NULL,
    CONSTRAINT fk_cama_quarto FOREIGN KEY (idQuarto) REFERENCES Quarto (idQuarto)
) ENGINE=InnoDB;

-- Estudante e Administrador são tabelas independentes, cada uma com o seu id
CREATE TABLE IF NOT EXISTS Estudante (
    idEstudante INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    telefone    VARCHAR(20),
    email       VARCHAR(100),
    sexo        ENUM('MASCULINO','FEMININO') NOT NULL,
    curso       VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Administrador (
    idAdministrador INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    telefone        VARCHAR(20),
    email           VARCHAR(100),
    dataInicio      DATETIME     NOT NULL,
    grau            VARCHAR(50)  NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Gestao (
    idResidencia    INT      NOT NULL,
    idAdministrador INT      NOT NULL,
    dataInicio      DATETIME NOT NULL,
    dataFim         DATETIME NULL,
    estado          ENUM('ATIVA','ENCERRADA') NOT NULL DEFAULT 'ATIVA',
    PRIMARY KEY (idResidencia, idAdministrador, dataInicio),
    CONSTRAINT fk_gestao_residencia    FOREIGN KEY (idResidencia)    REFERENCES Residencia (idResidencia),
    CONSTRAINT fk_gestao_administrador FOREIGN KEY (idAdministrador) REFERENCES Administrador (idAdministrador)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Requisicao (
    idRequisicao    INT AUTO_INCREMENT PRIMARY KEY,
    data            DATETIME NOT NULL,
    resultado       ENUM('PENDENTE','APROVADO','NAO_APROVADO') NOT NULL DEFAULT 'PENDENTE',
    idEstudante     INT NOT NULL,
    idResidencia    INT NOT NULL,
    idAdministrador INT NULL,                                          -- preenchido quando é decidida
    CONSTRAINT fk_req_estudante      FOREIGN KEY (idEstudante)     REFERENCES Estudante (idEstudante),
    CONSTRAINT fk_req_residencia     FOREIGN KEY (idResidencia)    REFERENCES Residencia (idResidencia),
    CONSTRAINT fk_req_administrador  FOREIGN KEY (idAdministrador) REFERENCES Administrador (idAdministrador)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Contracto (
    idContracto    INT AUTO_INCREMENT PRIMARY KEY,
    dataAssinatura DATETIME NOT NULL,
    dataInicial    DATETIME NOT NULL,
    dataFinal      DATETIME NOT NULL,
    estado         ENUM('PENDENTE','ATIVO','CANCELADO') NOT NULL DEFAULT 'PENDENTE',
    idRequisicao   INT NOT NULL,
    idCama         INT NOT NULL,
    idEstudante    INT NOT NULL,                                       -- ligação "assina"
    idAdministrador INT NULL,                                          -- ligação "gere" (responsável que o gerou)
    CONSTRAINT uq_contracto_requisicao UNIQUE (idRequisicao),          -- Requisicao 1—0..1 Contracto
    CONSTRAINT fk_con_requisicao FOREIGN KEY (idRequisicao) REFERENCES Requisicao (idRequisicao),
    CONSTRAINT fk_con_cama       FOREIGN KEY (idCama)       REFERENCES Cama (idCama),
    CONSTRAINT fk_con_estudante  FOREIGN KEY (idEstudante)  REFERENCES Estudante (idEstudante),
    CONSTRAINT fk_con_administrador FOREIGN KEY (idAdministrador) REFERENCES Administrador (idAdministrador)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Pagamento (
    idPagamento INT AUTO_INCREMENT PRIMARY KEY,
    data        DATETIME      NOT NULL,
    metodo      ENUM('NUMERARIO','TRANSFERENCIA','CARTAO','MOBILE_MONEY') NOT NULL,
    totalMeses  INT           NOT NULL CHECK (totalMeses > 0),
    valorTotal  DECIMAL(12,2) NOT NULL CHECK (valorTotal >= 0),        -- totalMeses × Categoria.preco
    idContracto INT NOT NULL,
    idEstudante INT NOT NULL,                                          -- ligação "efectua"
    CONSTRAINT uq_pag_contracto UNIQUE (idContracto),                  -- Contracto 1—0..1 Pagamento
    CONSTRAINT fk_pag_contracto FOREIGN KEY (idContracto) REFERENCES Contracto (idContracto),
    CONSTRAINT fk_pag_estudante FOREIGN KEY (idEstudante) REFERENCES Estudante (idEstudante)
) ENGINE=InnoDB;
