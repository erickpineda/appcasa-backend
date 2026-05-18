-- V1__init_schema.sql
-- Flyway ejecuta este script en el primer arranque.
-- Contiene únicamente el DDL (sin datos de ejemplo).
-- Los datos maestros van en V2__datos_maestros.sql

-- Extensiones
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE TM_TIPO_MIEMBRO (
  id          SERIAL        PRIMARY KEY,
  codigo      VARCHAR(30)   NOT NULL UNIQUE,
  descripcion VARCHAR(100)  NOT NULL,
  es_mascota  BOOLEAN       NOT NULL DEFAULT FALSE,
  activo      BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE TM_ESTADO_GENERAL (
  id          SERIAL        PRIMARY KEY,
  codigo      VARCHAR(30)   NOT NULL UNIQUE,
  descripcion VARCHAR(100)  NOT NULL
);

CREATE TABLE TM_PRIORIDAD (
  id          SERIAL       PRIMARY KEY,
  codigo      VARCHAR(20)  NOT NULL UNIQUE,
  descripcion VARCHAR(50)  NOT NULL,
  orden       INTEGER      NOT NULL
);

CREATE TABLE TM_TIPO_RECORDATORIO (
  id          SERIAL       PRIMARY KEY,
  codigo      VARCHAR(30)  NOT NULL UNIQUE,
  descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE TM_ROL_HOGAR (
  id          SERIAL       PRIMARY KEY,
  codigo      VARCHAR(30)  NOT NULL UNIQUE,
  descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE TM_TIPO_EVENTO (
  id          SERIAL       PRIMARY KEY,
  codigo      VARCHAR(30)  NOT NULL UNIQUE,
  descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE TB_HOGAR (
  id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  nombre      VARCHAR(100)  NOT NULL,
  descripcion TEXT,
  codigo      VARCHAR(10)   NOT NULL UNIQUE,
  id_estado   INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by  UUID,
  updated_by  UUID,
  version     INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_USUARIO (
  id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  nombre          VARCHAR(100)  NOT NULL,
  apellidos       VARCHAR(150),
  email           VARCHAR(200)  NOT NULL UNIQUE,
  password_hash   VARCHAR(255)  NOT NULL,
  avatar_url      VARCHAR(500),
  tema            VARCHAR(10)   NOT NULL DEFAULT 'CLARO',
  locale          VARCHAR(10)   NOT NULL DEFAULT 'es-ES',
  id_estado       INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  ultimo_acceso   TIMESTAMPTZ,
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  version         INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_HOGAR_USUARIO (
  id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar     UUID         NOT NULL REFERENCES TB_HOGAR(id),
  id_usuario   UUID         NOT NULL REFERENCES TB_USUARIO(id),
  id_rol       INTEGER      NOT NULL REFERENCES TM_ROL_HOGAR(id),
  es_principal BOOLEAN      NOT NULL DEFAULT FALSE,
  id_estado    INTEGER      NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (id_hogar, id_usuario)
);

CREATE TABLE TB_MIEMBRO_HOGAR (
  id                UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar          UUID          NOT NULL REFERENCES TB_HOGAR(id),
  id_tipo_miembro   INTEGER       NOT NULL REFERENCES TM_TIPO_MIEMBRO(id),
  nombre            VARCHAR(100)  NOT NULL,
  fecha_nacimiento  DATE,
  avatar_url        VARCHAR(500),
  notas             TEXT,
  raza              VARCHAR(100),
  color             VARCHAR(100),
  peso_kg           NUMERIC(5,2),
  microchip         VARCHAR(50),
  chip_num          VARCHAR(50),
  esterilizado      BOOLEAN,
  id_usuario        UUID          REFERENCES TB_USUARIO(id),
  id_estado         INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by        UUID          REFERENCES TB_USUARIO(id),
  updated_by        UUID          REFERENCES TB_USUARIO(id),
  version           INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_TAREA (
  id                UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar          UUID          NOT NULL REFERENCES TB_HOGAR(id),
  titulo            VARCHAR(200)  NOT NULL,
  descripcion       TEXT,
  id_prioridad      INTEGER       NOT NULL REFERENCES TM_PRIORIDAD(id) DEFAULT 1,
  categoria         VARCHAR(100),
  fecha_limite      DATE,
  fecha_completada  TIMESTAMPTZ,
  es_periodica      BOOLEAN       NOT NULL DEFAULT FALSE,
  periodicidad      VARCHAR(30),
  regla_recurrencia TEXT,
  es_personal       BOOLEAN       NOT NULL DEFAULT FALSE,
  id_creador        UUID          REFERENCES TB_USUARIO(id),
  adjunto_url       VARCHAR(500),
  id_estado         INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by        UUID          REFERENCES TB_USUARIO(id),
  updated_by        UUID          REFERENCES TB_USUARIO(id),
  version           INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_TAREA_ASIGNACION (
  id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_tarea    UUID         NOT NULL REFERENCES TB_TAREA(id),
  id_miembro  UUID         NOT NULL REFERENCES TB_MIEMBRO_HOGAR(id),
  aceptada    BOOLEAN,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (id_tarea, id_miembro)
);

CREATE TABLE TB_EVENTO (
  id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar        UUID          NOT NULL REFERENCES TB_HOGAR(id),
  id_tipo_evento  INTEGER       NOT NULL REFERENCES TM_TIPO_EVENTO(id),
  titulo          VARCHAR(200)  NOT NULL,
  descripcion     TEXT,
  fecha_inicio    TIMESTAMPTZ   NOT NULL,
  fecha_fin       TIMESTAMPTZ,
  todo_el_dia     BOOLEAN       NOT NULL DEFAULT FALSE,
  es_anual        BOOLEAN       NOT NULL DEFAULT FALSE,
  id_miembro      UUID          REFERENCES TB_MIEMBRO_HOGAR(id),
  id_estado       INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by      UUID          REFERENCES TB_USUARIO(id),
  updated_by      UUID          REFERENCES TB_USUARIO(id),
  version         INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_RECORDATORIO (
  id                    UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar              UUID          NOT NULL REFERENCES TB_HOGAR(id),
  titulo                VARCHAR(200)  NOT NULL,
  descripcion           TEXT,
  id_tipo_recordatorio  INTEGER       NOT NULL REFERENCES TM_TIPO_RECORDATORIO(id),
  fecha_hora            TIMESTAMPTZ   NOT NULL,
  regla_recurrencia     TEXT,
  anticipacion_minutos  INTEGER       NOT NULL DEFAULT 30,
  activo                BOOLEAN       NOT NULL DEFAULT TRUE,
  id_tarea              UUID          REFERENCES TB_TAREA(id),
  id_miembro            UUID          REFERENCES TB_MIEMBRO_HOGAR(id),
  id_evento             UUID          REFERENCES TB_EVENTO(id),
  id_estado             INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by            UUID          REFERENCES TB_USUARIO(id),
  updated_by            UUID          REFERENCES TB_USUARIO(id),
  version               INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_LISTA (
  id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar    UUID          NOT NULL REFERENCES TB_HOGAR(id),
  nombre      VARCHAR(100)  NOT NULL,
  tipo        VARCHAR(50)   NOT NULL DEFAULT 'COMPRA',
  icono       VARCHAR(50),
  color       VARCHAR(7),
  id_estado   INTEGER       NOT NULL REFERENCES TM_ESTADO_GENERAL(id) DEFAULT 1,
  created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_by  UUID          REFERENCES TB_USUARIO(id),
  version     INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE TB_LISTA_ITEM (
  id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_lista    UUID          NOT NULL REFERENCES TB_LISTA(id),
  descripcion VARCHAR(300)  NOT NULL,
  cantidad    NUMERIC(8,2),
  unidad      VARCHAR(30),
  completado  BOOLEAN       NOT NULL DEFAULT FALSE,
  orden       INTEGER       NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE TB_HERRAMIENTA (
  id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  codigo      VARCHAR(50)   NOT NULL UNIQUE,
  nombre      VARCHAR(100)  NOT NULL,
  descripcion TEXT,
  icono       VARCHAR(50),
  ruta        VARCHAR(200),
  version     VARCHAR(20)   NOT NULL DEFAULT '1.0.0',
  activa      BOOLEAN       NOT NULL DEFAULT TRUE,
  es_core     BOOLEAN       NOT NULL DEFAULT FALSE,
  orden       INTEGER       NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE TB_HOGAR_HERRAMIENTA (
  id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar        UUID         NOT NULL REFERENCES TB_HOGAR(id),
  id_herramienta  UUID         NOT NULL REFERENCES TB_HERRAMIENTA(id),
  activa          BOOLEAN      NOT NULL DEFAULT TRUE,
  UNIQUE (id_hogar, id_herramienta)
);

CREATE TABLE TB_CONFIGURACION (
  id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_hogar    UUID          REFERENCES TB_HOGAR(id),
  id_usuario  UUID          REFERENCES TB_USUARIO(id),
  clave       VARCHAR(100)  NOT NULL,
  valor       TEXT          NOT NULL,
  updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  UNIQUE (id_hogar, id_usuario, clave)
);

-- Índices
CREATE INDEX idx_miembro_hogar      ON TB_MIEMBRO_HOGAR(id_hogar);
CREATE INDEX idx_tarea_hogar        ON TB_TAREA(id_hogar);
CREATE INDEX idx_tarea_estado       ON TB_TAREA(id_estado);
CREATE INDEX idx_recordatorio_fecha ON TB_RECORDATORIO(fecha_hora);
CREATE INDEX idx_recordatorio_hogar ON TB_RECORDATORIO(id_hogar);
CREATE INDEX idx_evento_hogar       ON TB_EVENTO(id_hogar);
CREATE INDEX idx_evento_fecha       ON TB_EVENTO(fecha_inicio);
CREATE INDEX idx_lista_hogar        ON TB_LISTA(id_hogar);
CREATE INDEX idx_lista_item         ON TB_LISTA_ITEM(id_lista);
CREATE INDEX idx_config_hogar       ON TB_CONFIGURACION(id_hogar);
