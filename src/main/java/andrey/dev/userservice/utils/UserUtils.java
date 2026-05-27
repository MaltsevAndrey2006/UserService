package andrey.dev.userservice.utils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        throw new SecurityException("Invalid principal type");
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        return authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElseThrow(() -> new SecurityException("No role found"));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    public boolean isUser() {
        return "USER".equals(getCurrentUserRole());
    }

    public boolean isCurrentUserOrAdmin(Long resourceUserId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId.equals(resourceUserId) || isAdmin();
    }

    public void checkAccessToUser(Long resourceUserId) {
        if (!isCurrentUserOrAdmin(resourceUserId)) {
            throw new AccessDeniedException("You can only access your own resources");
        }
    }
}
