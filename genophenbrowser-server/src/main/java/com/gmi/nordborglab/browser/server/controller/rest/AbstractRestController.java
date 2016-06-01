package com.gmi.nordborglab.browser.server.controller.rest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by uemit.seren on 6/1/16.
 */
public abstract class AbstractRestController {

    @Value("${HDF5_SERVER}")
    private String HDF5_SERVER;

    protected void passThroughRequest(HttpServletResponse response, String postUrl) throws IOException {
        passThroughRequest(response, postUrl, null);
    }

    protected void passThroughRequest(HttpServletResponse response, String postUrl, String body) throws IOException {
        if (response.getContentType() == null) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        }
        URL url = new URL("http://" + HDF5_SERVER + postUrl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        if (body == null) {
            httpCon.setRequestMethod("GET");
        } else {
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Content-Type", "application/json");
            IOUtils.write(body, httpCon.getOutputStream());
        }
        int responseCode = httpCon.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to complete request");
        }
        IOUtils.copy(httpCon.getInputStream(), response.getOutputStream());
    }

    protected void donwloadPlotRequest(HttpServletResponse response, String type, Long id, String chr, Integer minMac, String format) throws IOException {
        String filename = String.format("%s_%s_mac%s_chr%s.%s", type, id, minMac, chr == null ? "all" : chr, format);
        String url = String.format("/plotting/%s/%s?macs=%s&format=%s", type, id, minMac, format);
        if (chr != null) {
            url += "&chr=" + chr;
        }
        String mimeType;
        switch (format) {
            case "png":
                mimeType = "image/png";
                break;
            case "pdf":
                mimeType = "application/pdf";
                break;
            default:
                throw new IOException(String.format("Unknown format %s", format));
        }
        response.setContentType(mimeType);
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        passThroughRequest(response, url);
    }
}
