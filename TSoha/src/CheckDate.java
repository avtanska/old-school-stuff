final class CheckDate {

  private CheckDate() { }
 
 
   /*
    * Pieni apumetodi p‰iv‰m‰‰rien formatointiin.
    * J‰‰ muutama if-lause pois koodista
    */

   public static String c(int num) {
     if (num < 10) 
       return "0" + num;
     else
       return "" + num;
   }


   /*
    * Apumetodi, joka raa'asti korjaa liian suuren p‰ivien
    * numeron kuukaudessa alasp‰in sopivaan lukuun. 
    * Myˆs karkausvuodet huomioitu.
    */ 

   public static String checkDate(String date) {
     int dd = Integer.parseInt(date.substring(0,2));
     int mm = Integer.parseInt(date.substring(3,5));
     int yyyy = Integer.parseInt(date.substring(6,10));
     String retval = "";

     if (dd == 31 && 
         (mm == 4 || mm == 6 || mm == 9 || mm == 11)) {
       dd = dd - 1;
       retval = dd + "." + c(mm) + "." + yyyy;
     }
     else if (dd > 28 && mm == 2) {
       if ((yyyy%4 == 0 && yyyy%100 != 0) || (yyyy%400 == 0))
         dd = 29;
       else
         dd = 28;      

       retval = dd + "." + c(mm) + "." + yyyy;
     }
     else {
       retval = c(dd) + "." + c(mm) + "." + yyyy;
     }

     return retval;
   }
}