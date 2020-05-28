package security.web.banking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import security.web.banking.domain.User;
import security.web.banking.forms.UserCreateForm;
import security.web.banking.repository.UserRepository;
import security.web.banking.service.UserService;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public Optional<User> findUserById(long id) {
        return userRepository.findUserById(id);
    }

    @Override
    public User registerUser(UserCreateForm userCreateForm) {
        User user = new User();
        user.setUsername(userCreateForm.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(userCreateForm.getPassword()));
        user.setAmount(userCreateForm.getAmount());
        return userRepository.save(user);
    }

}
