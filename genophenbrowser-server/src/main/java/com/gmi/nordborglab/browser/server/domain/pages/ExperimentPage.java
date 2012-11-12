package com.gmi.nordborglab.browser.server.domain.pages;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;

public class ExperimentPage extends PageImpl<Experiment>{
	

	public ExperimentPage() {
		super(new ArrayList<Experiment>(),new PageRequest(0, 1),0);
	}
	
	public ExperimentPage(List<Experiment> content, Pageable pageable, long total) {
		super(content,pageable,total);
	}

}
