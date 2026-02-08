package vn.system.app.modules.departmentjobtitle.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class DepartmentJobTitleQueryService {

    private final DepartmentJobTitleRepository deptRepo;
    private final SectionJobTitleRepository sectionRepo;
    private final CompanyJobTitleRepository companyRepo;
    private final DepartmentService departmentService;
    private final DepartmentJobTitleService sharedMapper; // để reuse convertToResDTO()

    /*
     * =====================================================
     * API MỚI — LẤY CHỈ PHÒNG BAN + BỘ PHẬN
     * =====================================================
     */
    @Transactional(readOnly = true)
    public List<ResDepartmentJobTitleDTO> fetchDeptAndSectionJobTitles(Long departmentId) {

        Department department = departmentService.fetchEntityById(departmentId);

        List<DepartmentJobTitle> deptList = deptRepo.findByDepartment_IdAndActiveTrue(departmentId);

        List<SectionJobTitle> sectionList = sectionRepo.findBySection_Department_IdAndActiveTrue(departmentId);

        Map<Long, ResDepartmentJobTitleDTO> result = new LinkedHashMap<>();

        // SECTION trước vì ưu tiên nhỏ nhất
        for (SectionJobTitle sjt : sectionList) {
            JobTitle jt = sjt.getJobTitle();
            ResDepartmentJobTitleDTO dto = sharedMapper.convertToResDTO(
                    buildVirtual(department, jt));
            dto.setSource("SECTION");
            result.put(jt.getId(), dto);
        }

        // DEPARTMENT
        for (DepartmentJobTitle djt : deptList) {
            JobTitle jt = djt.getJobTitle();
            if (!result.containsKey(jt.getId())) {
                ResDepartmentJobTitleDTO dto = sharedMapper.convertToResDTO(djt);
                dto.setSource("DEPARTMENT");
                result.put(jt.getId(), dto);
            }
        }

        return sort(result);
    }

    /*
     * =====================================================
     * API CŨ — CÔNG TY + PHÒNG BAN + BỘ PHẬN (GIỮ NGUYÊN)
     * =====================================================
     */
    @Transactional(readOnly = true)
    public List<ResDepartmentJobTitleDTO> fetchCompanyDeptSection(Long departmentId) {

        Department department = departmentService.fetchEntityById(departmentId);
        Long companyId = department.getCompany().getId();

        List<DepartmentJobTitle> deptList = deptRepo.findByDepartment_IdAndActiveTrue(departmentId);

        List<SectionJobTitle> sectionList = sectionRepo.findBySection_Department_IdAndActiveTrue(departmentId);

        List<CompanyJobTitle> companyList = companyRepo.findByCompany_IdAndActiveTrue(companyId);

        Map<Long, ResDepartmentJobTitleDTO> result = new LinkedHashMap<>();

        // SECTION
        for (SectionJobTitle sjt : sectionList) {
            ResDepartmentJobTitleDTO dto = sharedMapper.convertToResDTO(
                    buildVirtual(department, sjt.getJobTitle()));
            dto.setSource("SECTION");
            result.put(sjt.getJobTitle().getId(), dto);
        }

        // DEPARTMENT
        for (DepartmentJobTitle djt : deptList) {
            Long jobId = djt.getJobTitle().getId();
            if (!result.containsKey(jobId)) {
                ResDepartmentJobTitleDTO dto = sharedMapper.convertToResDTO(djt);
                dto.setSource("DEPARTMENT");
                result.put(jobId, dto);
            }
        }

        // COMPANY
        for (CompanyJobTitle cjt : companyList) {
            Long jobId = cjt.getJobTitle().getId();
            if (!result.containsKey(jobId)) {
                ResDepartmentJobTitleDTO dto = sharedMapper.convertToResDTO(
                        buildVirtual(department, cjt.getJobTitle()));
                dto.setSource("COMPANY");
                result.put(jobId, dto);
            }
        }

        return sort(result);
    }

    // ==========================
    // Helper build virtual
    // ==========================
    private DepartmentJobTitle buildVirtual(Department department, JobTitle jobTitle) {
        DepartmentJobTitle fake = new DepartmentJobTitle();
        fake.setDepartment(department);
        fake.setJobTitle(jobTitle);
        fake.setActive(false);
        return fake;
    }

    // ==========================
    // Sort final list
    // ==========================
    private List<ResDepartmentJobTitleDTO> sort(Map<Long, ResDepartmentJobTitleDTO> map) {
        return map.values().stream()
                .sorted(Comparator
                        .comparing((ResDepartmentJobTitleDTO dto) -> dto.getJobTitle().getBandOrder() != null
                                ? dto.getJobTitle().getBandOrder()
                                : Integer.MAX_VALUE)
                        .thenComparing(dto -> dto.getJobTitle().getLevelNumber() != null
                                ? dto.getJobTitle().getLevelNumber()
                                : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }
}
