package com.gmi.nordborglab.browser.server.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;


public class Registration {

    @NotEmpty
    @Size(min = 4, max = 20)
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
    

    @AssertTrue(message="Password does not match")
    public boolean isPasswordValid(){
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
}
