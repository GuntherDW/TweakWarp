CREATE TABLE IF NOT EXISTS `warps` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `z` double NOT NULL,
  `pitch` float NOT NULL,
  `yaw` float NOT NULL,
  `world` varchar(25) NOT NULL DEFAULT 'world',
  `warpgroup` varchar(64) NOT NULL DEFAULT '',
  `accessgroup` varchar(64) NOT NULL DEFAULT ''
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
