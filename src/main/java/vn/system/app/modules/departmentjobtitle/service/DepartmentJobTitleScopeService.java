package vn.system.app.modules.departmentjobtitle.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;
import vn.system.app.modules.companyjobtitle.repository.CompanyJobTitleRepository;

import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.service.DepartmentService;

import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.domain.response.ResDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;

import vn.system.app.modules.jobtitle.domain.JobTitle;

import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;
import vn.system.app.modules.sectionjobtitle.repository.SectionJobTitleRepository;

import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DepartmentJobTitleScopeService {

    private final CompanyJobTitleRepository companyRepo;
    private final DepartmentJobTitleRepository deptRepo;
    private final SectionJobTitleRepository sectionRepo;

    private final DepartmentService departmentService;
    private final DepartmentJobTitleService mapperService; // reuse convertToResDTO()

    private final UserRepository userRepo;

    /*
     * =====================================================
     * LẤY DANH SÁCH QUYỀN CỦA USER HIỆN TẠI
     * =====================================================
     */
    private Set<String> getCurrentUserPermissions() {

        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (email == null) {
            throw new IdInvalidException("Không tìm thấy user đăng nhập");
        }
        User user = userRepo.findByEmail(email);

        if (user == null || user.getRole() == null) {
            return Collections.emptySet();
        }

        return user.getRole()
                .getPermissions()
                .stream()
                .map(p -> p.getName()) // ví dụ: COMPANY_VIEW_TITLE, DEPARTMENT_VIEW_TITLE
                .collect(Collectors.toSet());
    }

    /*
     * =====================================================
     * API: TRẢ VỀ CHỨC DANH THEO PHẠM VI QUYỀN
     * =====================================================
     */
    @Transactional(readOnly = true)
    public List<ResDepartmentJobTitleDTO> fetchByScope(Long departmentId) {

        Department department = departmentService.fetchEntityById(departmentId);
        Long companyId = department.getCompany().getId();

        // Lấy quyền người dùng
        Set<String> permissions = getCurrentUserPermissions();

        boolean canCompany = permissions.contains("COMPANY_VIEW_TITLE");
        boolean canDepartment = permissions.contains("DEPARTMENT_VIEW_TITLE");
        boolean canSection = permissions.contains("SECTION_VIEW_TITLE");

        if (!canCompany && !canDepartment && !canSection) {
            throw new IdInvalidException("Bạn không có quyền xem chức danh");
        }

        Map<Long, ResDepartmentJobTitleDTO> result = new LinkedHashMap<>();

        /*
         * =====================================================
         * SECTION (nhỏ nhất)
         * =====================================================
         */
        if (canSection) {
            List<SectionJobTitle> sectionList = sectionRepo.findBySection_Department_IdAndActiveTrue(departmentId);

            for (SectionJobTitle sjt : sectionList) {
                JobTitle jt = sjt.getJobTitle();
                ResDepartmentJobTitleDTO dto = mapperService.convertToResDTO(buildVirtual(department, jt));
                dto.setSource("SECTION");
                result.put(jt.getId(), dto);
            }
        }

        /*
         * =====================================================
         * DEPARTMENT
         * =====================================================
         */
        if (canDepartment) {
            List<DepartmentJobTitle> deptList = deptRepo.findByDepartment_IdAndActiveTrue(departmentId);

            for (DepartmentJobTitle djt : deptList) {
                Long jobId = djt.getJobTitle().getId();
                if (!result.containsKey(jobId)) {
                    ResDepartmentJobTitleDTO dto = mapperService.convertToResDTO(djt);
                    dto.setSource("DEPARTMENT");
                    result.put(jobId, dto);
                }
            }
        }

        /*
         * =====================================================
         * COMPANY (quyền lớn nhất)
         * =====================================================
         */
        if (canCompany) {
            List<CompanyJobTitle> companyList = companyRepo.findByCompany_IdAndActiveTrue(companyId);

            for (CompanyJobTitle cjt : companyList) {
                Long jobId = cjt.getJobTitle().getId();
                if (!result.containsKey(jobId)) {
                    ResDepartmentJobTitleDTO dto = mapperService
                            .convertToResDTO(buildVirtual(department, cjt.getJobTitle()));
                    dto.setSource("COMPANY");
                    result.put(jobId, dto);
                }
            }
        }

        return sort(result);
    }

    /*
     * =====================================================
     * BUILD VIRTUAL DEPARTMENT_JOB_TITLE
     * =====================================================
     */
    private DepartmentJobTitle buildVirtual(Department department, JobTitle jobTitle) {
        DepartmentJobTitle fake = new DepartmentJobTitle();
        fake.setDepartment(department);
        fake.setJobTitle(jobTitle);
        fake.setActive(false);
        return fake;
    }

    /*
     * =====================================================
     * SORT — theo BandOrder & LevelNumber
     * =====================================================
     */
    private List<ResDepartmentJobTitleDTO> sort(Map<Long, ResDepartmentJobTitleDTO> map) {
        return map.values().stream()
                .sorted(
                        Comparator
                                .comparing((ResDepartmentJobTitleDTO dto) -> dto.getJobTitle().getBandOrder() != null
                                        ? dto.getJobTitle().getBandOrder()
                                        : Integer.MAX_VALUE)
                                .thenComparing(dto -> dto.getJobTitle().getLevelNumber() != null
                                        ? dto.getJobTitle().getLevelNumber()
                                        : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }
}