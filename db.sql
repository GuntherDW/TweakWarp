CREATE TABLE IF NOT EXISTS `warps` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `z` double NOT NULL,
  `rotX` float NOT NULL,
  `rotY` float NOT NULL,
  `world` varchar(25) NOT NULL DEFAULT 'world',
  `group` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE(`name`,`group`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
