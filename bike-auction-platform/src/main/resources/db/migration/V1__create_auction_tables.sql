CREATE TABLE auctions (
                          id UUID PRIMARY KEY,
                          title VARCHAR(150) NOT NULL,
                          starting_price NUMERIC(12, 2) NOT NULL,
                          current_price NUMERIC(12, 2) NOT NULL,
                          minimum_increment NUMERIC(12, 2) NOT NULL,
                          starts_at TIMESTAMPTZ NOT NULL,
                          ends_at TIMESTAMPTZ NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          version BIGINT,

                          CONSTRAINT chk_auction_starting_price
                              CHECK (starting_price >= 0),

                          CONSTRAINT chk_auction_current_price
                              CHECK (current_price >= starting_price),

                          CONSTRAINT chk_auction_minimum_increment
                              CHECK (minimum_increment > 0),

                          CONSTRAINT chk_auction_dates
                              CHECK (ends_at > starts_at),

                          CONSTRAINT chk_auction_status
                              CHECK (
                                  status IN ('SCHEDULED', 'LIVE', 'CLOSED', 'CANCELLED')
                                  )
);

CREATE INDEX idx_auctions_status_ends_at
    ON auctions (status, ends_at);

CREATE TABLE bids (
                      id UUID PRIMARY KEY,
                      auction_id UUID NOT NULL REFERENCES auctions(id),
                      bidder_id UUID NOT NULL,
                      amount NUMERIC(12, 2) NOT NULL,
                      request_id UUID NOT NULL,
                      created_at TIMESTAMPTZ NOT NULL,

                      CONSTRAINT uk_bid_auction_request
                          UNIQUE (auction_id, request_id),

                      CONSTRAINT chk_bid_amount
                          CHECK (amount > 0)
);

CREATE INDEX idx_bid_auction_created
    ON bids (auction_id, created_at DESC);