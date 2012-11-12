package com.gmi.nordborglab.browser.server.domain.pages;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

public class TraitUomPage extends PageImpl<TraitUom> {

	public TraitUomPage(List<TraitUom> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

}
