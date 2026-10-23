CREATE TABLE produto (
    id                  BIGSERIAL    PRIMARY KEY,
    nome                VARCHAR(120) NOT NULL,
    estoque_disponivel  INTEGER      NOT NULL CHECK (estoque_disponivel >= 0),
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE reserva (
    id          BIGSERIAL    PRIMARY KEY,
    produto_id  BIGINT       NOT NULL REFERENCES produto (id),
    quantidade  INTEGER      NOT NULL CHECK (quantidade > 0),
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'CANCELADA', 'EXPIRADA')),
    expires_at  TIMESTAMPTZ  NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_reserva_status_expires_at ON reserva (status, expires_at);
