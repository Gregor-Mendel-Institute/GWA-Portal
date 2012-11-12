package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AppUser.class)
public class AppUser_ {

	public static volatile SingularAttribute<AppUser, String> lastname;
	public static volatile SingularAttribute<AppUser, String> firstname;
	
}
