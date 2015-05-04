-- Create syntax for TABLE 'articles'
CREATE TABLE `articles` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `stock` varchar(11) CHARACTER SET utf8mb4 DEFAULT NULL,
  `date` date DEFAULT NULL,
  `source` varchar(512) CHARACTER SET utf8mb4 DEFAULT NULL,
  `title` varchar(512) CHARACTER SET utf8mb4 DEFAULT NULL,
  `importance` int(11) DEFAULT NULL,
  `sentiment` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `stock` (`stock`),
  KEY `date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'scores'
CREATE TABLE `scores` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `stock` varchar(11) CHARACTER SET utf8mb4 DEFAULT NULL,
  `date` date DEFAULT NULL,
  `sentiment` int(11) DEFAULT NULL COMMENT '[0, 100]',
  `volume` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `stock` (`stock`),
  KEY `date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'stocks'
CREATE TABLE `stocks` (
  `stock` varchar(11) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
  `stockname` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  PRIMARY KEY (`stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;