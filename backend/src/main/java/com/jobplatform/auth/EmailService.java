package com.jobplatform.auth;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@hireflow.com}")
    private String fromAddress;

    @Value("${app.mail.reset-url-base:http://localhost:3000/reset-password}")
    private String resetUrlBase;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String userName, String rawToken) {
        String resetUrl = resetUrlBase + "?token=" + rawToken;

        if (!mailEnabled) {
            log.info("[DEV MODE] Password reset requested for {} ({}). Reset link logged to console only.", userName, toEmail);
            log.info("[DEV MODE] Reset URL: {}", resetUrl);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("HireFlow - Reset Your Password");

            String htmlContent = buildResetEmailHtml(userName, resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildResetEmailHtml(String userName, String resetUrl) {
        String displayName = (userName != null && !userName.isBlank()) ? userName : "there";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;background-color:#f4f5f7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7;padding:40px 20px;">
                <tr>
                  <td align="center">
                    <table width="100%" cellpadding="0" cellspacing="0" style="max-width:560px;background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#171A2B 0%,#1e2235 100%);padding:32px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:700;letter-spacing:-0.3px;">HireFlow</h1>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px 40px 24px;">
                          <h2 style="margin:0 0 16px;color:#171A2B;font-size:20px;font-weight:600;">Reset Your Password</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            Hi <strong>%s</strong>,
                          </p>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            We received a request to reset the password for your HireFlow account. Click the button below to choose a new password:
                          </p>
                          <!-- Button -->
                          <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:32px;">
                            <tr>
                              <td align="center">
                                <a href="%s" style="display:inline-block;background-color:#635BFF;color:#ffffff;font-size:15px;font-weight:600;text-decoration:none;padding:14px 40px;border-radius:8px;letter-spacing:0.2px;">Reset Password</a>
                              </td>
                            </tr>
                          </table>
                          <p style="margin:0 0 16px;color:#999999;font-size:13px;line-height:1.5;">
                            This link expires in <strong>30 minutes</strong>. If you did not request a password reset, you can safely ignore this email — your password will remain unchanged.
                          </p>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="background-color:#f8f9fa;padding:24px 40px;border-top:1px solid #eeeeee;">
                          <p style="margin:0 0 4px;color:#999999;font-size:12px;line-height:1.5;">
                            This is a security message from HireFlow. Do not forward this email.
                          </p>
                          <p style="margin:0;color:#bbbbbb;font-size:11px;line-height:1.5;">
                            &copy; %d HireFlow. All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(displayName, resetUrl, java.time.Year.now().getValue());
    }
}
