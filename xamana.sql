-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 07-03-2025 a las 19:24:16
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `xamana`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `atributo`
--

CREATE TABLE `atributo` (
  `id_atributo` bigint(10) NOT NULL,
  `tipo` varchar(255) NOT NULL,
  `size` int(10) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `id_fragmento` bigint(10) DEFAULT NULL,
  `descriptor` tinyint(1) DEFAULT NULL,
  `multimedia` tinyint(1) DEFAULT NULL,
  `id_tabla` bigint(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `atributo_crud`
--

CREATE TABLE `atributo_crud` (
  `id_atributo` bigint(10) NOT NULL,
  `id_CRUD` bigint(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `bd`
--

CREATE TABLE `bd` (
  `id_BD` bigint(10) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `puerto` varchar(255) NOT NULL,
  `gestor` varchar(255) NOT NULL,
  `usr` varchar(255) DEFAULT NULL,
  `psw` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `bd`
--

INSERT INTO `bd` (`id_BD`, `nombre`, `direccion`, `puerto`, `gestor`, `usr`, `psw`) VALUES
(4, 'prueba', '127.0.0.1:3306', '', 'a', 'root', NULL),
(5, 'Prueba', '127.0.0.1', '3306', 'a', 'root', NULL),
(6, 'Prueba', '127.0.0.1', '3306', 'a', 'root', NULL),
(7, 'Prueba', '127.0.0.1', '3306', 'a', 'root', NULL),
(8, 'Prueba', '127.0.0.1', '3306', 'a', 'root', NULL),
(9, 'prueba', '127.0.0.1', '3306', 'a', 'root', NULL),
(10, 'prueba', '127.0.0.1', '3306', 'a', 'root', NULL),
(11, 'Ejemplo2', '127.0.0.1', '5432', 'b', 'postgres', 'sunny_4teamo'),
(12, 'Ejemplo2', '127.0.0.1', '5432', 'c', 'postgres', 'sunny_4teamo'),
(13, 'Ejemplo2', '127.0.0.1', '5432', 'c', 'postgres', 'sunny_4teamo'),
(14, 'Ejemplo2', '127.0.0.1', '5432', 'c', 'postgres', 'sunny_4teamo'),
(15, 'Ejemplo2', '127.0.0.1', '5432', 'c', 'postgres', 'sunny_4teamo'),
(16, 'Ejemplo2', '127.0.0.1', '5432', 'b', 'postgres', 'sunny_4teamo'),
(17, 'Ejemplo2', '127.0.0.1', '5432', 'b', 'postgres', 'sunny_4teamo'),
(18, 'Ejemplo2', '127.0.0.1', '5432', 'b', 'postgres', 'sunny_4teamo'),
(19, 'Ejemplo2', '127.0.0.1', '5432', 'c', 'postgres', 'sunny_4teamo'),
(20, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(21, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(22, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(23, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(24, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(25, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(26, 'prueba', '192.168.200.5', '3306', 'a', 'root', NULL),
(27, 'Ejemplo2', '192.168.200.5', '5432', 'c', 'postgres', 'sunny_4teamo'),
(28, 'prueba', '192.168.200.5', '3306', 'a', 'root', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crud`
--

CREATE TABLE `crud` (
  `id_CRUD` bigint(10) NOT NULL,
  `costo_operacion` smallint(2) NOT NULL,
  `tipo` smallint(2) NOT NULL,
  `b_basado_en_contenido` smallint(2) NOT NULL,
  `id_sitio` bigint(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `fragmento`
--

CREATE TABLE `fragmento` (
  `id_fragmento` bigint(10) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `id_tabla` bigint(10) NOT NULL,
  `id_sitio` bigint(10) NOT NULL,
  `noperaciones` decimal(10,2) DEFAULT NULL,
  `ndesempenio` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `fragmento`
--

INSERT INTO `fragmento` (`id_fragmento`, `nombre`, `id_tabla`, `id_sitio`, `noperaciones`, `ndesempenio`) VALUES
(31, 'tabla1_f1', 3, 9, 850.00, NULL),
(32, 'tabla1_f2', 3, 9, 200.00, NULL),
(33, 'tabla1_f3', 3, 9, 200.00, NULL),
(39, 'tabla1_f2_1', 3, 9, 100.00, NULL),
(40, 'tabla1_f2_2', 3, 9, 100.00, NULL),
(41, 'tabla1_f3_1', 3, 9, 100.00, NULL),
(42, 'tabla1_f1', 7, 11, 0.00, NULL),
(43, 'tabla1_f2', 7, 11, 0.00, NULL),
(44, 'tabla1_f3', 7, 11, 0.00, NULL),
(45, 'tabla1_f1', 8, 12, 24.00, NULL),
(46, 'tabla1_f2', 8, 12, 24.00, NULL),
(47, 'tabla1_f3', 8, 12, 12.00, NULL),
(48, 'tabla1_f1', 9, 13, 24.00, NULL),
(49, 'tabla1_f2', 9, 13, 24.00, NULL),
(50, 'tabla1_f3', 9, 13, 12.00, NULL),
(51, 'tabla1_f1', 17, 19, 1.00, NULL),
(52, 'tabla1_f2', 17, 19, 1.00, NULL),
(53, 'tabla1_f3', 17, 19, 1.00, NULL),
(54, 'tabla1_f1', 18, 20, 1.00, NULL),
(55, 'tabla1_f2', 18, 20, 1.00, NULL),
(56, 'tabla1_f3', 18, 20, 1.00, NULL),
(57, 'tabla1_f1', 19, 22, 1.00, NULL),
(58, 'tabla1_f2', 19, 22, 1.00, NULL),
(59, 'tabla1_f3', 19, 22, 0.00, NULL),
(60, 'tabla1_f1', 21, 24, 1.00, 1.00),
(61, 'tabla1_f2', 21, 24, 1.00, 1.00),
(62, 'tabla1_f3', 21, 24, 0.00, 0.00),
(63, 'tabla1_f1', 23, 26, 0.72, 0.88),
(64, 'tabla1_f2', 23, 26, 0.72, 0.88),
(65, 'tabla1_f3', 23, 26, 0.36, 0.44),
(66, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(67, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(68, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(69, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(70, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(71, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(72, 'tabla1_f1_2', 18, 28, 0.50, NULL),
(73, 'tabla1_f1', 24, 30, 0.48, 0.88),
(74, 'tabla1_f2', 24, 30, 0.48, 0.88),
(75, 'tabla1_f3', 24, 30, 0.24, 0.44),
(76, 'tabla1_f1_2', 24, 28, 50.00, 200.00),
(77, 'tabla1_f1_2_1', 24, 28, 100.00, 300.00),
(78, 'tabla1_f1', 25, 31, 0.48, 2.40),
(79, 'tabla1_f2', 25, 31, 0.48, 2.40),
(80, 'tabla1_f3', 25, 31, 0.24, 1.20),
(81, 'tabla1_f1_2', 25, 28, 0.24, NULL),
(82, 'tabla1_f1', 26, 32, 0.48, 0.88),
(83, 'tabla1_f2', 26, 32, 0.48, 0.88),
(84, 'tabla1_f3', 26, 32, 0.24, 0.44),
(85, 'tabla1_f1_2', 26, 28, 5.00, 20.00),
(86, 'tabla1_f1_2_1', 26, 28, 10.00, 35.00),
(87, 'tabla1_f1', 27, 33, 0.48, 2.40),
(88, 'tabla1_f2', 27, 33, 0.48, 2.40),
(89, 'tabla1_f3', 27, 33, 0.24, 1.20),
(90, 'tabla1_f1_2', 27, 28, 0.24, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `fragmento_predicado`
--

CREATE TABLE `fragmento_predicado` (
  `id_fragmento` bigint(10) NOT NULL,
  `id_predicado` bigint(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `predicado`
--

CREATE TABLE `predicado` (
  `id_predicado` bigint(10) NOT NULL,
  `frecuencia` int(10) NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `nTuplas` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `predicado_crud`
--

CREATE TABLE `predicado_crud` (
  `id_predicado` bigint(10) NOT NULL,
  `id_CRUD` bigint(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sitio`
--

CREATE TABLE `sitio` (
  `id_sitio` bigint(10) NOT NULL,
  `direccion` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sitio_conexion`
--
-- Registro de conexión por sitio (dirección/host, puerto, base de datos,
-- gestor, usuario y contraseña). Reemplaza el supuesto de que todos los
-- sitios comparten las credenciales y el puerto del sitio local.
--

CREATE TABLE `sitio_conexion` (
  `direccion` varchar(255) NOT NULL,
  `puerto` varchar(255) DEFAULT NULL,
  `basedatos` varchar(255) DEFAULT NULL,
  `gestor` varchar(2) DEFAULT NULL,
  `usuario` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`direccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `sitio`
--

INSERT INTO `sitio` (`id_sitio`, `direccion`) VALUES
(9, '127.0.0.1:3306'),
(10, '127.0.0.1:3306'),
(11, '127.0.0.1'),
(12, '127.0.0.1'),
(13, '127.0.0.1'),
(14, '127.0.0.1'),
(15, '127.0.0.1'),
(16, '127.0.0.1'),
(17, '127.0.0.1'),
(18, '127.0.0.1'),
(19, '127.0.0.1'),
(20, '192.168.200.5'),
(21, '187.190.29.200'),
(22, '192.168.200.5'),
(23, '192.168.200.5'),
(24, '192.168.200.5'),
(25, '192.168.200.5'),
(26, '192.168.200.5'),
(27, '192.168.200.32'),
(28, '192.168.200.33'),
(29, '192.168.200.33'),
(30, '192.168.200.5'),
(31, '192.168.200.5'),
(32, '192.168.200.5'),
(33, '192.168.200.5');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tabla`
--

CREATE TABLE `tabla` (
  `id_tabla` bigint(10) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `umbral_op` int(10) NOT NULL,
  `umbral_des` int(10) NOT NULL,
  `tipo_frag` varchar(2) NOT NULL,
  `id_BD` bigint(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tabla`
--

INSERT INTO `tabla` (`id_tabla`, `nombre`, `umbral_op`, `umbral_des`, `tipo_frag`, `id_BD`) VALUES
(3, 'tabla1', -99, -98, 'a', 4),
(4, 'tabla1', 23, -43, 'a', 5),
(5, 'tabla1', 32, -31, 'a', 6),
(6, 'tabla1', 32, -31, 'a', 7),
(7, 'tabla1', 41, -76, 'a', 8),
(8, 'tabla1', 42, -66, 'a', 9),
(9, 'tabla1', 33, -64, 'a', 10),
(10, 'tabla1', 13, -20, 'a', 11),
(11, 'tabla1', 21, -37, 'a', 12),
(12, 'tabla1', 21, -37, 'a', 13),
(13, 'tabla1', 21, -37, 'a', 14),
(14, 'tabla1', 18, -27, 'a', 15),
(15, 'tabla1', 18, -56, 'a', 16),
(16, 'tabla1', 23, -36, 'a', 17),
(17, 'tabla1', 17, -29, 'a', 18),
(18, 'tabla1', 20, -28, 'a', 19),
(19, 'tabla1', 20, 24, 'a', 20),
(20, 'tabla1', 20, 24, 'a', 21),
(21, 'tabla1', 20, 24, 'a', 22),
(22, 'tabla1', 20, 24, 'a', 23),
(23, 'tabla1', 20, 24, 'a', 24),
(24, 'tabla1', 20, 24, 'a', 25),
(25, 'tabla1', 20, 24, 'a', 26),
(26, 'tabla1', 20, 24, 'a', 27),
(27, 'tabla1', 20, 24, 'a', 28);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tupla`
--

CREATE TABLE `tupla` (
  `id_local` bigint(10) NOT NULL,
  `id_remoto` varchar(255) NOT NULL,
  `id_fragmento` bigint(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tupla`
--

INSERT INTO `tupla` (`id_local`, `id_remoto`, `id_fragmento`) VALUES
(12, '1.0', 31),
(13, '2.0', 31),
(14, '3.0', 40),
(15, '4.0', 39),
(16, '5.0', 41),
(17, '1.0', 42),
(18, '2.0', 42),
(19, '3.0', 43),
(20, '4.0', 43),
(21, '5.0', 44),
(22, '1.0', 45),
(23, '2.0', 45),
(24, '3.0', 46),
(25, '4.0', 46),
(26, '5.0', 47),
(27, '1.0', 48),
(28, '2.0', 48),
(29, '3.0', 49),
(30, '4.0', 49),
(31, '5.0', 50),
(32, '1', 51),
(33, '2', 51),
(34, '3', 52),
(35, '4', 52),
(36, '5', 53),
(37, '1', 54),
(38, '2', 72),
(39, '3', 55),
(40, '4', 55),
(41, '5', 56),
(42, '1', 57),
(43, '2', 57),
(44, '3', 58),
(45, '4', 58),
(46, '5', 59),
(47, '1', 60),
(48, '2', 60),
(49, '3', 61),
(50, '4', 61),
(51, '5', 62),
(52, '1', 63),
(53, '2', 63),
(54, '3', 64),
(55, '4', 64),
(56, '5', 65),
(57, '1', 73),
(58, '2', 77),
(59, '3', 74),
(60, '4', 74),
(61, '5', 75),
(62, '1.0', 78),
(63, '2.0', 81),
(64, '3.0', 79),
(65, '4.0', 79),
(66, '5.0', 80),
(67, '1', 82),
(68, '2', 86),
(69, '3', 83),
(70, '4', 83),
(71, '5', 84),
(72, '1.0', 87),
(73, '2.0', 90),
(74, '3.0', 88),
(75, '4.0', 88),
(76, '5.0', 89);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tupla_sitio`
--

CREATE TABLE `tupla_sitio` (
  `id_local` bigint(10) NOT NULL,
  `id_sitio` bigint(10) NOT NULL,
  `freq_acceso` int(10) NOT NULL,
  `desempenio` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tupla_sitio`
--

INSERT INTO `tupla_sitio` (`id_local`, `id_sitio`, `freq_acceso`, `desempenio`) VALUES
(12, 9, 2, 0),
(12, 10, 3, 0),
(13, 9, 2, 0),
(13, 10, 2, 0),
(14, 9, 5, 0),
(15, 9, 3, 0),
(16, 9, 10, 0),
(16, 10, 4, 0),
(38, 21, 100, 0),
(38, 28, 532, 2108),
(38, 29, 30, 120),
(53, 27, 102, 0),
(58, 28, 200, 600),
(63, 28, 130, 520),
(68, 28, 20, 70),
(73, 28, 100, 600);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `atributo`
--
ALTER TABLE `atributo`
  ADD PRIMARY KEY (`id_atributo`),
  ADD KEY `FKAtributo35522` (`id_fragmento`),
  ADD KEY `id_tabla` (`id_tabla`);

--
-- Indices de la tabla `atributo_crud`
--
ALTER TABLE `atributo_crud`
  ADD PRIMARY KEY (`id_atributo`,`id_CRUD`),
  ADD KEY `FKAtributo_C398973` (`id_CRUD`);

--
-- Indices de la tabla `bd`
--
ALTER TABLE `bd`
  ADD PRIMARY KEY (`id_BD`);

--
-- Indices de la tabla `crud`
--
ALTER TABLE `crud`
  ADD PRIMARY KEY (`id_CRUD`),
  ADD KEY `FKCRUD500906` (`id_sitio`);

--
-- Indices de la tabla `fragmento`
--
ALTER TABLE `fragmento`
  ADD PRIMARY KEY (`id_fragmento`),
  ADD KEY `FKFragmento645433` (`id_tabla`),
  ADD KEY `FKFragmento641509` (`id_sitio`);

--
-- Indices de la tabla `fragmento_predicado`
--
ALTER TABLE `fragmento_predicado`
  ADD PRIMARY KEY (`id_fragmento`,`id_predicado`),
  ADD KEY `FKFragmento_207667` (`id_predicado`);

--
-- Indices de la tabla `predicado`
--
ALTER TABLE `predicado`
  ADD PRIMARY KEY (`id_predicado`);

--
-- Indices de la tabla `predicado_crud`
--
ALTER TABLE `predicado_crud`
  ADD PRIMARY KEY (`id_predicado`,`id_CRUD`),
  ADD KEY `FKPredicado_697576` (`id_CRUD`);

--
-- Indices de la tabla `sitio`
--
ALTER TABLE `sitio`
  ADD PRIMARY KEY (`id_sitio`);

--
-- Indices de la tabla `tabla`
--
ALTER TABLE `tabla`
  ADD PRIMARY KEY (`id_tabla`),
  ADD KEY `FKTabla649936` (`id_BD`);

--
-- Indices de la tabla `tupla`
--
ALTER TABLE `tupla`
  ADD PRIMARY KEY (`id_local`),
  ADD KEY `FKTupla358862` (`id_fragmento`);

--
-- Indices de la tabla `tupla_sitio`
--
ALTER TABLE `tupla_sitio`
  ADD PRIMARY KEY (`id_local`,`id_sitio`),
  ADD KEY `FKTupla_Siti187453` (`id_sitio`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `atributo`
--
ALTER TABLE `atributo`
  MODIFY `id_atributo` bigint(10) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `bd`
--
ALTER TABLE `bd`
  MODIFY `id_BD` bigint(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT de la tabla `crud`
--
ALTER TABLE `crud`
  MODIFY `id_CRUD` bigint(10) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `fragmento`
--
ALTER TABLE `fragmento`
  MODIFY `id_fragmento` bigint(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=91;

--
-- AUTO_INCREMENT de la tabla `predicado`
--
ALTER TABLE `predicado`
  MODIFY `id_predicado` bigint(10) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `sitio`
--
ALTER TABLE `sitio`
  MODIFY `id_sitio` bigint(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT de la tabla `tabla`
--
ALTER TABLE `tabla`
  MODIFY `id_tabla` bigint(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT de la tabla `tupla`
--
ALTER TABLE `tupla`
  MODIFY `id_local` bigint(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=77;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `atributo`
--
ALTER TABLE `atributo`
  ADD CONSTRAINT `FKAtributo35522` FOREIGN KEY (`id_fragmento`) REFERENCES `fragmento` (`id_fragmento`),
  ADD CONSTRAINT `atributo_ibfk_1` FOREIGN KEY (`id_tabla`) REFERENCES `tabla` (`id_tabla`);

--
-- Filtros para la tabla `atributo_crud`
--
ALTER TABLE `atributo_crud`
  ADD CONSTRAINT `FKAtributo_C398973` FOREIGN KEY (`id_CRUD`) REFERENCES `crud` (`id_CRUD`),
  ADD CONSTRAINT `FKAtributo_C771807` FOREIGN KEY (`id_atributo`) REFERENCES `atributo` (`id_atributo`);

--
-- Filtros para la tabla `crud`
--
ALTER TABLE `crud`
  ADD CONSTRAINT `FKCRUD500906` FOREIGN KEY (`id_sitio`) REFERENCES `sitio` (`id_sitio`);

--
-- Filtros para la tabla `fragmento`
--
ALTER TABLE `fragmento`
  ADD CONSTRAINT `FKFragmento641509` FOREIGN KEY (`id_sitio`) REFERENCES `sitio` (`id_sitio`),
  ADD CONSTRAINT `FKFragmento645433` FOREIGN KEY (`id_tabla`) REFERENCES `tabla` (`id_tabla`);

--
-- Filtros para la tabla `fragmento_predicado`
--
ALTER TABLE `fragmento_predicado`
  ADD CONSTRAINT `FKFragmento_207667` FOREIGN KEY (`id_predicado`) REFERENCES `predicado` (`id_predicado`),
  ADD CONSTRAINT `FKFragmento_87642` FOREIGN KEY (`id_fragmento`) REFERENCES `fragmento` (`id_fragmento`);

--
-- Filtros para la tabla `predicado_crud`
--
ALTER TABLE `predicado_crud`
  ADD CONSTRAINT `FKPredicado_489125` FOREIGN KEY (`id_predicado`) REFERENCES `predicado` (`id_predicado`),
  ADD CONSTRAINT `FKPredicado_697576` FOREIGN KEY (`id_CRUD`) REFERENCES `crud` (`id_CRUD`);

--
-- Filtros para la tabla `tabla`
--
ALTER TABLE `tabla`
  ADD CONSTRAINT `FKTabla649936` FOREIGN KEY (`id_BD`) REFERENCES `bd` (`id_BD`);

--
-- Filtros para la tabla `tupla`
--
ALTER TABLE `tupla`
  ADD CONSTRAINT `FKTupla358862` FOREIGN KEY (`id_fragmento`) REFERENCES `fragmento` (`id_fragmento`);

--
-- Filtros para la tabla `tupla_sitio`
--
ALTER TABLE `tupla_sitio`
  ADD CONSTRAINT `FKTupla_Siti187453` FOREIGN KEY (`id_sitio`) REFERENCES `sitio` (`id_sitio`),
  ADD CONSTRAINT `FKTupla_Siti515194` FOREIGN KEY (`id_local`) REFERENCES `tupla` (`id_local`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
