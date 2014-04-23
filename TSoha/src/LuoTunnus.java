import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LuoTunnus extends HttpServlet {

   final String dbDriver="org.postgresql.Driver";
   final String dbServer ="jdbc:postgresql://localhost:10388/tsoha";
   final String dbUser= "avtanska";        // replace with your db user account
   final String dbPassword ="postgres"; // replace with your password

  public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    ServletOutputStream out;
    res.setContentType("text/html");
    out= res.getOutputStream();

    String tunnus = req.getParameter("tunnus");
    String salasana = req.getParameter("salasana");
    String varmistus = req.getParameter("varmistus");
    String sukunimi = req.getParameter("sukunimi");
    String etunimi = req.getParameter("etunimi");
    String sposti = req.getParameter("sposti");
    String puh = req.getParameter("puh");

    out.println("<html><head><title>Database query from DB (tsoha)</title>" +
                "<link rel='stylesheet' style='text/css' " +
                "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/" +
                "perus.css'></head><body>");

    Connection con=null;
    con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
    if (con==null) {
       out.println("</body></html>");
       return;
    }

    String insertHenkilö =
        "INSERT INTO henkilö VALUES('" + tunnus + "','" + sukunimi +
         "','" + etunimi + "','" + sposti + "','" + puh + "')";

    String insertSalasana =
        "INSERT INTO salasana VALUES('" + tunnus + "','" + salasana + "')";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();

      /*
       * Tarkistetaan pakollisten kenttien tiedot
       */

      if (tunnus.length() == 0) {
        out.println("<p class='virhe'>Anna tunnus.</p>");
      }
      else if (tunnus.length() > 8) {
        out.println("<p class='virhe'>Tunnus voi olla korkeintaan " +
                    " <b>8</b> merkkiä pitkä.</p>");
      }
      else {
        if (etunimi.length() == 0 || sukunimi.length() == 0 ||
            salasana.length() == 0 || varmistus.length() == 0) {
          out.println("<p><b>Täytä pakolliset kentät.</b></p>");
        }
        else if (etunimi.length() > 30) {
          out.println("<span class='virhe'>Etunimi liian pitkä</span>");
        } 
        else if (sukunimi.length() > 50) {
          out.println("<span class='virhe'>Sukunimi liian pitkä</span>");
        }
        else if (sposti.length() > 60) { 
          out.println("<span class='virhe'>Sähköposti liian pitkä</span>");    
        }   
        else if (puh.length() > 20) {    
          out.println("<span class='virhe'>Puhelinnumero liian pitkä</span>");
        }   
        else {

          /*
           * Tarkistetaan onko haluttu tunnus jo olemassa
           */

          rs = stmt.executeQuery("SELECT tunnus FROM henkilö WHERE tunnus='" +
                                 tunnus + "'");

          if (rs.next()) {
            out.println("<p><b>Tunnus jo olemassa</b></p>");
          }
          else if (!salasana.equals(varmistus)) {
            out.println("<p><b>Antamasi salasanan varmistus ei täsmää</b></p>");
          }
          else {
            
            /*
             * Kaikki kunnossa, luodaan henkilö ja salasana
             */
            
            int count = stmt.executeUpdate(insertHenkilö);
            count = stmt.executeUpdate(insertSalasana);
            out.println("<h3>Tunnuksesi luonti onnistui</h3>" +
                        "<p>Voit kirjautua sisään tunnuksellasi.</p>");
          }
        }
      }
      while(rs.next()) {
           out.println(rs.getString("tunnus")); 

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

    out.println("</body></html>");
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