package Utilisateur;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
	
	private Connection connexion = null;
	
	public Db() {
		// Vérification du driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch(ClassNotFoundException e) {
			System.out.println("Driver PostregreSQL manquant");
			System.exit(1);
		}
		
		// Connection à la base de données
		String hote = "172.24.2.6";
		String port = "5432";
		String nom = "dbndelann15";
		String utilisateur = "ftimmer16";
		String mdp = "A2#FazE";
	
		String url = "jdbc:postgresql://" + hote + ":" + port + "/" + nom;
		
		try {
			connexion = DriverManager.getConnection(url, utilisateur, mdp);
		} catch (SQLException e) {
			System.out.println("Impossible de joindre le serveur !");
			System.exit(1);
		}
	}
	
	public Connection getConnexion() {
		return connexion;
	}

}
