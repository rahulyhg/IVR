import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;


/**
 *
 * @author Administrator
 */
public class tatcriya_dbinteraction extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */

	static DataSource ds;
	String pageName;

	public void init() {
		try {
			Context initCtx, envCtx;
			initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup("java:comp/env");

			// Look up our data source
			ds = (DataSource) envCtx.lookup("jdbc/indicom_manchala");
		} catch (Exception e) {
				pageName = getServletConfig().getServletName();
				System.out.println("Servlet Name iss : "+pageName+ " Error in datasource block  " +e.toString());
				pageName = null;
		}

	}

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse res) throws ServletException, IOException {

		if (request.getProtocol().equals ("HTTP/1.1")){
						    res.setHeader ("Cache-Control", "no-cache");}

		res.setContentType("application/ecmascript");


		String PROCEDURE = (String) request.getParameter("PROCEDURE");
		String Inparameter_string = (String) request.getParameter("INTOKEN");
		String Outparameter_string = (String) request.getParameter("OUTTOKEN");
		int Inparameter = Integer.parseInt(Inparameter_string);
		int Outparameter = Integer.parseInt(Outparameter_string);
		//System.out.println("PROCEDURE:"+PROCEDURE);
		//System.out.println("Inparameter:"+Inparameter);
		//System.out.println("Outparameter:"+Outparameter);
		String[] IN;
		IN = new String[Inparameter];
		String[] OUT;
		OUT = new String[Outparameter];
		String CALL = PROCEDURE+"(";
		for(int i=0;i<Inparameter;i++)
		{
			IN[i] = (String) request.getParameter("INPARAM["+i+"]");
			//System.out.println("IN["+i+"]:"+IN[i]);
			CALL = CALL+"?";
			if(i<Inparameter-1)
				CALL = CALL+",";
		}
		if(Outparameter!=0)
		{
			CALL = CALL+",?";
		}
		CALL = CALL+")";
		//System.out.println("CALL procedure:"+CALL);
		res.setContentType("text/html;charset=UTF-8");
		Connection conat = null;
		CallableStatement ccstmt = null;
		PrintWriter out = res.getWriter();
		String value = null;
		String usr_status1=null,lang1=null;
		try {

			conat = ds.getConnection();

			if (conat != null) {

				ccstmt = conat.prepareCall("{call "+CALL+"}");
				for(int i=0;i<Inparameter;i++)
				{
					int j=i+1;
					ccstmt.setString(j, IN[i]);
					//System.out.println("ccstmt.setString("+j+","+IN[i]+");");
				}

				if(Outparameter!=0)
				{
					Outparameter = Inparameter+1;
					ccstmt.registerOutParameter(Outparameter, java.sql.Types.VARCHAR);
					//System.out.println("ccstmt.setString("+Outparameter+", java.sql.Types.VARCHAR);");
					Outparameter = 1;
				}
				ccstmt.execute();
				if(Outparameter!=0)
				{
					value = ccstmt.getString(Inparameter+1);
					String temp[]=value.split("#");
					String out_string = "out_string.length=" + temp.length;
					int counter = 0;
					while (counter<temp.length) {
						out_string = out_string + ";" + "out_string" + "[" + counter
								+ "]" + "=" + "'" + temp[counter].trim() + "'";
						counter++;
					}
					out.println(out_string);
				}
			}
		} catch (Exception e) {
						pageName = getServletConfig().getServletName();
						System.out.println(" calling procedure iss :  "+CALL+"Servlet Name iss : "+pageName+ " Error in calling procedure block  " +e.toString());
						pageName = null;
		} finally {
			out.close();
			out=null;
			value=null;
			lang1=null;
			if (ccstmt != null) {
				try {
					ccstmt.close();
				} catch (SQLException e) {
					;
				}
				ccstmt = null;
			}
			if (conat != null) {
				try {
					conat.close();
				} catch (SQLException e) {
					;
				}
				conat = null;
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on
	// the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Short description";
	}
	// </editor-fold>
}