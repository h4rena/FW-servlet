package main.java.model;

public class MapFile {

    private String nom;
    private String extension;
    private byte[] contenu;
    private String chemin; 

    public MapFile() {
    }

    public MapFile(String nom, String extension, byte[] contenu) {
        this.nom = nom;
        this.extension = extension;
        this.contenu = contenu;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }

    public long getTaille() {
        return contenu == null ? 0 : contenu.length;
    }

    public String getChemin() { 
        return chemin; 
    }

    public void setChemin(String chemin) { 
        this.chemin = chemin; 
    }

    public String toString() {
        return "MapFile{nom=" + nom + ", extension=" + extension
             + ", taille=" + getTaille() + ", chemin=" + getChemin() + "}";
    }
}
