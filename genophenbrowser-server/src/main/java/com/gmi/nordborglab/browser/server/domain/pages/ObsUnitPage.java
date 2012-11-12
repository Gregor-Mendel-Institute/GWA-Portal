package com.gmi.nordborglab.browser.server.domain.pages;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;

public class ObsUnitPage extends PageImpl<ObsUnit>{
	

	public ObsUnitPage() {
		super(new ArrayList<ObsUnit>(),new PageRequest(0, 1),0);
	}
	
	public ObsUnitPage(List<ObsUnit> content, Pageable pageable, long total) {
		super(content,pageable,total);
	}

}
