#!/bin/bash

# Définition des variables
APP_NAME="FW"
SRC_DIR="/home/hatsugoki/Web-dyn/Framework-servlet/src/main/java"
WEB_DIR="/home/hatsugoki/Web-dyn/Framework-servlet/src/main/webapp"
BUILD_DIR="build"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/home/hatsugoki/tomcat/apache-tomcat-10.0.16/webapps"
SERVLET_API_JAR="/home/hatsugoki/tomcat/apache-tomcat-10.0.16/lib/servlet-api.jar"

JACKSON_CP="$WEB_DIR/WEB-INF/lib/jackson-databind-2.21.2.jar:$WEB_DIR/WEB-INF/lib/jackson-core-2.21.2.jar:$WEB_DIR/WEB-INF/lib/jackson-annotations-2.21.jar"

# Nettoyage et création du répertoire temporaire
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/WEB-INF/classes

# Compilation des fichiers Java avec le JAR des Servlets
find $SRC_DIR -name "*.java" > sources.txt
javac -cp "$SERVLET_API_JAR:$JACKSON_CP" -d $BUILD_DIR/WEB-INF/classes @sources.txt || exit 1
# rm sources.txt

# Copier les fichiers web (web.xml, JSP, etc.)
cp -r $WEB_DIR/* $BUILD_DIR/

# Générer le fichier .war dans le dossier build
cd $BUILD_DIR || exit 1
jar -cvf $APP_NAME.war *
cd ..

# Déploiement dans Tomcat
cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/

echo ""

echo "Déploiement terminé. Redémarrez Tomcat si nécessaire."

echo ""
