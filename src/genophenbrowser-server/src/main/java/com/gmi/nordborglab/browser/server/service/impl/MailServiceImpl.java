package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.service.MailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.11.13
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */

@Service
public class MailServiceImpl implements MailService {

    private final static String FROM = "GWA-Portal@gmi.oeaw.ac.at";

    @Value("${RESETPW.url}")
    private String RESET_PASSWORD_URL;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private Configuration configuration;


    @Override
    public void sendPasswordResetLink(final AppUser user) {
        try {
            if (user.getPasswordResetToken() == null || user.getPasswordResetToken().isEmpty()) {
                return;
            }
            Template template = configuration.getTemplate("resetpassword.ftl");
            final Map model = new HashMap();
            model.put("name", user.getName());
            String password_reset_url = RESET_PASSWORD_URL + "?token=" + user.getPasswordResetToken();
            model.put("password_reset_url", password_reset_url);
            final String mailBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            MimeMessagePreparator preparator = new MimeMessagePreparator() {

                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                    message.setTo(user.getEmail());
                    message.setSubject("GWA-Portal password reset confirmation");
                    message.setFrom(FROM);
                    message.setText(mailBody, false);
                }
            };
            mailSender.send(preparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPasswordChanged(final AppUser user) {
        try {
            Template template = configuration.getTemplate("passwordchanged.ftl");
            final Map model = new HashMap();
            model.put("name", user.getName());
            final String mailBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            MimeMessagePreparator preparator = new MimeMessagePreparator() {

                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                    message.setTo(user.getEmail());
                    message.setSubject("GWA-Portal password changed");
                    message.setFrom(FROM);
                    message.setText(mailBody, false);
                }
            };
            mailSender.send(preparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
