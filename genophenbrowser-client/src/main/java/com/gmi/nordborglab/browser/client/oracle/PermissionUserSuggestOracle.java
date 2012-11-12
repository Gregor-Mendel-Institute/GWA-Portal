package com.gmi.nordborglab.browser.client.oracle;

import com.gmi.nordborglab.browser.client.ui.CustomMultiWordSuggestOracle;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.PermissionPrincipalProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchPermissionUserRoleProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class PermissionUserSuggestOracle extends CustomMultiWordSuggestOracle{

	protected final CustomRequestFactory rf;
	protected SearchPermissionUserRoleProxy searchResult; 
	protected int previousQueryLength = 0;
	protected String startChar = "";
	
	@Inject
	public PermissionUserSuggestOracle(CustomRequestFactory rf) 
	{
		super();
		this.rf = rf;
	}
	
	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		
		
		if (searchResult == null || !request.getQuery().toLowerCase().startsWith(startChar)) 
		{
			startChar = request.getQuery().toLowerCase(); 
			rf.permissionRequest().searchUserAndRoles(request.getQuery()).fire(new Receiver<SearchPermissionUserRoleProxy>() {

				@Override
				public void onSuccess(SearchPermissionUserRoleProxy response) {
					searchResult = response;
					clear();
					for (PermissionPrincipalProxy principal:searchResult.getPrincipals()) {
						add(new MultiWordSuggestion(principal.getId(), principal.getName(),principal));
					}
					PermissionUserSuggestOracle.super.requestSuggestions(request, callback);
				}
			});
		}
		else
		{
			super.requestSuggestions(request, callback);
		}
	}

}
