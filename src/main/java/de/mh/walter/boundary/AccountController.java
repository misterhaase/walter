package de.mh.walter.boundary;

import de.mh.walter.boundary.bean.LoginRequest;
import de.mh.walter.boundary.bean.LoginResponse;
import de.mh.walter.boundary.bean.RegisterRequest;
import de.mh.walter.boundary.bean.RegisterResponse;
import de.mh.walter.control.Constants;
import de.mh.walter.control.UserController;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = AccountController.API_PATH)
public class AccountController {

    @Autowired
    private UserController userController;
    @Autowired
    private HttpServletRequest httpServletRequest;
    //
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-\\_]+@[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z0-9]{2,64}$");
    public static final String API_PATH = Constants.API_LOCATION + "/account";
    private static final Logger LOG = Logger.getLogger(AccountController.class.getName());

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        LOG.log(Level.INFO, "register request for user: {0}", username);

        if (username == null || username.length() < 5 || username.length() > 255 || !validEmailAddress(username) || userController.userExists(username)) {
            RegisterResponse errorResponse = new RegisterResponse();
            errorResponse.setMessage("invalid username");
            errorResponse.setSuccess(false);
            return errorResponse;
        }
        if (password == null || password.length() < 8) {
            RegisterResponse errorResponse = new RegisterResponse();
            errorResponse.setMessage("password too short");
            errorResponse.setSuccess(false);
            return errorResponse;
        }
        userController.createUser(username, password);
        RegisterResponse errorResponse = new RegisterResponse();
        errorResponse.setMessage(null);
        errorResponse.setSuccess(true);
        return errorResponse;
    }

    protected boolean validEmailAddress(String username) {
        return EMAIL_PATTERN.matcher(username).matches();
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (!Tarpit.tarpitOk(httpServletRequest)) {
            return error("you have to wait before trying again");
        }
        String username = request.getUsername();
        String password = request.getPassword();
        String applicationName = request.getApplicationName();
        LOG.log(Level.FINE, "login request for user: {0}", username);
        if (username == null || password == null) {
            return error("invalid input");
        }
        if (userController.checkPassword(username, password)) {
            String key = userController.createAndPersistKey(username, applicationName);
            if (key == null) {
                return error("strange things happen");
            }
            LoginResponse response = new LoginResponse();
            response.setKey(key);
            response.setMessage("OK");
            return response;
        }
        Tarpit.registerFailedAttempt(httpServletRequest);
        return error("unauthorized");
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout() {
        String key = httpServletRequest.getHeader("Authorization");
        if (key == null) {
            return;
        }
        userController.removeKey(key);
    }

    private LoginResponse error(String message) {
        LoginResponse response = new LoginResponse();
        response.setKey(null);
        response.setMessage(message);
        return response;
    }

}
