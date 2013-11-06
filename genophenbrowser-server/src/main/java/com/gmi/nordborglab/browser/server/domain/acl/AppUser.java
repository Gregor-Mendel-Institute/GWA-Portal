package com.gmi.nordborglab.browser.server.domain.acl;


import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.validation.PasswordsEqual;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.Email;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "users", schema = "acl")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "acl.users_id_seq", allocationSize = 1)
@PasswordsEqual
public class AppUser extends BaseEntity {


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
    private boolean openidUser;

    private Date registrationdate;

    @Enumerated(EnumType.ORDINAL)
    private AppUserProxy.AVATAR_SOURCE avatarSource;

    @Transient
    private String newPassword;
    @Transient
    private String newPasswordConfirm;

    @Transient
    private String gravatarHash;

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
        for (Authority authority : this.authorities) {
            authority.setUser(this);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        setGravatarHash(DigestUtils.md5Hex(getEmail().toLowerCase().trim()));
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
}
