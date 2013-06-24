package com.gmi.nordborglab.browser.server.domain.acl;


import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;

import java.util.Date;
import java.util.List;
import javax.persistence.*;


@Entity
@Table(name = "users", schema = "acl")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "acl.users_id_seq")
public class AppUser extends BaseEntity {


    private String username;
    private String password;
    @Column(unique = true)
    private String email;
    private String firstname;
    private String lastname;
    private boolean enabled = true;
    private boolean openidUser;

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
}
