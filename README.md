# Locate Parking

Mobilna android aplikacija iz predmeta Mobilni sistemi i servisi. Na temu parking servisa, pod nazivom 


<h1 align="center">Locate Parking </h1>
<p align="center">  
  <img src="https://github.com/nikolcar/BottomNavigation/blob/master/app/src/main/res/mipmap-xxxhdpi/icon_white.png"/>
</p>

Aplikacija se sastoji od:
- Mobilnog i
- Serverskog dela

Sa sledećim funkcionalnostima

- Registracija na sistemu unosom osnovnih podataka (ime, prezime, nadimak, datum dođenja, email adrese, šifre).
- Logovanje na sistem email-adresom i šifrom, mogućnost promene šifre preko mail-a.

<p align="center">  
  <img src="https://github.com/nikolcar/BottomNavigation/blob/master/app/src/main/res/screenshots/registration.png"/>
  <img src="https://github.com/nikolcar/BottomNavigation/blob/master/app/src/main/res/screenshots/login.png"/>
  <img src="https://github.com/nikolcar/BottomNavigation/blob/master/app/src/main/res/screenshots/forgotenPassword.png"/>
</p>

- Prikaz trenutne lokacije korisnika na mapi, kao i prikaz parkinga.
- Pretraživanje parkinga po imenu, po radijusu u odnosu na korisnika (u metrima) i po tipu (private/public).
- Klikom na informaciju o parkingu, korisnik može dobiti i direkciju do istog. Tada se u bazi pamti kada je korisnik zatražio direkciju do određenog parkinga.
- Dodavanje parkinga, na trenutnoj lokaciji korisnika.

- Prikaz rang liste prijatelja.
- Dodavanje prijatelja preko bluetooth-a.

- Korisnik može videti svoje podatke, može promeniti šifru i sliku.
- Korisnik može da izabere da li želi da vidi svoje prijatelje na mapi, kao i druge korisnike aplikacije.
- Startovanje i stopiranje background servisa, koji šalje notifikaciju, kada je korisnik blizu nekog parkinga ili prijatelja.
- Logout.


