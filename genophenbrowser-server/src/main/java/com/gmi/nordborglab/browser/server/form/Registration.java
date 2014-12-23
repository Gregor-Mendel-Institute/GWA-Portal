package com.gmi.nordborglab.browser.server.form;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.AssertTrue;


public class Registration {

    @Length(min = 4, max = 20, message = "Password must be between 4 and 20 characters long.")
    private String password;

    @NotEmpty
    private String confirmPassword;

    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    private String firstname;
    @NotEmpty
    private String lastname;

    private boolean isSocialAccount = false;


    @AssertTrue(message = "Password does not match")
    public boolean isPasswordValid() {
        if (password == null) {
            return false;
        } else {
            return password.equals(confirmPassword);
        }
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean isSocialAccount) {
        this.isSocialAccount = isSocialAccount;
    }
}
