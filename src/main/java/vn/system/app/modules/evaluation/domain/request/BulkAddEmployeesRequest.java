package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;
import java.util.List;

@Data
public class BulkAddEmployeesRequest {
    private List<AddPeriodEmployeeRequest> employees;
}
