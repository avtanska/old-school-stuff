BEGIN;

CREATE TABLE henkil� ( 
	tunnus varchar(8) NOT NULL, 
	sukunimi varchar(50) NOT NULL, 
	etunimi varchar(30) NOT NULL, 
	sposti varchar(60), 
	puh varchar(20), 
	PRIMARY KEY (tunnus) 
);  
 
CREATE TABLE n�kyvyys ( 
	nid numeric(1) NOT NULL, 
	kuvaus varchar(100) NOT NULL, 
	PRIMARY KEY (nid) 
); 
 
CREATE TABLE salasana ( 
	tunnus varchar(8) NOT NULL, 
	salasana varchar(20) NOT NULL, 
	PRIMARY KEY (tunnus), 
	FOREIGN KEY (tunnus) REFERENCES henkil� 
); 
 
CREATE TABLE ryhm� ( 
	nimi varchar(40) NOT NULL, 
	omistaja varchar(8) NOT NULL, 
	PRIMARY KEY (nimi), 
	FOREIGN KEY (omistaja) REFERENCES henkil� 
); 
 
CREATE TABLE j�senyys ( 
	tunnus varchar(8) NOT NULL, 
	ryhm� varchar(40) NOT NULL, 
	PRIMARY KEY (tunnus, ryhm�), 
	FOREIGN KEY (tunnus) REFERENCES henkil�, 
	FOREIGN KEY (ryhm�) REFERENCES ryhm� 
); 
 
CREATE TABLE varaus ( 
	vid SERIAL, 
	tunnus varchar(8), 
	pvm Date NOT NULL, 
	alkuaika Time NOT NULL, 
	kesto numeric(3,1) NOT NULL, 
	aihe varchar(100) NOT NULL, 
        varaaja varchar(8) NOT NULL,
	n�kyvyys numeric(1) NOT NULL, 
	ryhm� varchar(40), 
	PRIMARY KEY (vid), 
	FOREIGN KEY (tunnus) REFERENCES henkil�, 
	FOREIGN KEY (n�kyvyys) REFERENCES n�kyvyys, 
	FOREIGN KEY (ryhm�) REFERENCES ryhm� 
);

COMMIT;
