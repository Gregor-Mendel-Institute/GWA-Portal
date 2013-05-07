package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/3/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationPage extends PageImpl<Publication> {

    public PublicationPage(List<Publication> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

}