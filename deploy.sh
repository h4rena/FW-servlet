#!/bin/bash

# Définition des variables
APP_NAME="FW"
SRC_DIR="/home/hatsugoki/Web-dyn/Framework-servlet/src/main/java"
WEB_DIR="/home/hatsugoki/Web-dyn/Framework-servlet/src/main/webapp"
BUILD_DIR="build"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/home/hatsugoki/tomcat/apache-tomcat-10.0.16/webapps"
SERVLET_API_JAR="/home/hatsugoki/tomcat/apache-tomcat-10.0.16/lib/servlet-api.jar"

# Nettoyage et création du répertoire temporaire
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/WEB-INF/classes
mkdir -p $BUILD_DIR/WEB-INF/lib
cp -f "$LIB_DIR"/*.jar $BUILD_DIR/WEB-INF/lib/ 2>/dev/null || true

# Compilation des fichiers Java avec le JAR des Servlets
find $SRC_DIR -name "*.java" > sources.txt
javac -parameters -cp "$SERVLET_API_JAR:$LIB_DIR/*" -d $BUILD_DIR/WEB-INF/classes @sources.txt
# rm sources.txt

# Copier les fichiers web (web.xml, JSP, etc.)
cp -r $WEB_DIR/* $BUILD_DIR/

# Générer le fichier .war dans le dossier build
cd $BUILD_DIR || exit
jar -cvf $APP_NAME.war *
cd ..

# Déploiement dans Tomcat
cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/

echo ""

echo "Déploiement terminé. Redémarrez Tomcat si nécessaire."

echo ""
