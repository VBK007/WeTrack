CREATE TABLE WE_TRACK_USERS_NO_OF_LOGIN
(
    ID                                 BIGSERIAL,
    USER_ID                            TEXT      NOT NULL,
    MOBILE_MODEL                       TEXT      NOT NULL,
    IP_ADDRESS                         TEXT      NOT NULL,
    COUNTRY                            TEXT      NOT NULL,
    ONE_SIGNAL_EXTERNAL_USERID         TEXT      NOT NULL,
    MOBILE_VERSION                     TEXT      NOT NULL,
    Expiry_TIME                        TEXT,
    IS_USER_CREATED_IN_WETRACK_SERVICE BOOLEAN,
    IS_PURCHASED                       BOOLEAN,
    TOKEN_HEADER                       TEXT,
    SCHEMA_NAME                        TEXT,
    IS_NUMBER_ADDER                    BOOLEAN,
    CREDIT_LIMIT                       INTEGER,
    PACKAGE_NAME                       TEXT      NOT NULL,
    CREATED_AT                         TIMESTAMP NOT NULL,
    UPDATED_AT                         TIMESTAMP NOT NULL,
    PRIMARY KEY (ID, USER_ID,PACKAGE_NAME)
);


CREATE INDEX WE_TRACK_USERS_NO_OF_LOGIN_INDEX
    ON WE_TRACK_USERS_NO_OF_LOGIN (USER_ID, IS_NUMBER_ADDER, Expiry_TIME,PACKAGE_NAME,CREDIT_LIMIT);
