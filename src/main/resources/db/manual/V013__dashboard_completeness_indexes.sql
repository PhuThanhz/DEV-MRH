CREATE INDEX idx_job_position_charts_department
    ON job_position_charts (department_id);

CREATE INDEX idx_permission_categories_department_active
    ON permission_categories (department_id, active);

CREATE INDEX idx_career_paths_department_active
    ON career_paths (department_id, active);

CREATE INDEX idx_department_job_titles_department_active
    ON department_job_titles (department_id, active);
