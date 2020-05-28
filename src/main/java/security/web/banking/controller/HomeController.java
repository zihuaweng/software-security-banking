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
    public TransactionForm getAddCardForm() {
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
        // create jwt
        Claims claims = Jwts.claims().setIssuedAt(Date.from(Instant.now().plus(Duration.ofDays(10))));
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
            return "transaction";
        }
        transactionService.processTransaction(transactionForm);
        model.addAttribute("successfulTransaction", "successfulTransaction"); // todo
        logger.info("Transaction succeed.");
        return "redirect:/";
    }


    @RequestMapping(value = "/", method = RequestMethod.POST, params = "clearTransaction")
    public String clearTransactionHistory(@CookieValue(value = "access_token") String accessToken,
                                          BindingResult bindingResult,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (bindingResult.hasErrors()) {
            return "transaction";
        }
        if (!StringUtils.isEmpty(accessToken)) {
            try {
                Jwt jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken);
                Claims claims = (Claims) jwt.getBody();
                boolean hasSuperPower = Boolean.valueOf((String) claims.get("superPower"));
                if (hasSuperPower) {
                    transactionService.deleteTransactionsByUserId(customUserDetails.getId());
                    logger.info("Clear transaction succeed.");
                    return "redirect:/";
                }
            } catch (JwtException e) {
                model.addAttribute("error", "You are not allow to clear transactions"); // todo
                return "redirect:/";
            }
        }
        model.addAttribute("error", "You are not allow to clear transactions"); // todo
        return "redirect:/";
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
