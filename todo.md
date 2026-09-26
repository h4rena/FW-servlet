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