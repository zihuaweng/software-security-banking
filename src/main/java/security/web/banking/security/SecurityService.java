package security.web.banking.security;

import security.web.banking.domain.User;

public interface SecurityService {
    String findLoggedInUsername();
    void autoLogin(User user);
}
