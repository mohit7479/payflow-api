CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       full_name VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE wallets (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         user_id UUID NOT NULL REFERENCES users(id),
                         balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
                         currency VARCHAR(3) NOT NULL DEFAULT 'INR',
                         created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              wallet_id UUID NOT NULL REFERENCES wallets(id),
                              amount NUMERIC(19, 4) NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT now()
);