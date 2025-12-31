CREATE TABLE IF NOT EXISTS `members`
(
    `uuid`      CHAR(36) PRIMARY KEY NOT NULL COLLATE NOCASE,
    `nation`    CHAR(2)                       DEFAULT NULL COLLATE NOCASE,
    `staff`     TINYINT(1)           NOT NULL DEFAULT 0,
    `alt_owner` CHAR(36)                      DEFAULT NULL COLLATE NOCASE,
    `added`     DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `tokens`
(
    `token`     CHAR(36) PRIMARY KEY NOT NULL COLLATE NOCASE,
    `member`    CHAR(36)             NOT NULL COLLATE NOCASE,
    `created`   CHAR(36)             NOT NULL COLLATE NOCASE,
    `last_used` DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `nations`
(
    `id`            CHAR(2) PRIMARY KEY NOT NULL COLLATE NOCASE,
    `name`          VARCHAR(128)        NOT NULL COLLATE NOCASE,
    `short_name`    VARCHAR(16)         NOT NULL COLLATE NOCASE,
    `color`         CHAR(6)             NOT NULL COLLATE NOCASE,
    `leader`        CHAR(36)            NOT NULL COLLATE NOCASE,
    `vice`          CHAR(36)            NOT NULL COLLATE NOCASE,
    `founded`       DATETIME            NOT NULL,
    `founded_ticks` INTEGER             NOT NULL,
    `bank`          CHAR(16)            NOT NULL COLLATE BINARY
);

CREATE TABLE IF NOT EXISTS `citizen_requests`
(
    `member`  CHAR(36)   NOT NULL COLLATE NOCASE,
    `nation`  CHAR(2)    NOT NULL COLLATE NOCASE,
    `mode`    TINYINT(1) NOT NULL, -- 0 = nation invites, 1 = member requests
    `created` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires` DATETIME   NOT NULL,
    PRIMARY KEY (`member`, `nation`)
);

DELETE
from `citizen_requests`
WHERE `expires` >= CURRENT_TIMESTAMP;
