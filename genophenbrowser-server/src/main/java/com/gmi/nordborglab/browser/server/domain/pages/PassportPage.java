package com.gmi.nordborglab.browser.server.domain.pages;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;

public class PassportPage extends PageImpl<Passport> {
	
	public PassportPage(List<Passport> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

}
