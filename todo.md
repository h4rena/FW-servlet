# FRAMEWORK PROJECT

## S0
servlet
FrontControllerServelet
function:
doGet |
      |------->ProcessRequest(..)
doPost|	       outprint(URL)
	      
## Pre requis S1 
Pre requis : 
Spring MVC + WebApi

Annotation : miasa eo @
Classe , Methode, Attributs 

Annotation : Classe spéciale 

Mila tenenina ko oe inn avy ny anaran ny variables azo apetrakaa anaty annotation 

Atao: mi creer test ana annotation de apesaina ao am main

## S1

Creer la classe annotation Contoller
(Package ze tina)

Dans test
Creer une classe TestContoller, annote 

But : les Classe qu'on a déclaré Contoller : on peut connaître oe ces classes la sont des controllers  dans le framework 


Mila manana code tsika oe :
- Soit au démarrage anle application Web no executé le izy ( mapiasa Listener afahana miaino événements) (na mbola tsy misy application miditra)
- na au premier appel anle FrontServlet (mapiasa init())
- Le code : But : Mila fantatra oe inn avy ny controller rehetra
Comment : Ao anatinle FrontServlet : 
Asina Attribut 1 : List<Contoller> 

1- (init()) Refa atsoina le FrontServlet de alainy daoly ny Controller reetra ao anaty Classpath de atao anatinle List<Contoller>
- Refa mitafy ny Controller rehetra izy de Alainy daoly ny Classes reetra ao amle Classpath , fa ny mety omena ny  packages misy anle controllers 

Yap time:
Ny framework samy manana ny mode de fonctionnement any 


Aveo printena dool fotsn


Ao anatinla web.xml asina variables mitahiry ny package misy ny package misy ny controllers reetra , aveo iny variables vakianle framework de aveo jereny daoly ze callses ao anatiny puis jerena daoly ndray oe iza avy ny classes annotés ao 

Mila fonction mandray Le annotation ho veriviena,package,de ao au niveau inn le annotation , de retournena daoly le classes
Mety mila fonction 2 : 1 miparcourir ny classes reetra amle package, de ny ray omena List<> Classes de sy oe inn le annotation ho tediavina de retourneny daoly le classes annotés 
 
de io fonction io atao anaty Classe Utilitaire 



Ra ampiasa Listener ndray :

Am WebService , anaky 3 no tena important: 
-Servlet
-
-Listener

Listener miaino events fotsn ny ataony :

Mila declarena ao anaty web.xml ko le listener ( otran le servlet iany ) 

De micreer Classe 1 déclenché au moment du listener

## S2

But : Refa misy lien de fantatra oe iza ny controller mifanaraka amn iny 

A afficher : /emp/list : dans Contoller (EmpContoller) de List() no methode associe aminy 

1- Afficherna daoly fotns lo le liens valides reetra 
2- Aveo amzay refa mi taper lien de tokony tenenina raha efa defini ve le lien sa tsy de affichena le Controller sy method miaraka aminy
Raha tsy ita le lien : Apseo daoly oe inn ny liens supportés miaraka amle Cotroller sy Méthode associe 

Fa raha ohatra oe /emp/list ohatra ny supporté, ka /emp/new no nataoko de tsy apseo daoly ny liens reetra fa /emp/new iany no apseo miaraka am infos any


Comment :

- Creer une annotation de type methode 

Aty am Test : 
Misy Contoller ex : EmpContoller annote Contoller

De misy 
Fonction Liste() annote  ohtr : UrlMapping("/emp/list") 

tohinyS2:
mampiasa map pour url unique 

S3
      

S3 bis
