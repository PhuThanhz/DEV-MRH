-- Phase 2 performance indexes. Run manually with mysql CLI, preferably outside peak load.
DELIMITER //
CREATE PROCEDURE add_accounting_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_columns TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index) THEN
        SET @ddl = CONCAT('CREATE INDEX `', p_index, '` ON `', p_table, '` (', p_columns, ')');
        PREPARE statement_from_ddl FROM @ddl;
        EXECUTE statement_from_ddl;
        DEALLOCATE PREPARE statement_from_ddl;
    END IF;
END//
DELIMITER ;

CALL add_accounting_index_if_missing('accounting_dossier_outbox', 'idx_acc_outbox_status_next_retry_at', 'status, next_retry_at');
CALL add_accounting_index_if_missing('accounting_dossier_approval_steps', 'idx_acc_step_status_active_due_at', 'status, active, due_at');
DROP PROCEDURE add_accounting_index_if_missing;
