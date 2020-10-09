package Utilisateur;
import java.sql.*;
import java.util.Scanner;

import static Utilisateur.BCrypt.*;
public class Utilisateur {
	private static Db database;
	private static Scanner scan = new Scanner(System.in); 
	
	private PreparedStatement insererUtilisateur,insererQuestion,insererQuestionTags,insererReponse,insererVote;
	private PreparedStatement modifierQuestion,modifierReponse,cloturerQuestion;
	private PreparedStatement selectionnerTags,selectionnerUtilisateur,selectionnerReponsesQuestion,selectionnerReponsesUtilisateur,selectionnerUneReponse;
	private PreparedStatement selectionnerQuestions,selectionnerUneQuestion,selectionnerQuestionsUtilisateur,selectionnerQuestionsTags;
	
	public Utilisateur() {
		database = new Db();
		try {
			//Insertions
			insererUtilisateur = database.getConnexion().prepareStatement("SELECT projet.insererUtilisateur(?,?,?)");
			insererQuestion = database.getConnexion().prepareStatement("SELECT projet.insererQuestion(?,?,?)");
			insererQuestionTags = database.getConnexion().prepareStatement("SELECT projet.insererQuestionTags(?,?,?)");
			insererReponse = database.getConnexion().prepareStatement("SELECT projet.insererReponse(?,?,?)");
			insererVote = database.getConnexion().prepareStatement("SELECT projet.insererVote(?,?,?,?)");
			//Modifications
			modifierQuestion = database.getConnexion().prepareStatement("SELECT projet.modifierQuestion(?,?,?,?)");
			modifierReponse = database.getConnexion().prepareStatement("SELECT projet.modifierReponse(?,?,?,?)");
			cloturerQuestion = database.getConnexion().prepareStatement("SELECT projet.cloturerQuestion(?,?)");
			//Selections
			selectionnerTags = database.getConnexion().prepareStatement("SELECT num_tag AS \"Tag numero\",intitule_tag AS \"Intitule\" FROM projet.tags");
			selectionnerUneQuestion = database.getConnexion().prepareStatement("SELECT * FROM projet.vue_questions  WHERE \"Numéro question\" = ? ");
			selectionnerQuestions = database.getConnexion().prepareStatement("SELECT * FROM projet.vue_questions");
			selectionnerQuestionsUtilisateur = database.getConnexion().prepareStatement("SELECT * FROM projet.vue_questions WHERE \"ID auteur question\" = ? ");
			selectionnerQuestionsTags =  database.getConnexion().prepareStatement("SELECT * FROM projet.vue_questions_tags WHERE \"Numéro tag\" = ? ");
			selectionnerUtilisateur = database.getConnexion().prepareStatement("SELECT id_utilisateur,nom_utilisateur,mail,mdp,est_desactive FROM projet.utilisateurs WHERE nom_utilisateur = ? ");
			selectionnerReponsesUtilisateur = database.getConnexion().prepareStatement("SELECT * FROM projet.vue_reponses WHERE \"Auteur\" = ?");
			selectionnerReponsesQuestion = database.getConnexion().prepareStatement("SELECT * FROM projet.vue_reponses WHERE  \"Numéro question\" = ?");
			selectionnerUneReponse = database.getConnexion().prepareStatement("SELECT * FROM projet.vue_reponses WHERE \"Numéro réponse\" = ? ");

			
		} catch (SQLException se) {
			System.out.println("Erreur des prepare statement");
			se.printStackTrace();
			System.exit(1);
		} 
	}
	
	public static void main(String[] args) {
		Utilisateur user = new Utilisateur();
		int idUtilisateur = -1;
		
		System.out.println("Bonjour ! Veux tu t'inscrire ou te connecter ? ");
		System.out.println("1. S'inscrire ");
		System.out.println("2. Se connecter");
		int choix = scan.nextInt();
		switch(choix) {
			case 1: 
				scan.nextLine();
				if(user.ajouterUtilisateur()) {
					System.out.println("Tu es inscrit ! Tu peux maintenant te connecter : ");
					idUtilisateur = user.connectionUtilisateur();
				}
				break;
			case 2:
				scan.nextLine();
				idUtilisateur = user.connectionUtilisateur();
				break;
			default:
				System.out.println("Fin du programme");
				break;
		}

		choix = 1;
		int question;
		while(idUtilisateur != -1 && (choix >= 1 && choix <= 4) ) {
			System.out.println("1. Ajouter une question ");
			System.out.println("2. Visualiser ses questions et ses réponses");
			System.out.println("3. Visualiser toutes les questions");
			System.out.println("4. Visualiser les questions liées à un tag");
			System.out.println("Autre. Quitter le programme");

			choix = scan.nextInt();
			switch(choix){
			case 1:
				scan.nextLine();
				user.ajouterQuestion(idUtilisateur);
				break;
			case 2:
				scan.nextLine();
				question = user.afficherSesQuestionsReponses(idUtilisateur);
				if(question != -1)
					user.gererQuestion(idUtilisateur,question);
				break;
			case 3:
				scan.nextLine();
				question = user.afficherToutesLesQuestions();
				if(question != -1)
					user.gererQuestion(idUtilisateur,question);
				break;
			case 4:
				scan.nextLine();
				question = user.afficherQuestionsLieesAUnTag();
				if(question != -1)
					user.gererQuestion(idUtilisateur,question);
				break;
			default:
				System.out.println("Fin du programme");
				break;
			}
		}
		
	}

	private boolean ajouterUtilisateur() {
		System.out.println("Entre ton nom :");
		String nom = scan.nextLine();
		System.out.println("Entre ton mail : ");
		String mail = scan.nextLine();
		System.out.println("Choisis un mot de passe : ");
		String mdp = scan.nextLine();
		mdp = hashpw(mdp, gensalt());
	
		try {
			insererUtilisateur.setString(1, nom);
			insererUtilisateur.setString(2, mail);
			insererUtilisateur.setString(3, mdp);
			insererUtilisateur.executeQuery();
		} catch (SQLException e) {
			System.out.println("Erreur lors de l'inscription");
			return false;
		}
		return true;
	}

	private int connectionUtilisateur(){
		int idUtilisateur = -1;
		ResultSet resultat = null;
		System.out.println("Entre ton nom d'utilisateur : ");
		String nom = scan.nextLine();
		System.out.println("Entre ton mdp : ");
		String mdp = scan.nextLine();

		try {
			selectionnerUtilisateur.setString(1, nom);
			resultat = selectionnerUtilisateur.executeQuery();
			resultat.next();
			String mdpCrypte = resultat.getString(4);	
			if(checkpw(mdp, mdpCrypte)) {
				idUtilisateur = resultat.getInt(1);
			}else{
				System.out.println("Mot de passe incorrect");
				return -1;
			}
			Boolean estDesactive = resultat.getBoolean(5);
			if(estDesactive) {
				System.out.println("Ton compte est désactivé, tu ne peux plus te connecter !");
				return -1;
			}
			System.out.println("Bienvenue "+resultat.getString(2)+" !\n");
		} catch (SQLException e) {
			System.out.println("Utilisateur inexistant");
			return -1;
		}

		return idUtilisateur;
	}
	
	private boolean ajouterQuestion(int idUtilisateur) {
		System.out.println("Titre de la question : ");
		String titre = scan.nextLine();
		System.out.println("Corps de la question : ");
		String corps = scan.nextLine();
		try {
			insererQuestion.setString(1, titre);
			insererQuestion.setString(2, corps);
			insererQuestion.setInt(3, idUtilisateur);
			insererQuestion.executeQuery();
		} catch (SQLException e) {
			System.out.println("Erreur lors de l'ajout de la question");
			return false;
		}
		System.out.println("Question bien ajoutée !\n");
		return true;
	}

	private void ajouterTag(int idUtilisateur,int question){
		ResultSet tags = null;

		try {
			tags = selectionnerTags.executeQuery();
		} catch (SQLException e) {
			System.out.println("Impossible de selectionner les tags");
		}
		try {
			if (!tags.isBeforeFirst() ) {    
			    System.out.println("Pas de tags disponible"); 
			    return;
			}
		} catch (SQLException e) {
			System.out.println("Erreur d'accès à la base de données");
			return;
		} 
		try {
			System.out.println("TAGS DISPONIBLES : ");
			printResultSet(tags);
			System.out.println();
		} catch (SQLException e) {
			System.out.println("Impossible d'afficher les tags");
		}
		
		System.out.println("Quel tag voulez-vous ajouter ? (Entrez son numéro)");
		int choixTag = scan.nextInt();

		try {
			insererQuestionTags.setInt(1, idUtilisateur);
			insererQuestionTags.setInt(2, question);
			insererQuestionTags.setInt(3, choixTag);
			insererQuestionTags.executeQuery();
		
		} catch (SQLException e) {	
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Tag bien ajouté à la question" + question);

	}

	private void ajouterReponse(int idUtilisateur,int question) {
		System.out.println("Entre ta réponse : " );
		String contenu = scan.nextLine();
		try {
			insererReponse.setInt(1, question);
			insererReponse.setInt(2, idUtilisateur);
			insererReponse.setString(3, contenu);
			insererReponse.executeQuery();
		} catch (SQLException e) {
			System.out.println("Erreur lors de l'insertion");
			return;
		}
		System.out.println("Reponse insérée\n");
		
	}
	
	private int afficherSesQuestionsReponses(int idUtilisateur){
		ResultSet questions = null;
		ResultSet reponses = null;
		try {

			selectionnerQuestionsUtilisateur.setInt(1, idUtilisateur);
			questions = selectionnerQuestionsUtilisateur.executeQuery();
		} catch (SQLException e){
			e.printStackTrace();
			System.out.println("Impossible de sélectionner les questions");
		}
		try {

			selectionnerReponsesUtilisateur.setInt(1, idUtilisateur);
			reponses = selectionnerReponsesUtilisateur.executeQuery();
		} catch (SQLException e){
			System.out.println("Impossible de sélectionner les reponses");
		}
		try {
			if (!questions.isBeforeFirst()) {    
				System.out.println("Pas de questions posées\n"); 
				return -1;
			}else{
				System.out.println("QUESTIONS : ");
				printResultSet(questions);
				System.out.println();
			}
			if (!reponses.isBeforeFirst()) {    
				System.out.println("Pas de reponses\n"); 
				
			}else{
				System.out.println("REPONSES : ");
				printResultSet(reponses);
				System.out.println();
			}
		} catch (SQLException e) {
			System.out.println("Impossible d'afficher les questions ");
		}
		System.out.println("Voulez-vous choisir une question en particulier ? (O/N) ");
		char choix = scan.next().charAt(0);
		if(choix == 'O') {
			System.out.println("Entrez le numéro de la question ");
			int question = scan.nextInt();
			return question;
		}
		return -1;
	}
	
	private int afficherToutesLesQuestions(){
		ResultSet questions = null;
		try {
			questions = selectionnerQuestions.executeQuery();
		} catch (SQLException e){
			System.out.println("Impossible de sélectionner les questions");
		}
		try {
			if (!questions.isBeforeFirst() ) {    
			    System.out.println("Pas de questions posées\n");   
			    return -1;
			}else{
				System.out.println("QUESTIONS : ");
				printResultSet(questions);
				System.out.println();
			}
		} catch (SQLException e) {
			System.out.println("Impossible d'afficher les questions");
		} 
		System.out.println("Voulez-vous choisir une question en particulier ? (O/N) ");
		char choix = scan.next().charAt(0);
		if(choix == 'O') {
			System.out.println("Entrez le numéro de la question ");
			int question = scan.nextInt();
			return question;
		}
		return -1;
	}
	
	private int afficherQuestionsLieesAUnTag(){
		ResultSet questions = null;
		ResultSet tags = null;
		
		try {
			tags = selectionnerTags.executeQuery();
		} catch (SQLException e) {
			System.out.println("Impossible de sélectionner les tags");
		}
		try {
			if (!tags.isBeforeFirst() ) {    
			    System.out.println("Pas de tags disponible\n"); 
			    return -1;
			}
			printResultSet(tags);
		} catch (SQLException e) {
			System.out.println("Erreur d'accès à la base de données");
			return -1;
		} 
		System.out.println("Entre le numéro d'un tag : ");
		int tag = scan.nextInt();
		try {
			selectionnerQuestionsTags.setInt(1, tag);
			questions = selectionnerQuestionsTags.executeQuery();
		} catch (SQLException e) {
			System.out.println("Impossible de sélectionner des questions liees à ce tag ");
		}
		try {
			if (!questions.isBeforeFirst() ) {    
			    System.out.println("Pas de questions posées avec ce tag\n"); 
			    return -1;
			} 
			printResultSet(questions);
		} catch (SQLException e) {
			System.out.println("Erreur d'accès à la base de données");
			return -1;
		} 
		System.out.println("Voulez-vous choisir une question en particulier ? (O/N) ");
		char choix = scan.next().charAt(0);
		if(choix == 'O') {
			System.out.println("Entrez le numéro de la question ");
			int question = scan.nextInt();
			return question;
		}
		return -1;
	}
	
	private void modifierQuestion(int idUtilisateur,int question){
		
		try {
			System.out.println("Voici la question : ");
			selectionnerUneQuestion.setInt(1, question);
			printResultSet(selectionnerUneQuestion.executeQuery());
			System.out.println();
		} catch (SQLException e) {
			System.out.println("Impossible de selectionner la question");
			return;
		}
		try{
			scan.nextLine();
			System.out.println("Entrez un nouveau titre");
			String titre = scan.nextLine();
			System.out.println("Entrez un nouveau corps");
			String corps = scan.nextLine();
			if(titre.equals(""))
				modifierQuestion.setString(1, null);
			else
				modifierQuestion.setString(1, titre);
			if(corps.equals(""))
				modifierQuestion.setString(2, null);
			else
				modifierQuestion.setString(2, corps);
			modifierQuestion.setInt(3, idUtilisateur);
			modifierQuestion.setInt(4, question);
			modifierQuestion.executeQuery();
			System.out.println("Question modifiée !\n");
		}catch(SQLException e){
			System.out.println("Impossible de modifier la question");
			return;
		}
	}

	
	private void modifierReponse(int idUtilisateur, int question) {
		ResultSet reponses = null;
		try {
			selectionnerReponsesQuestion.setInt(1, question);
			reponses = selectionnerReponsesQuestion.executeQuery();
			printResultSet(reponses);
			System.out.println();
		} catch (SQLException e) {
			System.out.println("Impossible de récupérer les réponses liées à cette question");
			return;
		}
		System.out.println("Quelle réponse voulez-vous modifier ?");
		int choix = scan.nextInt();
		try {
			selectionnerUneReponse.setInt(1, choix);
			System.out.println("Voici la réponse à modifier : ");
			printResultSet(selectionnerUneReponse.executeQuery());
		} catch (SQLException e) {
			System.out.println("Impossible de sélectionner cette réponse");
			return;
		}

		try {
			scan.nextLine();
			System.out.println("Entrez un nouveau contenu");
			String contenu = scan.nextLine();
			modifierReponse.setInt(1, choix);
			modifierReponse.setInt(2, question);
			modifierReponse.setString(3, contenu);
			modifierReponse.setInt(4, idUtilisateur);

			modifierReponse.executeQuery();
			System.out.println("Réponse modifiée !");
		} catch (SQLException e) {
			System.out.println("Impossible de modifier la réponse");
			return;
		}
	}

	private void voterReponse(int idUtilisateur, int question) {
		ResultSet reponses = null;
		try {
			selectionnerReponsesQuestion.setInt(1, question);
			reponses = selectionnerReponsesQuestion.executeQuery();
			printResultSet(reponses);
			System.out.println();
		} catch (SQLException e) {
			System.out.println("Impossible de récupérer les réponses liées à cette question");
			return;
		}
		System.out.println("Quelle est la réponse pour laquelle vous voulez voter ?");
		int reponse = scan.nextInt();
		scan.nextLine();
		boolean voteCorrect = false;
		char vote;
		do {
			System.out.println("Voulez-vous voter positivement ou négativement (P/N) ?");
			vote = scan.nextLine().charAt(0);
			if(vote == 'P' || vote == 'N') voteCorrect = true;
		} while(!voteCorrect);

		try {
			insererVote.setInt(1, idUtilisateur);
			insererVote.setInt(2, reponse);
			insererVote.setInt(3, question);
			if(vote == 'P') {
				insererVote.setBoolean(4, true);
			} else {
				insererVote.setBoolean(4, false);
			}
			insererVote.executeQuery();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Vous avez voté !\n");
	}

	private void cloturerQuestion(int idUtilisateur,int question) {
		char reponse;
		System.out.println("Êtes-vous sûr de vouloir clôturer la question n°" + question + " ? Cette action est irréversible (O/N).");
		reponse = scan.nextLine().charAt(0);

		if(reponse == 'O') {
			try {
				cloturerQuestion.setInt(1, idUtilisateur);
				cloturerQuestion.setInt(2,question);
				cloturerQuestion.executeQuery();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				return;
			}
			System.out.println("Question cloturée !");
		}
	}

	private void gererQuestion(int idUtilisateur,int question) {
		int choix = 1;
	
		while((choix >= 1 && choix <= 5) ) {
			System.out.println("1. Ajouter une réponse");
			System.out.println("2. Voter pour une réponse");
			System.out.println("3. Editer la question ou une des réponses");
			System.out.println("4. Ajouter un tag");
			System.out.println("5. Cloturer une question");
			System.out.println("Autre. Retour au menu précédent");
			choix = scan.nextInt();
			switch(choix){
			case 1:
				scan.nextLine();
				ajouterReponse(idUtilisateur,question);
				break;
			case 2:
				voterReponse(idUtilisateur, question);
				break;
			case 3:
				System.out.println("1. Editer la question ");
				System.out.println("2. Editer une des réponses");
				int choixEdition = scan.nextInt();
				if(choixEdition == 1){
					modifierQuestion(idUtilisateur,question);
				}
				if(choixEdition == 2){
					modifierReponse(idUtilisateur,question);
				}
				break;
			case 4:
				ajouterTag(idUtilisateur,question);
				break;
			case 5: 
				scan.nextLine();
				cloturerQuestion(idUtilisateur,question);
				break;
			default:
				System.out.println("Menu précédent");
				break;
			}
		}
	}

	public void printResultSet(ResultSet rs) throws SQLException{
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
