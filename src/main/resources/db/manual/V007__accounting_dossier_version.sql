DELIMITER //
CREATE PROCEDURE add_accounting_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE statement_from_ddl FROM @ddl;
        EXECUTE statement_from_ddl;
        DEALLOCATE PREPARE statement_from_ddl;
    END IF;
END//
DELIMITER ;

CALL add_accounting_column_if_missing('accounting_dossier', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL add_accounting_column_if_missing('accounting_dossier_approval_steps', 'allow_delegation', 'BIT(1) NOT NULL DEFAULT b\'0\'');

-- Backfill dữ liệu cũ
UPDATE `accounting_dossier` SET `version` = 0 WHERE `version` IS NULL;
UPDATE `accounting_dossier_approval_steps` SET `allow_delegation` = b'0' WHERE `allow_delegation` IS NULL;

DROP PROCEDURE add_accounting_column_if_missing;
