create table account_transactions_changelog
(
    id             uuid primary key default gen_random_uuid(),
    account_id     uuid        not null,
    transaction_id uuid        not null unique,
    amount         bigint      not null,
    "type"         text        not null,
    occurred_at    timestamptz not null
);

comment on table account_transactions_changelog is 'Account transactions changelog';
comment on column account_transactions_changelog.id is 'ID of changelog record';
comment on column account_transactions_changelog.account_id is 'Account ID';
comment on column account_transactions_changelog.transaction_id is 'Transaction ID';
comment on column account_transactions_changelog.amount is 'Amount of money';
comment on column account_transactions_changelog.type is 'Type of transaction. Deposit or withdrawal';
comment on column account_transactions_changelog.occurred_at is 'The time at which transaction occurred in source system';