package vn.system.app.modules.jd.jdflow.service;

import org.springframework.stereotype.Service;

import vn.system.app.modules.user.domain.User;

@Service
public class JdPermissionService {

    /*
     * ==========================================
     * CHECK PERMISSION DUYỆT JD
     * ==========================================
     */
    public boolean hasApprovePermission(User user) {

        if (user == null || user.getRole() == null || user.getRole().getPermissions() == null) {
            return false;
        }

        return user.getRole().getPermissions().stream()
                .anyMatch(p -> "JD_FLOW".equalsIgnoreCase(p.getModule())
                        && "JD_APPROVE".equalsIgnoreCase(p.getName()));
    }

    /*
     * ==========================================
     * CHECK PERMISSION DUYỆT CUỐI JD
     * ==========================================
     */
    public boolean hasApproveFinalPermission(User user) {

        if (user == null || user.getRole() == null || user.getRole().getPermissions() == null) {
            return false;
        }

        return user.getRole().getPermissions().stream()
                .anyMatch(p -> "JD_FLOW".equalsIgnoreCase(p.getModule())
                        && "JD_APPROVE_FINAL".equalsIgnoreCase(p.getName()));
    }

    /*
     * ==========================================
     * CHECK PERMISSION BAN HÀNH JD
     * ==========================================
     */
    public boolean hasIssuePermission(User user) {

        if (user == null || user.getRole() == null || user.getRole().getPermissions() == null) {
            return false;
        }

        return user.getRole().getPermissions().stream()
                .anyMatch(p -> "JD_FLOW".equalsIgnoreCase(p.getModule())
                        && "JD_ISSUE".equalsIgnoreCase(p.getName()));
    }
}