CREATE TABLE `techcmsdb`.`cms_tables_relation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `tables_str` VARCHAR(200) NULL DEFAULT '',
  `relation` VARCHAR(1000) NULL DEFAULT '',
  `target` VARCHAR(100) NULL DEFAULT '',
  PRIMARY KEY (`id`));
