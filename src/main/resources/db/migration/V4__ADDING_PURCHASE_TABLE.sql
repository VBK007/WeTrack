CREATE TABLE PURCHASED_DETAILS_HISTORY
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


alter table WE_TRACK_USERS add column  purchase_mode TEXT;
alter table WE_TRACK_USERS add column  MAX_NUMBER INTEGER;


CREATE TABLE PROMO_CODE
(
    ID                BIGSERIAL,
    PROMO_CODE           TEXT NOT NULL,
    PROMO_MAX_NUMBER     INTEGER NOT NULL,
    PRIMARY KEY (ID,PROMO_CODE)
);


CREATE TABLE UPI_DETAILS
(
    ID                      BIGSERIAL,
    UPI_ID                  TEXT,
    PURCHASE_TYPE           TEXT,
    PURCHASE_DESCRIBITION   TEXT,
    MONEY_IN_INR            TEXT,
    MONEY_IN_USD            TEXT,
    COLOR_CODE              TEXT,
    COLOR_BAR               TEXT,
    CREATED_AT    TIMESTAMP NOT NULL,
    UPDATED_AT    TIMESTAMP NOT NULL,
    PRIMARY KEY (ID,UPI_ID)
);

insert into UPI_DETAILS (UPI_ID, PURCHASE_TYPE, PURCHASE_DESCRIBITION, MONEY_IN_INR, MONEY_IN_USD, COLOR_CODE, COLOR_BAR, CREATED_AT, UPDATED_AT)
values ('8778471128@ybl','Standard','Weekly Subscriptions','₹330.00','$5','#ffe7d8','#f2a814',current_timestamp,current_timestamp);

insert into UPI_DETAILS (UPI_ID, PURCHASE_TYPE, PURCHASE_DESCRIBITION, MONEY_IN_INR, MONEY_IN_USD, COLOR_CODE, COLOR_BAR, CREATED_AT, UPDATED_AT)
values ('8778471128@ybl','Popular','Monthly Subscriptions','₹750.00','$10','#01cd88','#ffffff',current_timestamp,current_timestamp);


insert into UPI_DETAILS (UPI_ID, PURCHASE_TYPE, PURCHASE_DESCRIBITION, MONEY_IN_INR, MONEY_IN_USD, COLOR_CODE, COLOR_BAR, CREATED_AT, UPDATED_AT)
    values ('8778471128@ybl','Deluxe','3 Months Subscriptions','₹2,450.00','$30','#c4f1ff','#2f26db',current_timestamp,current_timestamp);


insert into UPI_DETAILS (UPI_ID, PURCHASE_TYPE, PURCHASE_DESCRIBITION, MONEY_IN_INR, MONEY_IN_USD, COLOR_CODE, COLOR_BAR, CREATED_AT, UPDATED_AT)
    values ('8778471128@ybl','PromoCode','Promo Code Subscriptions','₹2,450.00','$30','#5ee0ff','#4f0805',current_timestamp,current_timestamp);
