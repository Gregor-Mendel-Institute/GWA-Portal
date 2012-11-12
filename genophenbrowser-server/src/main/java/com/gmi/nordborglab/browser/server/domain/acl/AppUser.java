package com.gmi.nordborglab.browser.server.domain.acl;


import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name="users", schema="acl")
public class AppUser {

	@Id
	private String username;
	private String password;
	@Column(unique=true)
	private String email;
	private String firstname;
	private String lastname;
	private boolean enabled = true;
	private boolean openidUser;
	
	@OneToMany(cascade=CascadeType.ALL,mappedBy="user")
	private List<Authority> authorities;
	
	public AppUser() {}
	
	public AppUser(String username) {
		this.username = username;
	}
	
	public List<Authority> getAuthorities() {
		return authorities;
	}
	
	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
		for (Authority authority: this.authorities) {
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

}
