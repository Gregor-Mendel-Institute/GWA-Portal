package com.gmi.nordborglab.browser.server.servlet;

import com.gmi.nordborglab.browser.server.data.GWASDataForClient;
import com.gmi.nordborglab.browser.server.service.impl.GWASDataTableGenerator;
import com.google.gson.Gson;
import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.base.DataSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

@Component
public class HDF5GWASDataServlet implements HttpRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(HDF5GWASDataServlet.class);

    @Resource
    protected GWASDataTableGenerator gwasDataTableGenerator;


    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {
        String chr = request.getParameter("chr");
        if (chr != null) {
            DataSourceHelper.executeDataSourceServletFlow(request, response, gwasDataTableGenerator, false);
        } else {
            try {
                GWASDataForClient data = gwasDataTableGenerator.getGWASDataForClient(request);
                String json = new Gson().toJson(data);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Writer writer = null;

                try {
                    writer = response.getWriter();
                    writer.write(json);
                } finally {
                }
            } catch (DataSourceException e) {
                logger.error("DatasourceException", e);
                throw new ServletException();
            } catch (AccessDeniedException e) {
                logger.error("AccessDeniedException", e);
                throw e;
            }
        }

    }

}
