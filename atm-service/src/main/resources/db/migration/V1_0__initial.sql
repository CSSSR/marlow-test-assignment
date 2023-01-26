create table accounts
(
    id      uuid primary key default gen_random_uuid(),
    balance bigint not null check ( balance >= 0 )
);
comment on table accounts is 'User accounts';
comment on column accounts.id is 'Account ID';
comment on column accounts.balance is 'Account balance';

create table account_transactions
(
    id              uuid primary key default gen_random_uuid(),
    account_id      uuid        not null references accounts,
    amount          bigint      not null check ( amount > 0 ),
    "type"          text        not null,
    "order"         bigint      not null,
    occurred_at     timestamptz not null,
    idempotency_key text        not null unique,
    event_sent_at   timestamptz
);

comment on table accounts is 'Account transactions';
comment on column account_transactions.id is 'Transaction ID';
comment on column account_transactions.account_id is 'Account ID';
comment on column account_transactions.amount is 'Amount of money to withdraw or deposit';
comment on column account_transactions.type is 'Type of transaction (DEPOSIT, WITHDRAWAL)';
comment on column account_transactions."order" is 'Global order of transactions';
comment on column account_transactions.occurred_at is 'The time at which the transaction occurred';
comment on column account_transactions.idempotency_key is 'Idempotency key for transaction';
comment on column account_transactions.event_sent_at is 'The time at which an event about transaction was sent to the queue';

create sequence account_transactions_order_sequence;
comment on sequence account_transactions_order_sequence is 'Order sequence for account transactions';