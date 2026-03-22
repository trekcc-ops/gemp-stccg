DROP TABLE gemp_db.league;
CREATE TABLE gemp_db.league (
  `league_id` INT(11) NOT NULL AUTO_INCREMENT,
  `properties` JSON COLLATE utf8_bin NOT NULL,
  `start` TIMESTAMP NOT NULL,
  `end` TIMESTAMP NOT NULL,
  PRIMARY KEY (league_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE gemp_db.league_match;
CREATE TABLE gemp_db.league_match (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `league_id` INT(11) NOT NULL,
  `series_name` VARCHAR(45) COLLATE utf8_bin NOT NULL,
  `winner` VARCHAR(45) COLLATE utf8_bin NOT NULL,
  `loser` VARCHAR(45) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `league_match_type` (`league_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE gemp_db.league_participation;
CREATE TABLE gemp_db.league_participation (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `league_id` int(11) NOT NULL,
  `player_name` varchar(45) COLLATE utf8_bin NOT NULL,
  `join_ip` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `league_participation_type` (`league_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;