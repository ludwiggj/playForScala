#!/usr/bin/env bash

USER_HOME=/Users/ludwiggj
PLAY_HOME=${USER_HOME}/code/play
PERSISTENCE_DB_HOME=${PLAY_HOME}/05_persistence_squeryl_stateful_relations/conf/db

DB_URL=jdbc:mysql://localhost:3306/paperclips
DB_DRIVER=com.mysql.jdbc.Driver
DB_USER=paper
DB_PASSWORD=cut

mysql -h 127.0.0.1 -P 3306 -u root < ${PERSISTENCE_DB_HOME}/dropAndRecreatePaperclipsDatabase.sql
mysql -h 127.0.0.1 -P 3306 -u root < ${PERSISTENCE_DB_HOME}/createPaperclipsBaseSchema.sql