package com.example.socialmedia.authentication;

import com.example.socialmedia.authentication.email.EmailSender;
import com.example.socialmedia.authentication.email.EmailValidator;
import com.example.socialmedia.authentication.email.token.ConfirmationToken;
import com.example.socialmedia.authentication.email.token.ConfirmationTokenService;
import com.example.socialmedia.user.User;
import com.example.socialmedia.user.UserRepository;
import com.example.socialmedia.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final EmailValidator emailValidator;

    @Autowired
    public AuthenticationService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                                 ConfirmationTokenService confirmationTokenService,
                                 EmailSender emailSender, EmailValidator emailValidator) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSender = emailSender;
        this.emailValidator = emailValidator;
    }

    public String login(LoginRequest loginRequest) {
        //TODO Custom login


        return "";
    }

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }

        boolean userExists = userRepository.findByEmail(request.getEmail()).isPresent();

        if (userExists) throw new IllegalStateException("email already taken");

        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                encodedPassword,
                UserRole.USER
        );

        user.setEnabled(true); //testing! need to delete it

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        String link = "http://localhost:8080/api/v1/auth/confirm?token=" + token;
        emailSender.send(request.getEmail(), buildEmail(link));

        return token;
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userRepository.enableUser(confirmationToken.getUser().getEmail());

        return "confirmed";
    }

    private String buildEmail(String link) {
        return "<div style=\"background-color: #43ad7c;\">\n" +
                "\n" +
                "  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "\n" +
                "    <tr>\n" +
                "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" +
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "          <tr>\n" +
                "            <td align=\"center\" valign=\"top\" style=\"padding: 36px 24px;\">\n" +
                "              <a href=\"http://localhost:8080/\" target=\"_blank\" style=\"display: inline-block;\">\n" +
                "                <img src=\"https://images.rawpixel.com/image_800/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDIyLTA1L2pvYjcyNi0yMDYtdi5qcGc.jpg\" alt=\"Logo\" border=\"0\" width=\"48\" style=\"display: block; width: 48px; max-width: 48px; min-width: 48px;\">\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "\n" +
                "    <tr>\n" +
                "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" +
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "          <tr>\n" +
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 36px 24px 0; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; border-top: 3px solid #d4dadf;\">\n" +
                "              <h1 style=\"margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -1px; line-height: 48px;\">Confirm Your Email Address</h1>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "\n" +
                "    <tr>\n" +
                "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" +
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "\n" +
                "          <tr>\n" +
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px;\">\n" +
                "              <p style=\"margin: 0;\">Tap the button below to confirm your email address. If you didn't create an account with <a href=\"http://localhost:8080/\">Lemonade</a>, you can safely delete this email.</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <tr>\n" +
                "            <td align=\"left\" bgcolor=\"#ffffff\">\n" +
                "              <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                <tr>\n" +
                "                  <td align=\"center\" bgcolor=\"#ffffff\" style=\"padding: 12px;\">\n" +
                "                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                      <tr>\n" +
                "                        <td align=\"center\" bgcolor=\"#1a82e2\" style=\"border-radius: 6px;\">\n" +
                "                          <a href=\"" + link + "\" target=\"_blank\" style=\"display: inline-block; padding: 16px 36px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; border-radius: 6px;\">Do Something Sweet</a>\n" +
                "                        </td>\n" +
                "                      </tr>\n" +
                "                    </table>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <tr>\n" +
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px;\">\n" +
                "              <p style=\"margin: 0;\">If that doesn't work, copy and paste the following link in your browser:</p>\n" +
                "              <p style=\"margin: 0;\"><a href=\"" + link + "\" target=\"_blank\">" + link + "</a></p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <tr>\n" +
                "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px; border-bottom: 3px solid #d4dadf\">\n" +
                "              <p style=\"margin: 0;\">Cheers,<br> Lemonade</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "\n" +
                "    <tr>\n" +
                "      <td align=\"center\" bgcolor=\"#e9ecef\" style=\"padding: 24px;\">\n" +
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "\n" +
                "          <tr>\n" +
                "            <td align=\"center\" bgcolor=\"#e9ecef\" style=\"padding: 12px 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 14px; line-height: 20px; color: #666;\">\n" +
                "              <p style=\"margin: 0;\">You received this email because we received a request for registration for your account. If you didn't request registration you can safely delete this email.</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <tr>\n" +
                "            <td align=\"center\" bgcolor=\"#e9ecef\" style=\"padding: 12px 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 14px; line-height: 20px; color: #666;\">\n" +
                "              <p style=\"margin: 0;\">To stop receiving these emails, you can unsubscribe at any time.</p>\n" +
                "              <p style=\"margin: 0;\">Locness Street 123/223</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "        </table>\n" +
                "    </tr>\n" +
                "\n" +
                "  </table>\n" +
                "</div>";
    }
}