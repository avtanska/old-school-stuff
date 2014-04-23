function tarkista() {

 var palauta = true;
 var puuttuu = ""; 

 if (document.varaus.aihe.value == '') {
   palauta = false;
   puuttuu = " - aihe\n";
 }

 if (document.varaus.paiva.value == 'tyhjä' ||  
     document.varaus.kuukausi.value == 'tyhjä' ||
     document.varaus.vuosi.value == 'tyhjä') { 
   palauta = false;
   puuttuu += " - päivämäärä\n";
 }

 if (document.varaus.alkuaika.value == 'tyhjä') {
   palauta = false;
   puuttuu += " - alkuaika\n";
 }

 if (document.varaus.kesto.value == 'tyhjä') {
   palauta = false;
   puuttuu += " - kesto\n";
 }

 if (document.varaus.nakyvyys.value == 'tyhjä') {
   palauta = false;
   puuttuu += " - näkyvyys\n";
 }

 if (document.varaus.henkilo.value == 'tyhjä' &&
     document.varaus.ryhma.value == 'tyhjä') {
   palauta = false;
   puuttuu += " - ryhmä tai henkilö\n";
 }
 
 if (document.varaus.henkilo.value != 'tyhjä' &&
     document.varaus.ryhma.value != 'tyhjä') {
   palauta = false;
   puuttuu += " - ryhmä TAI henkilö\n";
 }


 if( palauta == false ) {
   alert("Pakollisia tietoja puuttuu:\n" + puuttuu);
   return false;
 } else {
   return true;
 }
 
 return palauta;
}