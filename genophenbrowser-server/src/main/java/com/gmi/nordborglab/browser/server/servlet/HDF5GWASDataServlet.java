package com.gmi.nordborglab.browser.server.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import com.gmi.nordborglab.browser.server.data.GWASDataForClient;
import com.gmi.nordborglab.browser.server.service.impl.GWASDataTableGenerator;
import com.google.gson.Gson;
import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.base.DataSourceException;

@Component
public class HDF5GWASDataServlet implements HttpRequestHandler {
	
	@Resource
	protected GWASDataTableGenerator gwasDataTableGenerator;
	

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String chr = request.getParameter("chr");
		if (chr != null) {
			DataSourceHelper.executeDataSourceServletFlow(request, response, gwasDataTableGenerator,false);
		}
		else {
			Long studyId = Long.parseLong(request.getParameter("studyId"));
			try {
				GWASDataForClient data = gwasDataTableGenerator.getGWASDataForClient(studyId);
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
				throw new ServletException();
			}
		}
		
	}

}
