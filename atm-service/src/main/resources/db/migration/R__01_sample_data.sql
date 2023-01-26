insert into accounts(id, balance)
values ('72a0cfd0-8f38-4939-8100-72332f65b91f', 100);

insert into account_transactions(account_id, amount, "type", "order", occurred_at, idempotency_key)
values ('72a0cfd0-8f38-4939-8100-72332f65b91f', 100, 'DEPOSIT', nextval('account_transactions_order_sequence'), now(), 'a2a5b8e4-60aa-4472-bcb5-38e899e9aa46');