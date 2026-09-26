package main.java.tacos;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import main.java.utils.scanController;

public class FrontControllerServlet extends HttpServlet {

   public void init() throws ServletException {
    try {
        new scanController().scanControllers("main.java.controllers");
    } catch (Exception e) {
        throw new ServletException(e);
      }  
   }

   public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String URL = req.getPathInfo();
    res.getWriter().println(URL);
     //raha affichena anaty jsp 
    //req.setAttribute("URL", URL);
   //req.getRequestDispatcher("/url.jsp").forward(req, res);
   }

   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
      processRequest(req, res);
   } 

   public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
      processRequest(req, res);
   } 

}
