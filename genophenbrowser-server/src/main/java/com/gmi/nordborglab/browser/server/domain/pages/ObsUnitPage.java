package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class ObsUnitPage extends PageImpl<ObsUnit> {


    public ObsUnitPage() {
        super(new ArrayList<ObsUnit>(), new PageRequest(0, 1), 0);
    }

    public ObsUnitPage(List<ObsUnit> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public List<ObsUnit> getContents() {
        return getContent();
    }

}
