

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Administrator
 */
public class lovesol_Read extends HttpServlet {

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
	//AMIT


	static HashMap<String,String> hashMap= new HashMap<String,String>(16,.5f);


	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (request.getProtocol().equals ("HTTP/1.1")){
				    response.setHeader ("Cache-Control", "no-cache");}

		response.setContentType("application/ecmascript");

		PrintWriter out = response.getWriter();
		String lovesol=null;
		String TOKEN=null;
		response.setContentType("text/html;charset=UTF-8");
		String ConfigPath = request.getParameter("ConfigPath");
		TOKEN = request.getParameter("TOKEN");
		String stringSearch = request.getParameter("stringSearch");
		if(TOKEN!=null && TOKEN.equalsIgnoreCase("FREE"))
			hashMap.clear();
		String key =ConfigPath;

        if(TOKEN == null)
        	TOKEN="T20";

	    if(hashMap.get(key)!=null)
		{
			out.println(hashMap.get(key));
			return;
		}
		//String PreviewToPlay1 = "PreviewToPlay1.value='HINDI_TOP20'";
		 if(TOKEN == null)
        	TOKEN="T20";

		int testCounter=0;
		try {
			if (ConfigPath != null) {
				File file_txt1 = new File("/var/lib/apache-tomcat-6.0.29/webapps/hungama/RecordFiles/BOLBABYBOL/2012/LoveGuru/LoveGuru/" +ConfigPath);
				if (file_txt1.exists()) {
					ArrayList data = new ArrayList();
					BufferedReader in = new BufferedReader(new FileReader(
							file_txt1));
					String temp = null;
					while ((temp = in.readLine()) != null) {
						int indexfound = temp.indexOf(stringSearch);

						if (indexfound > -1) {
							//data.add(temp.trim().toLowerCase());
							data.add(temp.trim());

		                }
						temp = null;
						testCounter++;

					}
                      if(TOKEN.equalsIgnoreCase("superhit"))
                       Collections.shuffle(data);

					ListIterator ite = data.listIterator();
					lovesol = "lovesol.length=" + data.size();
					int counter = 0;
					while (ite.hasNext()) {
						lovesol = lovesol + ";" + "lovesol" + "[" + counter
								+ "]" + "=" + "'" + ite.next() + "'";
						counter++;
					}

					data.clear();
					in.close();
				} else {
					lovesol = "lovesol.length=0;lovesol[0]='0'";
				}

			}
			hashMap.put(key,lovesol);
			out.println(lovesol);

		} catch (Exception e) {
			out.println("error is" + e.toString());
		}finally{
			ConfigPath=null;
			lovesol=null;
			key=null;
			TOKEN=null;

		}
	}

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