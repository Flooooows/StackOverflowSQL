package Administrateur;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Scanner;

public class Administrateur {

	private static Scanner scan = new Scanner(System.in);
	private Db db;
	
	private PreparedStatement selectionnerUtilisateur, desactiverUtilisateur, ameliorerUtilisateur, selectionnerQuestions, selectionnerReponses, ajouterTag;
	
	public Administrateur() {
		db = new Db();
		try {
			selectionnerUtilisateur = db.getConnexion().prepareStatement("SELECT id_utilisateur FROM projet.utilisateurs WHERE nom_utilisateur = ?");
			desactiverUtilisateur = db.getConnexion().prepareStatement("SELECT projet.desactiverUtilisateur(?)");
			ameliorerUtilisateur = db.getConnexion().prepareStatement("SELECT projet.augmenterStatut(?, ?::projet.statut)");
			selectionnerQuestions = db.getConnexion().prepareStatement("SELECT * FROM projet.vue_questions WHERE \"ID auteur question\" = ? AND \"Date de publication\" BETWEEN ? AND ?");
			selectionnerReponses = db.getConnexion().prepareStatement("SELECT * FROM projet.vue_reponses WHERE \"Auteur\" = ? AND \"Date de publication\" BETWEEN ? AND ?");
			ajouterTag = db.getConnexion().prepareStatement("SELECT projet.insererTag(?)");
		} catch (SQLException e) {
			System.out.println("Une erreur s'est produite lors des prepareStatement");
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		Administrateur admin = new Administrateur();
		admin.menu();
	}
	
	public void menu() {
		int choix = 0;
		do {
			System.out.println("------------ MENU ------------");
			System.out.println("Que voulez-vous faire ?");
			System.out.println("1. Désactiver un compte utilisateur");
			System.out.println("2. Améliorer le statut d'un utilisateur");
			System.out.println("3. Consulteur l'historique d'un utilisateur");
			System.out.println("4. Ajouter un tag");
			System.out.println("5. Quitter l'application");
			choix = scan.nextInt();
		} while(choix < 1 || choix > 5);
		
		switch(choix) {
			case 1 :
				scan.nextLine();
				desactiverCompteUtilisateur();
				menu();
				break;
			case 2 :
				scan.nextLine();
				ameliorerStatutUtilisateur();
				menu();
				break;
			case 3 :
				scan.nextLine();
				consulterHistoriqueUtilisateur();
				menu();
				break;
			case 4 :
				scan.nextLine();
				ajouterTag();
				menu();
				break;
			case 5 :
				System.exit(0);
				break;
			default:

		}
	}
	
	private int selectionnerIdUtilisateur(String nomUtilisateur) {
		int idUtilisateur = 0;
		
		try {
			selectionnerUtilisateur.setString(1, nomUtilisateur);
			ResultSet rS = selectionnerUtilisateur.executeQuery();
			rS.next();
			idUtilisateur = rS.getInt(1);
		} catch (SQLException e) {
			System.out.println("Ce nom d'utilisateur n'existe pas !");
			return -1;
		}
		
		return idUtilisateur;
	}
	
	public void desactiverCompteUtilisateur() {
		System.out.println("Quel est le nom de l'utilisateur que vous voulez désactiver ?");
		String nomUtilisateur = scan.nextLine();
		
		int idUtilisateur = selectionnerIdUtilisateur(nomUtilisateur);
		
		if(idUtilisateur != -1) {
			try {
				desactiverUtilisateur.setInt(1, idUtilisateur);
				desactiverUtilisateur.executeQuery();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				return;
			}
			System.out.println("L'utilisateur a été désactivé !");
		}
	}
	
	public void ameliorerStatutUtilisateur() {
		System.out.println("Quel est le nom de l'utilisateur que vous voulez améliorer ?");
		String nomUtilisateur = scan.nextLine();
		
		int idUtilisateur = selectionnerIdUtilisateur(nomUtilisateur);
		
		if(idUtilisateur != -1) {
			int choix = 0;
			Statut statutUtilisateur = null;
			do {
				System.out.println("Quel statut voulez vous lui donner ?");
				System.out.println("1. Normal");
				System.out.println("2. Avancé");
				System.out.println("3. Master");
				choix = scan.nextInt();
			} while(choix < 1 || choix > 3);
			
			switch(choix) {
				case 1 :
					statutUtilisateur = Statut.NORMAL;
					break;
				case 2 :
					statutUtilisateur = Statut.AVANCE;
					break;
				case 3 :
					statutUtilisateur = Statut.MASTER;
					break;
				default :
					
			}
			
			try {
				ameliorerUtilisateur.setInt(1, idUtilisateur);
				ameliorerUtilisateur.setString(2, statutUtilisateur.getNom());
				ameliorerUtilisateur.executeQuery();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				return;
			}
			System.out.println("L'utilisateur a été amélioré !");
		}
		
	}
	
	public void consulterHistoriqueUtilisateur() {
		System.out.println("Quel est le nom de l'utilisateur que vous voulez consulter ?");
		String nomUtilisateur = scan.nextLine();
		
		int idUtilisateur = selectionnerIdUtilisateur(nomUtilisateur);
		
		if(idUtilisateur != -1) {
			System.out.println("Quelle est la date du début de la recherche (JJ/MM/AAAA) ? ");
			String dateDebut = scan.nextLine();
			System.out.println("Quelle est l'heure du début de la recherche (HH:MM:SS) ? ");
			String heureDebut = scan.nextLine();
			String[] partiesDebutDate = dateDebut.split("/");
			String[] partiesDebutHeure = heureDebut.split(":"); 
			Timestamp timestampDebut = Timestamp.valueOf(LocalDateTime.of(Integer.parseInt(partiesDebutDate[2]), Integer.parseInt(partiesDebutDate[1]), Integer.parseInt(partiesDebutDate[0]),
																		  Integer.parseInt(partiesDebutHeure[0]), Integer.parseInt(partiesDebutHeure[1]), Integer.parseInt(partiesDebutHeure[2])));
			
			System.out.println("Quelle est la date de la fin de la recherche (JJ/MM/AAAA) ? ");
			String dateFin = scan.nextLine();
			System.out.println("Quelle est l'heure de la fin de la recherche (HH:MM:SS) ? ");
			String heureFin = scan.nextLine();
			String[] partiesFinDate = dateFin.split("/");
			String[] partiesFinHeure = heureFin.split(":"); 
			Timestamp timestampFin = Timestamp.valueOf(LocalDateTime.of(Integer.parseInt(partiesFinDate[2]), Integer.parseInt(partiesFinDate[1]), Integer.parseInt(partiesFinDate[0]),
																		Integer.parseInt(partiesFinHeure[0]), Integer.parseInt(partiesFinHeure[1]), Integer.parseInt(partiesFinHeure[2])));
			try {
				selectionnerQuestions.setInt(1, idUtilisateur);
				selectionnerQuestions.setTimestamp(2, timestampDebut);
				selectionnerQuestions.setTimestamp(3, timestampFin);
				ResultSet resultatsQuestions = selectionnerQuestions.executeQuery();
				if(!resultatsQuestions.isBeforeFirst()) {
					System.out.println("Pas de questions");
				} else {
					System.out.println("Question(s) : ");
					printResultSet(resultatsQuestions);
				}
				
				selectionnerReponses.setInt(1, idUtilisateur);
				selectionnerReponses.setTimestamp(2, timestampDebut);
				selectionnerReponses.setTimestamp(3, timestampFin);
				ResultSet resultatsReponses = selectionnerReponses.executeQuery();
				if(!resultatsReponses.isBeforeFirst()) {
					System.out.println("Pas de réponses");
				} else {
					System.out.println("Réponse(s) : ");
					printResultSet(resultatsReponses);
				}
				
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void ajouterTag() {
		System.out.println("Quel est le tag à ajouter ?");
		String tag = scan.nextLine();
		
		try {
			ajouterTag.setString(1, tag);
			ajouterTag.executeQuery();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Le tag a bien été ajouté !");
	}
	
	public void printResultSet(ResultSet rs) throws SQLException {
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int columnsNumber = rsmd.getColumnCount();
	    while (rs.next()) {
	        for (int i = 1; i <= columnsNumber; i++) {
	            if (i > 1) System.out.print(" | ");
	            System.out.print(rsmd.getColumnName(i) + " : ");
	            if(rs.getString(i) == null)
	            	System.out.print("Non existant");
	            else
	            	System.out.print(rs.getString(i));
	        }
	        System.out.println("");
	    }
	}
	
	
	
	

}
