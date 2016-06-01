package com.gmi.nordborglab.browser.server.controller.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by uemit.seren on 6/1/16.
 */
@RestController
@RequestMapping("/api/gwasviewer")
public class GWASViewerRestController extends AbstractRestController {

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/plots")
    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.GWASResult','READ')")
    public void downloadPlot(HttpServletResponse response, HttpServletRequest request,
                             @PathVariable Long id,
                             @RequestParam(value = "chr", required = false) String chr,
                             @RequestParam(value = "mac", required = false, defaultValue = "15") Integer minMac,
                             @RequestParam(value = "format", required = false, defaultValue = "png") String format) throws IOException {
        donwloadPlotRequest(response, "viewer", id, chr, minMac, format);

    }

}
