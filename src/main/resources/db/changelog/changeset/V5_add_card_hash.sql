ALTER TABLE cards ADD COLUMN card_hash VARCHAR(64) NOT NULL;
ALTER TABLE cards ADD CONSTRAINT uk_card_hash UNIQUE (card_hash);
CREATE INDEX idx_card_hash ON cards(card_hash);