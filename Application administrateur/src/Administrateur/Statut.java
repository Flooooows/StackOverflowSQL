package Administrateur;

public enum Statut {
	NORMAL("Normal"), AVANCE("Avancé"), MASTER("Master");
	
	private String nom;
	
	private Statut(String nom) {
		this.nom = nom;
	}
	
	public String getNom() {
		return nom;
	}
}
