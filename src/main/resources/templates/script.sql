CREATE TABLE MONEY_TRANSFERS
(
  ID VARCHAR(40) PRIMARY KEY NOT NULL,
  CREATIONDATE TIMESTAMP NOT NULL,
  REQUEST BLOB NOT NULL
);
CREATE UNIQUE INDEX table_name_ID_uindex ON MONEY_TRANSFERS (ID);