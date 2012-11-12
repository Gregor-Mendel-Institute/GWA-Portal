package com.gmi.nordborglab.browser.server.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginPageFilter implements Filter
{
   public void init(FilterConfig filterConfig) throws ServletException
   {

   }

   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,   FilterChain filterChain) throws IOException, ServletException
   {
       HttpServletRequest request = (HttpServletRequest) servletRequest;
       HttpServletResponse response = (HttpServletResponse) servletResponse;

       if(request.getUserPrincipal() != null){ //If user is already authenticated
           response.sendRedirect("");// or, forward using RequestDispatcher
       } else{
           filterChain.doFilter(servletRequest, servletResponse);
       }
   }

   public void destroy()
   {

   }
}
