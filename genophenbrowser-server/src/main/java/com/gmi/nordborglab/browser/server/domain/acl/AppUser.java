package com.gmi.nordborglab.browser.server.domain.acl;


import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.es.ESDocument;
import com.gmi.nordborglab.browser.server.validation.PasswordsEqual;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "users", schema = "acl")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "acl.users_id_seq", allocationSize = 1)
@PasswordsEqual
public class AppUser extends BaseEntity implements ESDocument {

    public static final String ES_TYPE = "user";

    @Column(unique = true)
    @Email
    @NotEmpty
    private String username;

    private String password;
    @Column(unique = true)
    @Email
    private String email;
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    private boolean enabled = true;
    private boolean openidUser = false;
    private String openididentifier;

    private Date registrationdate = new Date();

    @Enumerated(EnumType.ORDINAL)
    private AppUserProxy.AVATAR_SOURCE avatarSource = AppUserProxy.AVATAR_SOURCE.GRAVATAR;

    @Transient
    private String newPassword;
    @Transient
    private String newPasswordConfirm;

    @Transient
    private String gravatarHash = null;

    @Transient
    private int numberOfStudies = 0;

    @Transient
    private int numberOfPhenotypes = 0;

    @Transient
    private int numberOfAnalysis = 0;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Authority> authorities;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "appUser")
    private List<UserNotification> userNotifications;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "appUser")
    private List<StudyJob> studyJobs;
    private Date notificationCheckDate;
    private String passwordResetToken;
    private Date passwordResetExpiration;

    public AppUser() {
    }

    public AppUser(String username) {
        this.username = username;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
        if (authorities != null) {
            for (Authority authority : this.authorities) {
                authority.setUser(this);
            }
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        gravatarHash = null;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Transient
    public String getName() {
        StringBuilder fullNameBldr = new StringBuilder();

        if (firstname != null) {
            fullNameBldr.append(firstname);
        }

        if (lastname != null) {
            fullNameBldr.append(" ").append(lastname);
        }
        return fullNameBldr.toString();
    }


    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean openidUser() {
        return openidUser;
    }

    public void setOpenidUser(boolean openidUser) {
        this.openidUser = openidUser;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<UserNotification> getUserNotifications() {
        return userNotifications;
    }

    public void addUserNotification(UserNotification userNotification) {
        userNotifications.add(userNotification);
    }

    public List<StudyJob> getStudyJobs() {
        return studyJobs;
    }

    public void addStudyJob(StudyJob studyJob) {
        studyJobs.add(studyJob);
    }

    public Date getNotificationCheckDate() {
        return notificationCheckDate;
    }

    public Date isNotificationCheckDate() {
        return notificationCheckDate;
    }

    public void setNotificationCheckDate(Date notificationCheckDate) {
        this.notificationCheckDate = notificationCheckDate;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }

    public AppUserProxy.AVATAR_SOURCE getAvatarSource() {
        return avatarSource;
    }

    public void setAvatarSource(AppUserProxy.AVATAR_SOURCE avatarSource) {
        this.avatarSource = avatarSource;
    }

    public String getGravatarHash() {
        if (gravatarHash == null && getEmail() != null) {
            gravatarHash = DigestUtils.md5Hex(getEmail().toLowerCase().trim());
        }
        return gravatarHash;
    }

    public void setGravatarHash(String gravatarHash) {
        this.gravatarHash = gravatarHash;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getNumberOfStudies() {
        return numberOfStudies;
    }

    public void setNumberOfStudies(int numberOfStudies) {
        this.numberOfStudies = numberOfStudies;
    }

    public int getNumberOfPhenotypes() {
        return numberOfPhenotypes;
    }

    public void setNumberOfPhenotypes(int numberOfPhenotypes) {
        this.numberOfPhenotypes = numberOfPhenotypes;
    }

    public int getNumberOfAnalysis() {
        return numberOfAnalysis;
    }

    public void setNumberOfAnalysis(int numberOfAnalysis) {
        this.numberOfAnalysis = numberOfAnalysis;
    }

    public Date getRegistrationdate() {
        return registrationdate;
    }

    public void setRegistrationdate(Date registrationdate) {
        this.registrationdate = registrationdate;
    }

    @Transient
    public String getAvatarHash() {
        String url = getGravatarHash() + "?d=identicon";
        if (getAvatarSource() == AppUserProxy.AVATAR_SOURCE.IDENTICON) {
            url = url + "?&=1";
        }
        return url;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
        if (passwordResetToken == null || passwordResetToken.isEmpty()) {
            passwordResetExpiration = null;
        } else {
            passwordResetExpiration = DateTime.now().plusDays(1).toDate();
        }
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public Date getPasswordResetExpiration() {
        return passwordResetExpiration;
    }

    public String getOpenididentifier() {
        return openididentifier;
    }

    public void setOpenididentifier(String openididentifier) {
        this.openididentifier = openididentifier;
    }

    @Override
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("enabled", isEnabled())
                .field("username", getUsername())
                .field("registrationdate", getRegistrationdate())
                .field("email", getEmail())
                .field("lastname", getLastname())
                .field("firstname", getFirstname());
        if (getAuthorities() != null && getAuthorities().size() > 0) {
            builder.startArray("authorities");
            for (Authority authority : getAuthorities()) {
                builder.value(authority.getAuthority());
            }
            builder.endArray();
        }
        return builder;
    }

    @Override
    public String getEsType() {
        return ES_TYPE;
    }

    @Override
    public String getEsId() {
        if (getId() != null)
            return getId().toString();
        return null;
    }

    @Override
    public String getRouting() {
        return null;
    }

    @Override
    public String getParentId() {
        return null;
    }
}
