package security.web.banking.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import security.web.banking.domain.User;
import security.web.banking.forms.UserCreateForm;
import security.web.banking.service.UserService;
import security.web.banking.validator.UserValidator;

import javax.validation.Valid;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserValidator userValidator;

    @InitBinder("userCreateForm")
    public void registerFormInitBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    @GetMapping("/registration")
    public String registration(@ModelAttribute("userCreateForm") UserCreateForm userForm) {
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@Valid @ModelAttribute("userCreateForm") UserCreateForm userForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        User user = userService.registerUser(userForm);
        logger.info("Registration succeeded.");
//        securityService.autoLogin(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "Your username and password is invalid.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }
}
