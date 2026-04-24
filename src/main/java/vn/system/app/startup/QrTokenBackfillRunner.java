package vn.system.app.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.companyprocedure.repository.CompanyProcedureRepository;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureRepository;
import vn.system.app.modules.procedure.qr.service.ProcedureQrService;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrTokenBackfillRunner implements ApplicationRunner {

    private final CompanyProcedureRepository companyRepo;
    private final DepartmentProcedureRepository departmentRepo;
    private final ConfidentialProcedureRepository confidentialRepo;
    private final ProcedureQrService qrService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // --- Company ---
        var company = companyRepo.findByQrTokenIsNull();
        if (!company.isEmpty()) {
            log.info("[QR Backfill] CompanyProcedure: {} records cần backfill", company.size());
            company.forEach(p -> p.setQrToken(qrService.buildQrToken()));
            companyRepo.saveAll(company);
            log.info("[QR Backfill] CompanyProcedure: done");
        } else {
            log.info("[QR Backfill] CompanyProcedure: không có record nào cần backfill");
        }

        // --- Department ---
        var department = departmentRepo.findByQrTokenIsNull();
        if (!department.isEmpty()) {
            log.info("[QR Backfill] DepartmentProcedure: {} records cần backfill", department.size());
            department.forEach(p -> p.setQrToken(qrService.buildQrToken()));
            departmentRepo.saveAll(department);
            log.info("[QR Backfill] DepartmentProcedure: done");
        } else {
            log.info("[QR Backfill] DepartmentProcedure: không có record nào cần backfill");
        }

        // --- Confidential ---
        var confidential = confidentialRepo.findByQrTokenIsNull();
        if (!confidential.isEmpty()) {
            log.info("[QR Backfill] ConfidentialProcedure: {} records cần backfill", confidential.size());
            confidential.forEach(p -> p.setQrToken(qrService.buildQrToken()));
            confidentialRepo.saveAll(confidential);
            log.info("[QR Backfill] ConfidentialProcedure: done");
        } else {
            log.info("[QR Backfill] ConfidentialProcedure: không có record nào cần backfill");
        }

        log.info("[QR Backfill] Hoàn tất toàn bộ!");
    }
}