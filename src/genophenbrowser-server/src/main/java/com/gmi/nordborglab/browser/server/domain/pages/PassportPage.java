package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PassportPage extends PageImpl<Passport> {

    public PassportPage(List<Passport> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public List<Passport> getContents() {
        return getContent();
    }

}
