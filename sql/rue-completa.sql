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
-- Dados de exemplo da base de dados RUE.
-- Corre DEPOIS de rue.sql, com a base de dados vazia:  mariadb -u root -p < sql/rue-dados-exemplo.sql
SET NAMES utf8mb4;
USE rue;
INSERT INTO `Categoria` (`idCategoria`, `nomeCategoria`, `capacidade`, `preco`) VALUES (1,'Normal',2,2500.00);
INSERT INTO `Categoria` (`idCategoria`, `nomeCategoria`, `capacidade`, `preco`) VALUES (2,'Suite',1,4500.00);
INSERT INTO `Residencia` (`idResidencia`, `nome`, `tipo`) VALUES (1,'Residência Central','Mista');
INSERT INTO `Residencia` (`idResidencia`, `nome`, `tipo`) VALUES (2,'Residência Norte','Feminina');
INSERT INTO `Quarto` (`idQuarto`, `numero`, `idResidencia`, `idCategoria`) VALUES (1,101,1,1);
INSERT INTO `Quarto` (`idQuarto`, `numero`, `idResidencia`, `idCategoria`) VALUES (2,102,1,1);
INSERT INTO `Quarto` (`idQuarto`, `numero`, `idResidencia`, `idCategoria`) VALUES (3,103,1,1);
INSERT INTO `Quarto` (`idQuarto`, `numero`, `idResidencia`, `idCategoria`) VALUES (4,201,1,2);
INSERT INTO `Quarto` (`idQuarto`, `numero`, `idResidencia`, `idCategoria`) VALUES (5,101,2,1);
INSERT INTO `Quarto` (`idQuarto`, `numero`, `idResidencia`, `idCategoria`) VALUES (6,102,2,1);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (1,'DISPONIVEL',1);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (2,'DISPONIVEL',1);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (3,'DISPONIVEL',2);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (4,'DISPONIVEL',2);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (5,'DISPONIVEL',3);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (6,'DISPONIVEL',3);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (7,'DISPONIVEL',4);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (8,'DISPONIVEL',5);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (9,'DISPONIVEL',5);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (10,'DISPONIVEL',6);
INSERT INTO `Cama` (`idCama`, `estado`, `idQuarto`) VALUES (11,'DISPONIVEL',6);
INSERT INTO `Estudante` (`idEstudante`, `nome`, `telefone`, `email`, `sexo`, `curso`) VALUES (1,'Carlos Mondlane','845550101','carlos@estudante.uni.mz','MASCULINO','Engenharia Informática');
INSERT INTO `Estudante` (`idEstudante`, `nome`, `telefone`, `email`, `sexo`, `curso`) VALUES (2,'Maria Chissano','825550202','maria@estudante.uni.mz','FEMININO','Medicina');
INSERT INTO `Administrador` (`idAdministrador`, `nome`, `telefone`, `email`, `dataInicio`, `grau`) VALUES (1,'Ana Macamo','841234567','ana.macamo@uni.mz','2024-11-18 04:37:19','Sénior');
INSERT INTO `Administrador` (`idAdministrador`, `nome`, `telefone`, `email`, `dataInicio`, `grau`) VALUES (2,'Paulo Nhaca','829876543','paulo.nhaca@uni.mz','2026-03-18 04:37:19','Júnior');
INSERT INTO `Administrador` (`idAdministrador`, `nome`, `telefone`, `email`, `dataInicio`, `grau`) VALUES (3,'Rita Sitoe','834445566','rita.sitoe@uni.mz','2026-10-18 04:37:19','Júnior');
INSERT INTO `Gestao` (`idResidencia`, `idAdministrador`, `dataInicio`, `dataFim`, `estado`) VALUES (1,1,'2026-05-18 04:37:19',NULL,'ATIVA');
INSERT INTO `Gestao` (`idResidencia`, `idAdministrador`, `dataInicio`, `dataFim`, `estado`) VALUES (2,2,'2026-08-18 04:37:19',NULL,'ATIVA');
