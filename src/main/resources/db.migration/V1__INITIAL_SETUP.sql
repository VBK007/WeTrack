CREATE TABLE WE_TRACK_USERS
(
    ID                               BIGSERIAL,
    USER_ID                          TEXT,
    MOBILE_MODEL                     TEXT,
    IP_ADDRESS                       TEXT,
    COUNTRY                          TEXT,
    ONE_SIGNAL_EXTERNAL_USERID       TEXT,
    MOBILE_VERSION                   TEXT,
    Expiry_TIME                      TEXT,
    IS_PURCHASED                      BOOLEAN,
    SCHEMA_NAME                      TEXT,
    CREATED_AT    TIMESTAMP NOT NULL,
    UPDATED_AT    TIMESTAMP NOT NULL,
    PRIMARY KEY (ID,USER_ID)
);



CREATE TABLE AUTH_TOKEN
(
    ID            BIGSERIAL,
    USER_ID       TEXT,
    AUTH_TOKEN     TEXT,
    CREATED_AT    TIMESTAMP NOT NULL,
    UPDATED_AT    TIMESTAMP NOT NULL,
    PRIMARY KEY (ID,USER_ID)
);




CREATE TABLE PURCHASED_DETAILS
(
    ID                               BIGSERIAL,
    USER_ID                          TEXT,
    PURCHASE_MODE                    TEXT,
    PURCHASE_PLATFORM                TEXT,
    COUNTRY                          TEXT,
    AMOUNT                           TEXT,
    TRANSATION_ID                    TEXT,
    TRANSACTION_REMARK               TEXT,
    EXPIRY_DATE                      TEXT,
    CREATED_AT    TIMESTAMP NOT NULL,
    UPDATED_AT    TIMESTAMP NOT NULL,
    PRIMARY KEY (ID,USER_ID)
);


CREATE TABLE NUMBER_FOR_USERS
(
    ID                BIGSERIAL,
    USER_ID           TEXT,
    NUMBER            TEXT,
    TOKEN_HEADER      TEXT,
    COUNTRY_CODE      TEXT,
    CREATED_AT       TIMESTAMP NOT NULL,
    UPDATED_AT       TIMESTAMP NOT NULL,
    PRIMARY KEY (ID,USER_ID)
);



CREATE TABLE MODEL_COMBINATION
(
    ID                BIGSERIAL,
    MODEL_ID           TEXT,
    MODEL_NUMBER       TEXT,
    CREATED_AT       TIMESTAMP NOT NULL,
    UPDATED_AT       TIMESTAMP NOT NULL,
    PRIMARY KEY (ID,MODEL_ID)
);









