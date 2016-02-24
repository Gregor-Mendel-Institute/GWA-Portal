package com.gmi.nordborglab.browser.shared.proxy;

import com.gmi.nordborglab.browser.shared.validation.PasswordsEqual;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;


@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.acl.AppUser")
@PasswordsEqual
public interface AppUserProxy extends ValueProxy {


    final String GRAVATAR_URL = "https://secure.gravatar.com/avatar/";

    enum AVATAR_SOURCE {
        GRAVATAR, IDENTICON, CUSTOM
    }

    Long getId();


    @NotNull
    @Size(min = 1)
    String getUsername();

    @NotNull
    @Size(min = 1)
    String getFirstname();

    void setFirstname(String firstname);

    String getNewPassword();

    void setNewPassword(String newPassword);

    String getNewPasswordConfirm();

    void setNewPasswordConfirm(String newPasswordConfirm);

    @NotNull
    @Size(min = 1)
    String getLastname();

    void setLastname(String lastname);

    String getEmail();

    void setEmail(String email);

    List<AuthorityProxy> getAuthorities();

    void setAuthorities(List<AuthorityProxy> authorities);

    void setId(Long id);

    String getGravatarHash();

    void setGravatarHash(String hash);

    AVATAR_SOURCE getAvatarSource();

    public void setAvatarSource(AVATAR_SOURCE avatarSource);

    int getNumberOfStudies();

    int getNumberOfPhenotypes();

    int getNumberOfAnalysis();

    public void setNumberOfStudies(int count);

    public void setNumberOfPhenotypes(int count);

    public void setNumberOfAnalysis(int count);

    public Date getRegistrationdate();
}
