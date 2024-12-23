-- Continuing Mission
UPDATE gemp_db.deck
SET contents = REPLACE(contents, '178_044', '155_022')

-- Reshape the Quadrant
UPDATE gemp_db.deck
SET contents = REPLACE(contents, '178_046', '167_030')