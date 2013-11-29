package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.11.13
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */

public interface MailService {

    public void sendPasswordResetLink(AppUser user);

    public void sendPasswordChanged(AppUser user);
}
