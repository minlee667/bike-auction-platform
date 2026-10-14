
ALTER TABLE auctions
    ADD COLUMN seller_id UUID;

ALTER TABLE auctions
    ADD CONSTRAINT fk_auctions_seller
        FOREIGN KEY (seller_id)
            REFERENCES users(id);

CREATE INDEX idx_auctions_seller_id
    ON auctions(seller_id);
