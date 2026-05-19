CREATE TABLE TB_REFRESH_TOKEN_SESSION (
  id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_usuario   UUID         NOT NULL REFERENCES TB_USUARIO(id),
  token_hash   VARCHAR(64)  NOT NULL UNIQUE,
  expira_en    TIMESTAMPTZ  NOT NULL,
  ultimo_uso_en TIMESTAMPTZ,
  revocado_en  TIMESTAMPTZ,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  created_by   UUID,
  updated_by   UUID,
  version      INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_refresh_token_usuario ON TB_REFRESH_TOKEN_SESSION(id_usuario);
CREATE INDEX idx_refresh_token_expira ON TB_REFRESH_TOKEN_SESSION(expira_en);
