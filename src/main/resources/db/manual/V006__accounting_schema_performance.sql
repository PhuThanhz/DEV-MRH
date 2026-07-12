-- Accounting dossier schema reconciliation and hot-query indexes.
-- MySQL 8.0.29+ / MySQL 9+: safe to run repeatedly (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS accounting_approval_workflow_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(255) NOT NULL,
    company_id BIGINT NULL,
    dossier_category_id BIGINT NULL,
    business_type VARCHAR(80) NULL,
    priority INT NOT NULL DEFAULT 100,
    is_default BIT(1) NOT NULL DEFAULT b'0',
    status VARCHAR(30) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    effective_from DATETIME(6) NULL,
    effective_to DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS accounting_approval_workflow_scopes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    scope_type VARCHAR(30) NOT NULL,
    scope_id BIGINT NULL,
    include_children BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    CONSTRAINT fk_acc_wf_scope_template FOREIGN KEY (template_id)
        REFERENCES accounting_approval_workflow_templates (id)
);

CREATE TABLE IF NOT EXISTS accounting_approval_workflow_steps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    step_key VARCHAR(80) NOT NULL,
    step_order INT NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    approver_strategy VARCHAR(50) NOT NULL,
    approver_ref_id VARCHAR(255) NULL,
    position_reference_type VARCHAR(30) NULL,
    position_resolver_scope VARCHAR(40) NULL,
    approval_rule VARCHAR(30) NOT NULL,
    minimum_approvals INT NULL,
    required BIT(1) NOT NULL DEFAULT b'1',
    sla_minutes INT NULL,
    allow_delegation BIT(1) NOT NULL DEFAULT b'0',
    allow_forward BIT(1) NOT NULL DEFAULT b'0',
    allow_same_approver_collapse BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    CONSTRAINT fk_acc_wf_step_template FOREIGN KEY (template_id)
        REFERENCES accounting_approval_workflow_templates (id)
);

CREATE TABLE IF NOT EXISTS accounting_approval_instances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dossier_id BIGINT NOT NULL,
    submission_no INT NOT NULL,
    template_id BIGINT NULL,
    template_version INT NULL,
    status VARCHAR(30) NOT NULL,
    snapshot_json TEXT NULL,
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_acc_wf_instance_dossier FOREIGN KEY (dossier_id)
        REFERENCES accounting_dossier (id)
);

CREATE TABLE IF NOT EXISTS accounting_approval_delegations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    delegator_user_id VARCHAR(36) NOT NULL,
    delegate_user_id VARCHAR(36) NOT NULL,
    company_id BIGINT NULL,
    valid_from DATETIME(6) NOT NULL,
    valid_to DATETIME(6) NOT NULL,
    scope_type VARCHAR(50) NULL,
    scope_ref_id BIGINT NULL,
    reason VARCHAR(1000) NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    revoked_at DATETIME(6) NULL,
    revoked_by VARCHAR(255) NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS accounting_dossier_approval_steps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dossier_id BIGINT NOT NULL,
    instance_id BIGINT NULL,
    step_key VARCHAR(80) NULL,
    step_order INT NOT NULL,
    step_name VARCHAR(150) NOT NULL,
    approver_type VARCHAR(50) NOT NULL,
    approver_user_id VARCHAR(36) NULL,
    eligible_approver_ids VARCHAR(2000) NULL,
    status VARCHAR(40) NOT NULL,
    action_note VARCHAR(1000) NULL,
    acted_at DATETIME(6) NULL,
    due_at DATETIME(6) NULL,
    sla_minutes INT NULL,
    created_at DATETIME(6) NOT NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    version BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_acc_step_dossier FOREIGN KEY (dossier_id)
        REFERENCES accounting_dossier (id)
);

CREATE TABLE IF NOT EXISTS accounting_dossier_document_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dossier_document_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    document_id BIGINT NULL,
    file_url VARCHAR(1000) NULL,
    external_link VARCHAR(1000) NULL,
    change_note TEXT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_acc_doc_version_document FOREIGN KEY (dossier_document_id)
        REFERENCES accounting_dossier_document (id)
);

CREATE TABLE IF NOT EXISTS accounting_dossier_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dossier_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    payload TEXT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    error_message TEXT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_acc_outbox_idempotency (idempotency_key)
);

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

CREATE PROCEDURE add_accounting_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_columns TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX `', p_index, '` ON `', p_table, '` (', p_columns, ')');
        PREPARE statement_from_ddl FROM @ddl;
        EXECUTE statement_from_ddl;
        DEALLOCATE PREPARE statement_from_ddl;
    END IF;
END//
DELIMITER ;

CALL add_accounting_column_if_missing('accounting_dossier_document', 'invoice_date', 'DATETIME(6) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_document', 'invoice_number', 'VARCHAR(100) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_document', 'invoice_content', 'VARCHAR(500) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_document', 'partner_name', 'VARCHAR(255) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_document', 'partner_type', 'VARCHAR(50) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_document', 'amount', 'DECIMAL(19,2) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_document', 'currency', 'VARCHAR(10) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'target_type', 'VARCHAR(80) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'target_id', 'BIGINT NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'from_status', 'VARCHAR(80) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'to_status', 'VARCHAR(80) NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'before_value', 'TEXT NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'after_value', 'TEXT NULL');
CALL add_accounting_column_if_missing('accounting_dossier_audit_log', 'bulk_action_id', 'VARCHAR(80) NULL');

CALL add_accounting_index_if_missing('accounting_approval_workflow_templates', 'idx_acc_wf_template_company_status', 'company_id, status');
CALL add_accounting_index_if_missing('accounting_approval_workflow_templates', 'idx_acc_wf_template_category', 'dossier_category_id');
CALL add_accounting_index_if_missing('accounting_approval_workflow_templates', 'idx_acc_wf_template_priority', 'priority');
CALL add_accounting_index_if_missing('accounting_approval_workflow_scopes', 'idx_acc_wf_scope_lookup', 'scope_type, scope_id, template_id');
CALL add_accounting_index_if_missing('accounting_approval_workflow_steps', 'idx_acc_wf_step_template_order', 'template_id, step_order');
CALL add_accounting_index_if_missing('accounting_approval_instances', 'idx_acc_wf_instance_dossier', 'dossier_id, submission_no');
CALL add_accounting_index_if_missing('accounting_approval_instances', 'idx_acc_wf_instance_status', 'status');
CALL add_accounting_index_if_missing('accounting_approval_delegations', 'idx_acc_delegation_lookup', 'delegator_user_id, delegate_user_id, status, valid_from, valid_to');
CALL add_accounting_index_if_missing('accounting_approval_delegations', 'idx_acc_delegation_company', 'company_id, status');
CALL add_accounting_index_if_missing('accounting_dossier_approval_steps', 'idx_acc_step_dossier_active_order', 'dossier_id, active, step_order');
CALL add_accounting_index_if_missing('accounting_dossier_approval_steps', 'idx_acc_step_dossier_status', 'dossier_id, status, active');
CALL add_accounting_index_if_missing('accounting_dossier_approval_steps', 'idx_acc_step_approver_status', 'approver_user_id, status, active');
CALL add_accounting_index_if_missing('accounting_dossier_document', 'idx_acc_doc_dossier_active', 'dossier_id, active');
CALL add_accounting_index_if_missing('accounting_dossier_document', 'idx_acc_doc_invoice_partner', 'invoice_number, partner_name');
CALL add_accounting_index_if_missing('accounting_dossier_document_version', 'idx_acc_doc_version_document', 'dossier_document_id, version_no');
CALL add_accounting_index_if_missing('accounting_dossier_audit_log', 'idx_acc_audit_dossier_created', 'dossier_id, created_at');
CALL add_accounting_index_if_missing('accounting_dossier_audit_log', 'idx_acc_audit_bulk_action', 'bulk_action_id');

DROP PROCEDURE add_accounting_column_if_missing;
DROP PROCEDURE add_accounting_index_if_missing;
