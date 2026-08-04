-- Đánh dấu thư mục hệ thống được sinh từ danh mục loại văn bản.
-- Không tạo foreign key: khi danh mục bị xóa, thư mục cũ có dữ liệu vẫn phải được giữ an toàn.
ALTER TABLE document_folders
    ADD COLUMN document_category_id BIGINT NULL;

CREATE INDEX idx_document_folders_document_category
    ON document_folders (document_category_id);
