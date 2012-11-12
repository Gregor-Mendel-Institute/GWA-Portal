package com.gmi.nordborglab.browser.server.domain.pages;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;

public class StudyPage extends PageImpl<Study>{
	

	public StudyPage() {
		super(new ArrayList<Study>(),new PageRequest(0, 1),0);
	}
	
	public StudyPage(List<Study> content, Pageable pageable, long total) {
		super(content,pageable,total);
	}

}
