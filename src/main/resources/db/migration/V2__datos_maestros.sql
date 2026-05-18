-- V2__datos_maestros.sql
-- Catálogos iniciales del sistema

INSERT INTO TM_TIPO_MIEMBRO (codigo, descripcion, es_mascota) VALUES
  ('PERSONA',  'Persona',           FALSE),
  ('PERRO',    'Perro',             TRUE),
  ('GATO',     'Gato',              TRUE),
  ('TORTUGA',  'Tortuga de tierra', TRUE),
  ('AVE',      'Ave',               TRUE),
  ('OTRO',     'Otro',              FALSE);

INSERT INTO TM_ESTADO_GENERAL (codigo, descripcion) VALUES
  ('ACTIVO',    'Activo'),
  ('INACTIVO',  'Inactivo'),
  ('ELIMINADO', 'Eliminado');

INSERT INTO TM_PRIORIDAD (codigo, descripcion, orden) VALUES
  ('BAJA',    'Baja',    1),
  ('MEDIA',   'Media',   2),
  ('ALTA',    'Alta',    3),
  ('URGENTE', 'Urgente', 4);

INSERT INTO TM_TIPO_RECORDATORIO (codigo, descripcion) VALUES
  ('PUNTUAL',  'Una sola vez'),
  ('DIARIO',   'Diario'),
  ('SEMANAL',  'Semanal'),
  ('MENSUAL',  'Mensual'),
  ('ANUAL',    'Anual'),
  ('CUSTOM',   'Personalizado');

INSERT INTO TM_ROL_HOGAR (codigo, descripcion) VALUES
  ('ADMIN',        'Administrador'),
  ('COLABORADOR',  'Colaborador'),
  ('SOLO_LECTURA', 'Solo lectura');

INSERT INTO TM_TIPO_EVENTO (codigo, descripcion) VALUES
  ('CUMPLEANOS',  'Cumpleaños'),
  ('VACUNA',      'Vacuna'),
  ('VETERINARIO', 'Visita veterinario'),
  ('CITA_MEDICA', 'Cita médica'),
  ('ANIVERSARIO', 'Aniversario'),
  ('OTRO',        'Otro');

INSERT INTO TB_HERRAMIENTA (codigo, nombre, icono, ruta, es_core, orden) VALUES
  ('DASHBOARD',     'Inicio',        'home',          '/dashboard',     TRUE,  0),
  ('TAREAS',        'Tareas',        'checkmark',     '/tareas',        TRUE,  1),
  ('RECORDATORIOS', 'Recordatorios', 'notifications', '/recordatorios', TRUE,  2),
  ('CALENDARIO',    'Calendario',    'calendar',      '/calendario',    TRUE,  3),
  ('MASCOTAS',      'Mascotas',      'paw',           '/mascotas',      TRUE,  4),
  ('FAMILIA',       'Familia',       'people',        '/familia',       TRUE,  5),
  ('LISTAS',        'Listas',        'list',          '/listas',        FALSE, 6),
  ('CALCULADORAS',  'Calculadoras',  'calculator',    '/calculadoras',  FALSE, 7);
