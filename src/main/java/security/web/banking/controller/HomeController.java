package security.web.banking.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import security.web.banking.auth.CustomUserDetails;
import security.web.banking.domain.User;
import security.web.banking.forms.TransactionForm;
import security.web.banking.service.TransactionService;
import security.web.banking.service.UserService;
import security.web.banking.validator.TransactionValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionValidator transactionValidator;
    @Autowired
    private TransactionForm transactionForm;

    @ModelAttribute("transactionForm")
    public TransactionForm getAddCardForm() {
        return transactionForm;
    }

    @InitBinder("transactionForm")
    public void registerFormInitBinder(WebDataBinder binder) {
        binder.addValidators(transactionValidator);
    }

    @ModelAttribute("currentUser")
    public User getCurrentUserCards(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long userId = customUserDetails.getId();
        return userService.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException(String.format("User with userId=%s was not found", userId)));
    }

    @GetMapping("/")
    public String transaction(@ModelAttribute("transactionForm") TransactionForm transactionForm,
                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        transactionForm.setUserId(customUserDetails.getId());
        transactionForm.setUserCurrentAmount(customUserDetails.getAmount());
        return "transaction";
    }

    @PostMapping("/")
    public String processTransaction(@Valid @ModelAttribute("transactionForm") TransactionForm transactionForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "transaction";
        }
        transactionService.processTransaction(transactionForm);
        model.addAttribute("successfulTransaction", "successfulTransaction");
        logger.info("Transaction succeed.");
        return "transaction";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/login";
    }
}
