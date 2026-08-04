package vn.system.app.modules.documentfolder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.repository.DocumentRepository;
import vn.system.app.modules.document.repository.DocumentShortcutRepository;
import vn.system.app.modules.document.service.DocumentService;
import vn.system.app.modules.documentcategory.domain.DocumentCategory;
import vn.system.app.modules.documentcategory.repository.DocumentCategoryRepository;
import vn.system.app.modules.documentfolder.domain.DocumentFolder;
import vn.system.app.modules.documentfolder.repository.DocumentFolderRepository;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@ExtendWith(MockitoExtension.class)
class DocumentFolderServiceTest {

    @Mock
    private DocumentFolderRepository folderRepository;
    @Mock
    private DocumentCategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserPositionRepository userPositionRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentShortcutRepository shortcutRepository;
    @Mock
    private DocumentService documentService;

    private DocumentFolderService service;
    private final AtomicLong generatedId = new AtomicLong(100);

    @BeforeEach
    void setUp() {
        service = new DocumentFolderService(
                folderRepository,
                categoryRepository,
                userRepository,
                userPositionRepository,
                documentRepository,
                shortcutRepository,
                documentService);

        when(folderRepository.save(any(DocumentFolder.class))).thenAnswer(invocation -> {
            DocumentFolder folder = invocation.getArgument(0);
            if (folder.getId() == null) {
                folder.setId(generatedId.getAndIncrement());
            }
            return folder;
        });
    }

    @Test
    void createsOneManagedFolderForEachActiveUserDocumentCategory() {
        when(folderRepository.findByOwnerIdAndParentIsNull("owner-1")).thenReturn(List.of());
        when(categoryRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(
                category(1L, "BIEU_MAU", "Biểu mẫu"),
                category(2L, "PHU_LUC", "Phụ lục"),
                category(3L, "ACCOUNTING_DOC", "Chứng từ kế toán")));

        service.initDefaultFoldersIfNecessary("owner-1");

        DocumentFolder yearFolder = savedYearFolder();
        assertThat(yearFolder.getFolderName()).isEqualTo("Năm " + LocalDate.now().getYear());
        assertThat(yearFolder.getChildren())
                .extracting(DocumentFolder::getFolderName)
                .containsExactly("01_Biểu mẫu", "02_Phụ lục");
        assertThat(yearFolder.getChildren())
                .extracting(DocumentFolder::getDocumentCategoryId)
                .containsExactly(1L, 2L);
    }

    @Test
    void replacesEmptyLegacyDefaultsButPreservesUserCreatedFolders() {
        DocumentFolder yearFolder = yearFolder("owner-1");
        for (String name : List.of(
                "01_Hóa đơn & Chứng từ",
                "02_Lương & Thuế",
                "03_Hợp đồng & Quyết định",
                "04_Bằng cấp & Chứng chỉ",
                "05_Tài liệu khác",
                "Hồ sơ riêng")) {
            yearFolder.getChildren().add(childFolder(generatedId.getAndIncrement(), name, yearFolder, null));
        }
        when(folderRepository.findByOwnerIdAndParentIsNull("owner-1")).thenReturn(List.of(yearFolder));
        when(categoryRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(
                category(1L, "BIEU_MAU", "Biểu mẫu"),
                category(2L, "PHU_LUC", "Phụ lục")));
        when(documentRepository.findByFolder_Id(any())).thenReturn(List.of());
        when(shortcutRepository.findByFolderId(any())).thenReturn(List.of());

        service.initDefaultFoldersIfNecessary("owner-1");

        assertThat(yearFolder.getChildren())
                .extracting(DocumentFolder::getFolderName)
                .containsExactly("Hồ sơ riêng", "01_Biểu mẫu", "02_Phụ lục");
    }

    @Test
    void renamesActiveManagedFolderAndKeepsInactiveFolderThatContainsDocuments() {
        DocumentFolder yearFolder = yearFolder("owner-1");
        DocumentFolder renamed = childFolder(2L, "01_Tên cũ", yearFolder, 10L);
        DocumentFolder inactiveWithData = childFolder(3L, "02_Loại đã tắt", yearFolder, 99L);
        yearFolder.setChildren(new ArrayList<>(List.of(renamed, inactiveWithData)));

        when(folderRepository.findByOwnerIdAndParentIsNull("owner-1")).thenReturn(List.of(yearFolder));
        when(categoryRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(
                category(10L, "HUONG_DAN", "Hướng dẫn")));
        when(documentRepository.findByFolder_Id(3L)).thenReturn(List.of(mock(Document.class)));

        service.initDefaultFoldersIfNecessary("owner-1");

        assertThat(renamed.getFolderName()).isEqualTo("01_Hướng dẫn");
        assertThat(yearFolder.getChildren()).containsExactly(renamed, inactiveWithData);
    }

    private DocumentFolder savedYearFolder() {
        try {
            return org.mockito.Mockito.mockingDetails(folderRepository)
                    .getInvocations().stream()
                    .filter(invocation -> invocation.getMethod().getName().equals("save"))
                    .map(invocation -> (DocumentFolder) invocation.getArgument(0))
                    .filter(folder -> folder.getParent() == null)
                    .findFirst()
                    .orElseThrow();
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    private DocumentFolder yearFolder(String ownerId) {
        DocumentFolder folder = new DocumentFolder();
        folder.setId(1L);
        folder.setOwnerId(ownerId);
        folder.setFolderType("PERSONAL");
        folder.setFolderName("Năm " + LocalDate.now().getYear());
        return folder;
    }

    private DocumentFolder childFolder(
            Long id,
            String name,
            DocumentFolder parent,
            Long documentCategoryId) {
        DocumentFolder folder = new DocumentFolder();
        folder.setId(id);
        folder.setOwnerId(parent.getOwnerId());
        folder.setFolderType("PERSONAL");
        folder.setFolderName(name);
        folder.setParent(parent);
        folder.setDocumentCategoryId(documentCategoryId);
        return folder;
    }

    private DocumentCategory category(Long id, String code, String name) {
        DocumentCategory category = new DocumentCategory();
        category.setId(id);
        category.setCategoryCode(code);
        category.setCategoryName(name);
        category.setActive(true);
        return category;
    }
}
