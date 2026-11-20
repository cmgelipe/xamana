-- Migración: registro de conexión por sitio (puerto, base de datos, gestor,
-- usuario y contraseña propios de cada sitio remoto).
--
-- Es puramente ADITIVA: crea una tabla nueva, no toca `sitio`, `fragmento`
-- ni ninguna otra tabla existente. Se puede ejecutar sobre la base de datos
-- de XAMANA en producción sin perder datos ni tiempo fuera de servicio.
--
-- Uso:
--   mysql -u <usuario_admin> -p xamana < migracion_sitio_conexion.sql

CREATE TABLE IF NOT EXISTS `sitio_conexion` (
  `direccion` varchar(255) NOT NULL,
  `puerto` varchar(255) DEFAULT NULL,
  `basedatos` varchar(255) DEFAULT NULL,
  `gestor` varchar(2) DEFAULT NULL,
  `usuario` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`direccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- A partir de aquí, opcional: precargar el registro con los sitios que ya
-- existen hoy en `sitio`, para no tener que escribirlos a mano uno por uno
-- desde la pantalla "Site registry". Quedan con NULL en puerto/usuario/
-- contraseña/base de datos/gestor; edítalos desde esa pantalla con los
-- valores reales de cada sitio antes de volver a fragmentar hacia ellos.
--
-- INSERT IGNORE INTO `sitio_conexion` (`direccion`)
-- SELECT DISTINCT `direccion` FROM `sitio`;

-- Importante: el sitio LOCAL de XAMANA (el que está configurado hoy en la
-- pantalla de conexión, tabla `bd`) también debe tener su propia fila en
-- `sitio_conexion` -- los fragmentadores ahora la necesitan como origen.
-- Ejemplo (ajusta los valores a los de tu tabla `bd`):
--
-- INSERT INTO `sitio_conexion` (`direccion`, `puerto`, `basedatos`, `gestor`, `usuario`, `password`)
-- SELECT `direccion`, `puerto`, `nombre`, `gestor`, `usr`, `psw`
-- FROM `bd`
-- ON DUPLICATE KEY UPDATE
--   `puerto` = VALUES(`puerto`),
--   `basedatos` = VALUES(`basedatos`),
--   `gestor` = VALUES(`gestor`),
--   `usuario` = VALUES(`usuario`),
--   `password` = VALUES(`password`);
