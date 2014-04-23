import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LuoRyhma extends HttpServlet {

  final String dbDriver="org.postgresql.Driver";
  final String dbServer ="jdbc:postgresql://localhost:10388/tsoha";
  final String dbUser= "avtanska";        // replace with your db user account
  final String dbPassword ="postgres"; // replace with your password


  public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    ServletOutputStream out;
    res.setContentType("text/html");
    out= res.getOutputStream();

    String submit = req.getParameter("submit");
    HttpSession session = req.getSession(false);
    String sessionTunnus = (String)session.getValue("sessionTunnus");
    String[] j‰senet = req.getParameterValues("jasenet");
    String ryhm‰ = req.getParameter("ryhma");
    String virhe;


    /*
     * Tarkistetaan onko tarvittavat tiedot t‰ytetty
     */

    if (submit != null && (j‰senet == null || ryhm‰.length() == 0)) {
      virhe = "Anna ryhm‰n nimi ja valitse j‰senet!";
    }
    else {
      virhe = null;
    }

    out.println("<html><head><title></title>" +
                "<link rel='stylesheet' type='text/css' " +
                "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/" +
                "perus.css' /><body>");

    /*
     * Jos tullaan sivulle 'Luo ryhm‰'-linkin kautta tai l‰hetet‰‰n
     * virheellinen lomake.
     */

    if (submit == null || (submit != null && virhe != null)) {

      out.println("<form method='post' action='http://db.cs.helsinki.fi/" +
                  "s/avtanska/LuoRyhma'>");

      if (virhe != null) {
        out.println("<p><b>" + virhe + "</b></p>");
      }

      out.println("<h3>Ryhm‰n luonti:</h3>");
      out.println("<table><tr><td>Ryhm‰n nimi:</td><td><input name='ryhma'" +
                  " type='text'></td></tr>");
      out.println("<tr><td valign='top'>Valitse j‰senet:</td><td><select " +
                  "multiple name='jasenet'>");

      Connection con=null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
      if (con==null) {
         out.println("</body></html>");
         return;
      }

      /*
       * Haetaan tietokannasta kaikki henkilˆt, admin-k‰ytt‰j‰‰
       * ei tulosteta listaan
       */

      Statement stmt = null;
      ResultSet rs = null;
      try {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT * FROM henkilˆ ORDER BY tunnus");
        while(rs.next()) {
          if(!(rs.getString("tunnus")).equals("admin")) {
             out.println("<option value='" + rs.getString("tunnus") + "'>" +
             rs.getString("etunimi") + " " + rs.getString("sukunimi"));
          }
        }

      } catch (SQLException ee) {
          out.println("Tietokantavirhe "+ee.getMessage());
      } finally {
          try {
             if (rs!=null) rs.close();
             if (stmt!=null) stmt.close();
             con.close();
          } catch(SQLException e) {
             out.println("An SQL Exception was thrown.");
          }
      }

      out.println("</td></tr><tr><td>&nbsp;</td></tr><tr><td colspan='2'>" +
                  "<input type='submit' name='submit' value='Luo ryhm‰'>" +
                  "<input type='reset' name='reset' value='Tyhjenn‰'>" +
                  "</td></tr></table></form></body></html>");

    } // loppu if submit null
 
 
    /*
     * Jos lomake l‰hetetty
     */
 
    else {

      Connection con = null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
 
      if (con==null) {
        out.println("</body></html>");
        return;
      }
 
      Statement stmt = null;
      ResultSet rs = null;
      try {
         stmt = con.createStatement();
 
 
         /*
          * Tarkistetaan, onko halutunnimist‰ ryhm‰‰ jo olemassa
          */
 
         rs = stmt.executeQuery("SELECT nimi FROM ryhm‰ WHERE nimi='" +
                                ryhm‰ + "'");
 
         if (rs.next()) {
           out.println("<span class='virhe'>Ryhm‰ on jo olemassa</b>");
         }
         else {
 
           /*
            * Luodaan ryhm‰ ja j‰senyydet halutuille j‰senille
            */
 
           String insertRyhm‰ =
               "INSERT INTO ryhm‰ VALUES ('" + ryhm‰ +
               "','" + sessionTunnus + "')";
 
           int count = stmt.executeUpdate(insertRyhm‰);
 
           String insertJ‰senyys = "";
 
           for (int i = 0; i < j‰senet.length; i++) {
             insertJ‰senyys =
                "INSERT INTO j‰senyys VALUES ('" + j‰senet[i] +
                "','" + ryhm‰ + "')";
 
             count = stmt.executeUpdate(insertJ‰senyys);
           }
 
           out.println("<h3>Ryhm‰n luonti onnistui.</h3>" +
                       "<p>Ryhm‰‰ ei viel‰ n‰y omien ryhmiesi " +
                       "listassa.<br />P‰‰set k‰siksi ryhm‰n tietoihin, " +
                       "kun kirjoittaudut<br />sis‰‰n seuraavan kerran.</p>");
 
         }
 
      } catch (SQLException ee) {
          out.println("Tietokantavirhe "+ee.getMessage());
      } finally {
         try {
           if (rs!=null) rs.close();
           if (stmt!=null) stmt.close();
           con.close();
         } catch(SQLException e) {
            out.println("An SQL Exception was thrown.");
         }
      }
   
    } // loppu else jos lomake l‰hetetty

  }



  private Connection createDbConnection(
    String dbDriver, String dbServer, String dbUser, String dbPassword,
    ServletOutputStream out) throws IOException {

   // establish a database connection
    try{
        Class.forName(dbDriver);               // load driver
    } catch (ClassNotFoundException e) {
          out.println("Couldn't find driver "+dbDriver);
          return null;
    }
    Connection con=null;
    try {
       con = DriverManager.getConnection(dbServer,dbUser,dbPassword);
    } catch (SQLException se) {
          out.println("Couldn\'t get connection to "+dbServer+
                      " for "+ dbUser+"<br>");
          out.println(se.getMessage());
    }
    return con;
  }

}