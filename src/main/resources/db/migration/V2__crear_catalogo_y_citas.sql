-- Catálogo de servicios, nutricionistas y solicitudes de cita.
-- Los datos iniciales son los mismos que usaba el frontend (servicios.json y nutricionistas.json).

CREATE TABLE categoria (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE servicio (
    id           SERIAL PRIMARY KEY,
    codigo       VARCHAR(10)  NOT NULL UNIQUE,
    nombre       VARCHAR(120) NOT NULL,
    tipo         VARCHAR(40),
    categoria_id INT          NOT NULL REFERENCES categoria (id) ON DELETE RESTRICT,
    duracion     VARCHAR(30),
    modalidad    VARCHAR(40),
    precio       INT          NOT NULL CHECK (precio > 0),
    descripcion  TEXT,
    activo       BOOLEAN      NOT NULL DEFAULT TRUE   -- borrado lógico: las citas antiguas lo siguen referenciando
);

CREATE TABLE nutricionista (
    id           SERIAL PRIMARY KEY,
    codigo       VARCHAR(10)  NOT NULL UNIQUE,
    nombre       VARCHAR(100) NOT NULL,
    especialidad VARCHAR(150),
    dias         VARCHAR(60),
    horario      VARCHAR(30)                            -- ej: "09:00 – 17:00"
);

CREATE TABLE cita (
    id               SERIAL PRIMARY KEY,
    paciente_id      INT          NOT NULL REFERENCES usuario (id) ON DELETE RESTRICT,
    servicio_id      INT          NOT NULL REFERENCES servicio (id) ON DELETE RESTRICT,
    nutricionista_id INT          NOT NULL REFERENCES nutricionista (id) ON DELETE RESTRICT,
    nombre           VARCHAR(100) NOT NULL,
    rut              VARCHAR(12)  NOT NULL,
    email            VARCHAR(120) NOT NULL,
    telefono         VARCHAR(20),
    fecha            DATE         NOT NULL,
    hora             TIME         NOT NULL,
    motivo           VARCHAR(500),
    estado           VARCHAR(20)  NOT NULL DEFAULT 'Pendiente',
    creado_en        TIMESTAMP    NOT NULL DEFAULT now()
);

-- Un mismo nutricionista no puede tener dos citas activas en la misma fecha y hora.
-- (El backend lo revisa antes y responde 409; este índice es la última barrera.)
CREATE UNIQUE INDEX cita_horario_ocupado
    ON cita (nutricionista_id, fecha, hora)
    WHERE estado NOT IN ('Cancelada', 'Rechazada');

CREATE INDEX cita_paciente ON cita (paciente_id);

INSERT INTO categoria (nombre) VALUES
    ('Consultas'),
    ('Planes Especializados'),
    ('Evaluaciones'),
    ('Talleres');

INSERT INTO servicio (codigo, nombre, tipo, categoria_id, duracion, modalidad, precio, descripcion) VALUES
    ('CN001', 'Primera consulta nutricional', 'Consulta', (SELECT id FROM categoria WHERE nombre = 'Consultas'), '50 min', 'Presencial', 35000, 'Evaluación inicial: anamnesis, antropometría completa y diseño del primer plan alimenticio.'),
    ('CN002', 'Control nutricional (seguimiento)', 'Consulta', (SELECT id FROM categoria WHERE nombre = 'Consultas'), '30 min', 'Presencial', 25000, 'Seguimiento mensual: medición de indicadores y ajuste del plan vigente.'),
    ('CN003', 'Control nutricional quincenal', 'Consulta', (SELECT id FROM categoria WHERE nombre = 'Consultas'), '30 min', 'Presencial', 22000, 'Seguimiento intensivo cada 15 días. Recomendado en los primeros 2 meses.'),
    ('CN004', 'Teleconsulta nutricional', 'Consulta', (SELECT id FROM categoria WHERE nombre = 'Consultas'), '30 min', 'Online (video)', 20000, 'Consulta de seguimiento vía videollamada. Requiere contar con consulta presencial previa.'),
    ('CN005', 'Consulta de urgencia / reagendada', 'Consulta', (SELECT id FROM categoria WHERE nombre = 'Consultas'), '30 min', 'Presencial', 28000, 'Para pacientes que requieren atención fuera de su control habitual.'),
    ('PL001', 'Plan pérdida de peso (1 mes)', 'Plan especializado', (SELECT id FROM categoria WHERE nombre = 'Planes Especializados'), '—', 'Presencial', 65000, 'Incluye primera consulta + 1 control quincenal + plan alimenticio personalizado + seguimiento por WhatsApp.'),
    ('PL002', 'Plan pérdida de peso (3 meses)', 'Plan especializado', (SELECT id FROM categoria WHERE nombre = 'Planes Especializados'), '—', 'Presencial', 170000, 'Incluye primera consulta + 5 controles + 3 planes mensuales + seguimiento continuo.'),
    ('PL003', 'Plan nutrición deportiva (1 mes)', 'Plan especializado', (SELECT id FROM categoria WHERE nombre = 'Planes Especializados'), '—', 'Presencial', 70000, 'Para deportistas y personas con actividad física frecuente. Cálculo de requerimientos energéticos y proteicos.'),
    ('PL004', 'Plan control diabetes / hipertensión', 'Plan especializado', (SELECT id FROM categoria WHERE nombre = 'Planes Especializados'), '—', 'Presencial', 75000, 'Plan adaptado para patologías metabólicas. Coordinación con médico tratante si aplica.'),
    ('PL005', 'Plan alimentación vegetariana/vegana', 'Plan especializado', (SELECT id FROM categoria WHERE nombre = 'Planes Especializados'), '—', 'Presencial', 68000, 'Diseñado para garantizar aporte adecuado de proteínas, hierro, vitamina B12 y calcio sin productos animales.'),
    ('PL006', 'Plan alimentación infantil (2-12 años)', 'Plan especializado', (SELECT id FROM categoria WHERE nombre = 'Planes Especializados'), '—', 'Presencial', 65000, 'Evaluación nutricional pediátrica y diseño de plan adaptado a la etapa de desarrollo del niño.'),
    ('EV001', 'Antropometría completa', 'Evaluación', (SELECT id FROM categoria WHERE nombre = 'Evaluaciones'), '20 min', 'Presencial', 18000, 'Peso, talla, IMC, circunferencia de cintura, cadera, brazo y % de grasa corporal con bioimpedanciometría.'),
    ('EV002', 'Bioimpedanciometría', 'Evaluación', (SELECT id FROM categoria WHERE nombre = 'Evaluaciones'), '15 min', 'Presencial', 12000, 'Medición de composición corporal: masa grasa, masa muscular, agua corporal y edad metabólica.'),
    ('EV003', 'Encuesta de hábitos alimentarios', 'Evaluación', (SELECT id FROM categoria WHERE nombre = 'Evaluaciones'), '20 min', 'Presencial', 10000, 'Análisis del patrón alimentario actual. Identificación de déficit y excesos nutricionales.'),
    ('EV004', 'Análisis de exámenes de laboratorio', 'Evaluación', (SELECT id FROM categoria WHERE nombre = 'Evaluaciones'), '20 min', 'Presencial', 15000, 'Interpretación de hemograma, perfil bioquímico y lipídico en contexto nutricional.'),
    ('TG001', 'Taller de alimentación saludable', 'Taller grupal', (SELECT id FROM categoria WHERE nombre = 'Talleres'), '90 min', 'Presencial (grupo)', 15000, 'Máx. 10 personas. Conceptos básicos de alimentación equilibrada y lectura de etiquetas.'),
    ('TG002', 'Taller de cocina nutritiva', 'Taller grupal', (SELECT id FROM categoria WHERE nombre = 'Talleres'), '120 min', 'Presencial (grupo)', 20000, 'Preparación de recetas saludables. Incluye degustación. Máx. 8 personas.'),
    ('TG003', 'Taller nutrición para deportistas', 'Taller grupal', (SELECT id FROM categoria WHERE nombre = 'Talleres'), '90 min', 'Presencial (grupo)', 18000, 'Hidratación, nutrición pre y post entrenamiento, suplementación básica. Máx. 12 personas.');

INSERT INTO nutricionista (codigo, nombre, especialidad, dias, horario) VALUES
    ('NUT001', 'Nut. Carolina Fuentes M.', 'Obesidad y síndrome metabólico', 'Lunes, Miércoles, Viernes', '09:00 – 17:00'),
    ('NUT002', 'Nut. Rodrigo Sepúlveda A.', 'Nutrición deportiva y rendimiento', 'Martes, Jueves, Sábado', '09:00 – 14:00'),
    ('NUT003', 'Nut. Daniela Morales C.', 'Alimentación vegetariana, vegana y trastornos alimentarios', 'Lunes a Viernes', '08:00 – 13:00'),
    ('NUT004', 'Nut. Felipe Araya R.', 'Nutrición pediátrica y familiar', 'Martes a Viernes', '14:00 – 19:00');
