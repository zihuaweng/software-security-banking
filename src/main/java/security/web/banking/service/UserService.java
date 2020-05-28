package security.web.banking.service;

import security.web.banking.domain.User;
import security.web.banking.forms.UserCreateForm;

import java.util.Optional;

public interface UserService {

    Optional<User> findUserByUsername(String username);
    Optional<User> findUserById(long id);
    User registerUser(UserCreateForm userCreateForm);
}
