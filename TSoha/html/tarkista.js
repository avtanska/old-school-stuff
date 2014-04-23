function tarkista() {

 var palauta = true;
 var puuttuu = ""; 

 if (document.varaus.aihe.value == '') {
   palauta = false;
   puuttuu = " - aihe\n";
 }

 if (document.varaus.paiva.value == 'tyhj�' ||  
     document.varaus.kuukausi.value == 'tyhj�' ||
     document.varaus.vuosi.value == 'tyhj�') { 
   palauta = false;
   puuttuu += " - p�iv�m��r�\n";
 }

 if (document.varaus.alkuaika.value == 'tyhj�') {
   palauta = false;
   puuttuu += " - alkuaika\n";
 }

 if (document.varaus.kesto.value == 'tyhj�') {
   palauta = false;
   puuttuu += " - kesto\n";
 }

 if (document.varaus.nakyvyys.value == 'tyhj�') {
   palauta = false;
   puuttuu += " - n�kyvyys\n";
 }

 if (document.varaus.henkilo.value == 'tyhj�' &&
     document.varaus.ryhma.value == 'tyhj�') {
   palauta = false;
   puuttuu += " - ryhm� tai henkil�\n";
 }
 
 if (document.varaus.henkilo.value != 'tyhj�' &&
     document.varaus.ryhma.value != 'tyhj�') {
   palauta = false;
   puuttuu += " - ryhm� TAI henkil�\n";
 }


 if( palauta == false ) {
   alert("Pakollisia tietoja puuttuu:\n" + puuttuu);
   return false;
 } else {
   return true;
 }
 
 return palauta;
}