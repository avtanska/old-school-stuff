BEGIN;

CREATE TABLE henkilö ( 
	tunnus varchar(8) NOT NULL, 
	sukunimi varchar(50) NOT NULL, 
	etunimi varchar(30) NOT NULL, 
	sposti varchar(60), 
	puh varchar(20), 
	PRIMARY KEY (tunnus) 
);  
 
CREATE TABLE näkyvyys ( 
	nid numeric(1) NOT NULL, 
	kuvaus varchar(100) NOT NULL, 
	PRIMARY KEY (nid) 
); 
 
CREATE TABLE salasana ( 
	tunnus varchar(8) NOT NULL, 
	salasana varchar(20) NOT NULL, 
	PRIMARY KEY (tunnus), 
	FOREIGN KEY (tunnus) REFERENCES henkilö 
); 
 
CREATE TABLE ryhmä ( 
	nimi varchar(40) NOT NULL, 
	omistaja varchar(8) NOT NULL, 
	PRIMARY KEY (nimi), 
	FOREIGN KEY (omistaja) REFERENCES henkilö 
); 
 
CREATE TABLE jäsenyys ( 
	tunnus varchar(8) NOT NULL, 
	ryhmä varchar(40) NOT NULL, 
	PRIMARY KEY (tunnus, ryhmä), 
	FOREIGN KEY (tunnus) REFERENCES henkilö, 
	FOREIGN KEY (ryhmä) REFERENCES ryhmä 
); 
 
CREATE TABLE varaus ( 
	vid SERIAL, 
	tunnus varchar(8), 
	pvm Date NOT NULL, 
	alkuaika Time NOT NULL, 
	kesto numeric(3,1) NOT NULL, 
	aihe varchar(100) NOT NULL, 
        varaaja varchar(8) NOT NULL,
	näkyvyys numeric(1) NOT NULL, 
	ryhmä varchar(40), 
	PRIMARY KEY (vid), 
	FOREIGN KEY (tunnus) REFERENCES henkilö, 
	FOREIGN KEY (näkyvyys) REFERENCES näkyvyys, 
	FOREIGN KEY (ryhmä) REFERENCES ryhmä 
);

COMMIT;
