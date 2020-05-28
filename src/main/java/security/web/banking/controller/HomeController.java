package security.web.banking.controller;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import security.web.banking.auth.CustomUserDetails;
import security.web.banking.domain.Transaction;
import security.web.banking.domain.User;
import security.web.banking.forms.TransactionForm;
import security.web.banking.service.TransactionService;
import security.web.banking.service.UserService;
import security.web.banking.validator.TransactionValidator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
public class HomeController {

    private String JWT_PASSWORD = TextCodec.BASE64.encode("huahua");
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
    public TransactionForm getTransactionForm() {
        return transactionForm;
    }

    @InitBinder("transactionForm")
    public void registerFormInitBinder(WebDataBinder binder) {
        binder.addValidators(transactionValidator);
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long userId = customUserDetails.getId();
        return userService.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException(String.format("User with userId=%s was not found", userId)));
    }

    @ModelAttribute("allTransactions")
    public List<Transaction> getCurrentUserTransactions(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<Transaction> transactions = transactionService.findTransactionsByUserId(customUserDetails.getId());
        Collections.reverse(transactions);
        return transactions;
    }

    @GetMapping("/")
    public String transaction(@ModelAttribute("transactionForm") TransactionForm transactionForm,
                              @AuthenticationPrincipal CustomUserDetails customUserDetails,
                              HttpServletResponse response) {
        transactionForm.setUserId(customUserDetails.getId());
        transactionForm.setUserCurrentAmount(customUserDetails.getAmount());
        transactionForm.reset();
        // create jwt
        Claims claims = Jwts.claims().setIssuedAt(Date.from(Instant.now().plus(Duration.ofDays(10))));
        claims.put("user", customUserDetails.getUsername());
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, JWT_PASSWORD)
                .compact();
        Cookie cookie = new Cookie("access_token", token);
        response.addCookie(cookie);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return "transaction";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, params = "submitTransaction")
    public String processTransaction(@Valid @ModelAttribute("transactionForm") TransactionForm transactionForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            transactionForm.reset();
            return "transaction";
        }
        transactionService.processTransaction(transactionForm);
        logger.info("Transaction succeeded.");
        return "redirect:/";
    }


    @RequestMapping(value = "/", method = RequestMethod.POST, params = "clearTransaction")
    public String clearTransactionHistory(@CookieValue(value = "access_token") String accessToken,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (!StringUtils.isEmpty(accessToken)) {
            try {
                Jwt jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken);
                Claims claims = (Claims) jwt.getBody();
                boolean root = Boolean.parseBoolean((String) claims.get("root"));
                if (root) {
                    transactionService.deleteTransactionsByUserId(customUserDetails.getId());
                    logger.info("Clear transaction succeeded.");
                    return "redirect:/";
                }
            } catch (Exception e) {
                logger.info(e.toString());
            }
        }
        model.addAttribute("error", "You are not allowed to clear transactions");
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
