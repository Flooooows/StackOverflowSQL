
DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet;
CREATE TYPE projet.statut AS ENUM('Normal','Avancé','Master');

CREATE TABLE projet.utilisateurs (
	id_utilisateur SERIAL PRIMARY KEY,
	nom_utilisateur VARCHAR(100) UNIQUE NOT NULL CHECK (nom_utilisateur<>''),
	mail VARCHAR(100) NOT NULL CHECK (mail SIMILAR TO '[a-zA-Z0-9._\-]+@([a-zA-Z0-9._\-]+\.[a-z]{2,4})'),
	mdp VARCHAR (100) NOT NULL CHECK (mdp<>''),
	statut projet.statut NOT NULL DEFAULT 'Normal',
	est_desactive BOOLEAN NOT NULL DEFAULT FALSE,
	reputation INTEGER NOT NULL DEFAULT 0 CHECK (reputation >= 0),
	dernier_vote TIMESTAMP
	
);

CREATE TABLE projet.questions (
	num_question SERIAL PRIMARY KEY,
	titre VARCHAR(100) NOT NULL CHECK (titre<>''),
	corps VARCHAR(300) NOT NULL CHECK (corps<>''),
	date_publication TIMESTAMP NOT NULL,
	auteur INTEGER NOT NULL REFERENCES projet.utilisateurs(id_utilisateur),
	date_modification TIMESTAMP,
	auteur_modification INTEGER REFERENCES projet.utilisateurs(id_utilisateur),
	est_cloture BOOLEAN NOT NULL DEFAULT FALSE

);

CREATE TABLE projet.tags (
	num_tag SERIAL PRIMARY KEY,
	intitule_tag VARCHAR(50) NOT NULL UNIQUE CHECK(intitule_tag<>'')

);

CREATE TABLE projet.questions_tags (
	question INTEGER NOT NULL REFERENCES projet.questions(num_question),
	tag INTEGER NOT NULL REFERENCES projet.tags(num_tag),
	PRIMARY KEY(question,tag)
	
);

CREATE TABLE projet.reponses (
	num_reponse SERIAL,
	num_question INTEGER NOT NULL REFERENCES projet.questions(num_question),
	date_publication TIMESTAMP NOT NULL,
	auteur INTEGER NOT NULL REFERENCES projet.utilisateurs(id_utilisateur),
	score INTEGER NOT NULL DEFAULT 0, 
	contenu VARCHAR(200) NOT NULL CHECK(contenu<>''),
	PRIMARY KEY(num_reponse,num_question)
	
);

CREATE TABLE projet.votes(
	utilisateur INTEGER,
	reponse INTEGER,
	num_question INTEGER,
	est_positif BOOLEAN,
	FOREIGN KEY(utilisateur) REFERENCES projet.utilisateurs(id_utilisateur),
	FOREIGN KEY(reponse, num_question) REFERENCES projet.reponses(num_reponse,num_question),
	PRIMARY KEY(utilisateur,reponse)
	
);

-- Damas ; damas
INSERT INTO "projet"."utilisateurs" ("id_utilisateur", "nom_utilisateur", "mail", "mdp", "statut", "est_desactive", "reputation", "dernier_vote") VALUES ('1', 'Damas', 'damas@vinci.be', '$2a$10$cJCU/Zqi0lrWvfvUBDBrHuRbgwYxPG9yAje7RtEgsr1Y9V5orxqKC', 'Normal', 'f', '0', NULL);
-- Ferneeuw ; ferneeuw
INSERT INTO "projet"."utilisateurs" ("id_utilisateur", "nom_utilisateur", "mail", "mdp", "statut", "est_desactive", "reputation", "dernier_vote") VALUES ('2', 'Ferneeuw', 'ferneeuw@vinci.be', '$2a$10$FwcFQ05vy2xYoSfoH6QTsOnK6alxIi.6G9dnkQ8DosKUKYKq5SROu', 'Normal', 'f', '0', NULL);
-- Grolaux ; grolaux
INSERT INTO "projet"."utilisateurs" ("id_utilisateur", "nom_utilisateur", "mail", "mdp", "statut", "est_desactive", "reputation", "dernier_vote") VALUES ('3', 'Grolaux', 'grolaux@vinci.be', '$2a$10$KROFjO4Y93fRW4t1NkiP3u1WwtSp46iCUXNsOPu8H4eUxtLzWN9qe', 'Normal', 'f', '0', NULL);

------------------------------------------
-- FONCTION POUR AJOUTER UN UTILISATEUR 
------------------------------------------

CREATE OR REPLACE FUNCTION projet.insererUtilisateur(VARCHAR(100),VARCHAR(100),VARCHAR(100)) RETURNS void AS $$
DECLARE 
	_nom_utilisateur ALIAS FOR $1;
	_mail ALIAS FOR $2;
	_mdp ALIAS FOR $3;
BEGIN
	INSERT INTO projet.utilisateurs VALUES (DEFAULT,_nom_utilisateur,_mail,_mdp,DEFAULT,DEFAULT,DEFAULT,NULL);
END;
$$ LANGUAGE plpgsql;

------------------------------------------
-- FONCTION POUR AJOUTER UNE QUESTION 
------------------------------------------

CREATE OR REPLACE FUNCTION projet.insererQuestion(VARCHAR(100),VARCHAR(300),INTEGER) RETURNS void AS $$
DECLARE 
	_titre ALIAS FOR $1;
	_corps ALIAS FOR $2;
	_auteur ALIAS FOR $3;
BEGIN 
	INSERT INTO projet.questions VALUES(DEFAULT,_titre,_corps,NOW(),_auteur,NULL,NULL,DEFAULT);
END;
$$ LANGUAGE plpgsql;

------------------------------------------
-- FONCTION POUR AJOUTER UN TAG
------------------------------------------

CREATE OR REPLACE FUNCTION projet.insererTag(VARCHAR(50)) RETURNS void AS $$
DECLARE 
	_intitule ALIAS FOR $1;
BEGIN
	INSERT INTO projet.tags VALUES(DEFAULT,_intitule);
END;
$$ LANGUAGE plpgsql;

--------------------------------------------------
-- FONCTION POUR AJOUTER UNE LIAISON QUESTION/TAG 
--------------------------------------------------

CREATE OR REPLACE FUNCTION projet.insererQuestionTags(INTEGER,INTEGER, INTEGER) RETURNS void AS $$
DECLARE 
	_id_utilisateur ALIAS for $1;
	_num_question ALIAS for $2;
	_tag ALIAS for $3;
	_auteur_question INTEGER;
	_est_cloture BOOLEAN;
BEGIN
	SELECT auteur q FROM projet.questions q WHERE num_question = _num_question INTO _auteur_question;
	SELECT est_cloture FROM projet.questions WHERE num_question = _num_question INTO _est_cloture;
	IF(_est_cloture = true) THEN RAISE 'Question cloturée'; 
	ELSIF( _id_utilisateur = _auteur_question) THEN INSERT INTO projet.questions_tags VALUES(_num_question,_tag);
	ELSE RAISE 'Seul lauteur peut ajouter des tags à sa question';
	END IF;
END;
$$ LANGUAGE plpgsql;

------------------------------------------
-- FONCTION POUR AJOUTER UNE REPONSE
------------------------------------------

CREATE OR REPLACE FUNCTION projet.insererReponse(INTEGER, INTEGER, VARCHAR(200)) RETURNS void AS $$
DECLARE
	_num_question ALIAS FOR $1;
	_auteur ALIAS FOR $2;
	_contenu ALIAS FOR $3;
BEGIN
	INSERT INTO projet.reponses VALUES(DEFAULT, _num_question, NOW(), _auteur, DEFAULT, _contenu);
END;
$$ LANGUAGE plpgsql;

------------------------------------------
-- FONCTION POUR AJOUTER UN VOTE
------------------------------------------

CREATE OR REPLACE FUNCTION projet.insererVote(INTEGER, INTEGER, INTEGER, BOOLEAN) RETURNS void AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
	_num_reponse ALIAS FOR $2;
	_num_question ALIAS FOR $3;
	_est_positif ALIAS FOR $4;
BEGIN
	INSERT INTO projet.votes VALUES(_id_utilisateur, _num_reponse, _num_question, _est_positif);
END;
$$ LANGUAGE plpgsql;

-------------------------------------------
-- FONCTION POUR MODIFIER UNE QUESTION
-------------------------------------------

CREATE OR REPLACE FUNCTION projet.modifierQuestion(VARCHAR(100),VARCHAR(300),INTEGER,INTEGER) RETURNS void AS $$
DECLARE 
	_titre ALIAS FOR $1;
	_corps ALIAS FOR $2;
	_auteur ALIAS FOR $3;
	_num_question ALIAS FOR $4;
	_est_cloture BOOLEAN;
BEGIN
	SELECT est_cloture FROM projet.questions WHERE num_question = _num_question INTO _est_cloture;
	IF(_est_cloture = true) THEN RAISE 'Question cloturée'; 
	ELSIF( _titre IS NULL) THEN UPDATE projet.questions SET corps= _corps,date_modification= NOW(), auteur_modification= _auteur  WHERE num_question= _num_question; 
	ELSIF( _corps IS NULL) THEN UPDATE projet.questions SET titre= _titre,date_modification= NOW(), auteur_modification= _auteur  WHERE num_question= _num_question;
	ELSE UPDATE projet.questions SET titre= _titre,corps= _corps,date_modification= NOW(), auteur_modification= _auteur  WHERE num_question= _num_question;
	END IF;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------
-- FONCTION POUR MODIFIER UNE REPONSE
-------------------------------------------

CREATE OR REPLACE FUNCTION projet.modifierReponse(INTEGER, INTEGER, VARCHAR(200), INTEGER) RETURNS void AS $$
DECLARE
	_num_reponse ALIAS FOR $1;
	_num_question ALIAS FOR $2;
	_contenu ALIAS FOR $3;
	_id_utilisateur ALIAS FOR $4;
	_statut projet.statut;
	_auteur INTEGER;
	_question_cloturee BOOLEAN;
BEGIN
	-- Vérifie que le statut est Avancé ou Master pour modifier les réponses des autres utilisateurs
	-- Impossibilité d'effectuer ça dans un trigger car il n'y a pas de champ dans la table reponses pour connaître l'auteur de la modification
	SELECT statut FROM projet.utilisateurs WHERE id_utilisateur = _id_utilisateur INTO _statut;
	SELECT auteur FROM projet.reponses WHERE num_reponse = _num_reponse AND num_question = _num_question INTO _auteur;
	SELECT est_cloture FROM projet.questions WHERE num_question = _num_question INTO _question_cloturee;
	IF(_statut = 'Normal' AND _id_utilisateur != _auteur) THEN RAISE 'Statut trop bas pour modifier les reponses des autres utilisateurs';
	END IF;
	IF(_question_cloturee = true) THEN RAISE 'La question a été cloturée, impossible de modifier ses réponses';
	END IF;
	UPDATE projet.reponses SET contenu = _contenu WHERE num_question = _num_question AND num_reponse = _num_reponse;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------
-- FONCTION POUR AUGMENTER LE STATUT
-------------------------------------------
CREATE OR REPLACE FUNCTION projet.augmenterStatut(INTEGER, projet.statut) RETURNS void AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
	_statut ALIAS FOR $2;
BEGIN
	UPDATE projet.utilisateurs SET statut = _statut WHERE id_utilisateur =_id_utilisateur;
END;
$$ LANGUAGE plpgsql;


-------------------------------------------
-- FONCTION POUR CLOTURER UNE QUESTION
-------------------------------------------

CREATE OR REPLACE FUNCTION projet.cloturerQuestion(INTEGER, INTEGER) RETURNS void AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
	_num_question ALIAS FOR $2;
	_statut projet.statut;
BEGIN
	SELECT u.statut FROM projet.utilisateurs u WHERE u.id_utilisateur = _id_utilisateur INTO _statut;
	IF((_statut = 'Normal' OR _statut = 'Avancé')) THEN RAISE 'Votre statut est trop bas pour cloturer une question';
	END IF;
	UPDATE projet.questions SET est_cloture = true WHERE num_question = _num_question;
END;
$$ LANGUAGE plpgsql;

--------------------------------------------
-- FONCTION POUR DESACTIVER UN UTILISATEUR
--------------------------------------------

CREATE OR REPLACE FUNCTION projet.desactiverUtilisateur(INTEGER) RETURNS void AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
BEGIN
	UPDATE projet.utilisateurs SET est_desactive = true WHERE id_utilisateur = _id_utilisateur;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------
-- TRIGGER POUR VERIFIER LE NOMBRE DE TAGS
-------------------------------------------

CREATE OR REPLACE FUNCTION projet.trigger_totalTags () RETURNS TRIGGER AS $$
DECLARE
	_nombre_tags INTEGER;
BEGIN
	SELECT count(qt.tag) FROM projet.questions_tags qt 
		WHERE qt.question=NEW.question INTO _nombre_tags;
	IF(_nombre_tags >= 2) THEN RAISE 'Limite du nombre de tags atteinte';	
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-----------------------------------------------------
-- TRIGGER POUR LES VERIFICATIONS DE L'AJOUT DE VOTE
-----------------------------------------------------

CREATE OR REPLACE FUNCTION projet.trigger_insererVote() RETURNS TRIGGER AS $$
DECLARE
	_statut projet.statut;
BEGIN
	-- Si la réponse appartient à l'utilisateur, il ne peut pas voter
	IF((SELECT COUNT(*) FROM projet.reponses WHERE num_reponse = NEW.reponse AND auteur = NEW.utilisateur) > 0)
	THEN RAISE 'Impossible de voter pour sa propre réponse';
	END IF;
	-- Récupération du statut
	SELECT statut FROM projet.utilisateurs WHERE id_utilisateur = NEW.utilisateur INTO _statut;
	-- Si l'utilisateur possède le statut normal, il ne peut pas voter
	IF(_statut = 'Normal')
	THEN RAISE 'Votre statut ne vous permet pas de voter';
	END IF;
	-- Si l'utilisateur possède le statut avancé, il ne doit pas avoir voté dans les dernières 24h
	IF(_statut = 'Avancé') THEN
		-- Si l'utilisateur possède le statut avancé, il ne doit pas avoir voté dans les dernières 24h
		IF((SELECT dernier_vote FROM projet.utilisateurs WHERE id_utilisateur = NEW.utilisateur) < (NOW() + interval '1 day'))
		THEN RAISE 'Vous ne pouvez pas voter plusieurs fois en 24 heures avec votre statut';
		END IF;
		-- Si l'utilisateur possède le statut avancé, il ne peut que voter positivement
		IF((NEW.est_positif = false))
		THEN RAISE 'Vous ne pouvez pas voter négativement avec votre statut';
		END IF;
	END IF;
	-- Update le score, la date du dernier vote & la réputation
	IF(NEW.est_positif = true) THEN
		UPDATE projet.reponses SET score = score+5 WHERE num_reponse = NEW.reponse;
		UPDATE projet.utilisateurs SET dernier_vote = NOW() WHERE id_utilisateur = NEW.utilisateur;
		UPDATE projet.utilisateurs SET reputation = reputation+5 WHERE id_utilisateur = (SELECT auteur FROM projet.questions WHERE num_question = NEW.reponse);
	-- Update le score & la date du dernier vote
	ELSE
		UPDATE projet.reponses SET score = score-5 WHERE num_reponse = NEW.reponse;
		UPDATE projet.utilisateurs SET dernier_vote = NOW() WHERE id_utilisateur = NEW.utilisateur;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

---------------------------------------------------------------
-- TRIGGER POUR LES VERIFICATIONS DE MODIFICATION DE QUESTIONS
---------------------------------------------------------------


CREATE OR REPLACE FUNCTION projet.trigger_modificationQuestion() RETURNS TRIGGER AS $$
DECLARE
	_statut projet.statut;
BEGIN
	SELECT u.statut FROM projet.utilisateurs u WHERE u.id_utilisateur = NEW.auteur_modification INTO _statut;
	IF(_statut='Normal' AND NEW.auteur_modification <> NEW.auteur) THEN RAISE 'Statut trop bas pour modifier les questions des autres utilisateurs';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---------------------------------------------------------------
-- TRIGGER POUR LES VERIFICATIONS DE MODIFICATION DE REPONSES
---------------------------------------------------------------

CREATE OR REPLACE FUNCTION projet.trigger_modificationReponse() RETURNS TRIGGER AS $$
DECLARE
	_statut projet.statut;
	_auteur INTEGER;
BEGIN
	SELECT u.statut FROM projet.utilisateurs u WHERE u.id_utilisateur = NEW.auteur INTO _statut;
	SELECT r.auteur FROM projet.reponses r WHERE r.num_reponse = NEW.num_reponse AND r.num_question = NEW.num_question INTO _auteur;
	IF( _statut='Normal' AND _auteur != NEW.auteur) THEN RAISE 'Statut trop bas pour modifier les reponses des autres utilisateurs';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

---------------------------------------------------------------
-- TRIGGER POUR LES VERIFICATIONS APRES UPDATE D'UTILISATEUR
---------------------------------------------------------------

CREATE OR REPLACE FUNCTION projet.trigger_updateUtilisateur() RETURNS TRIGGER AS $$
DECLARE

BEGIN
	-- Si la réputation a changé
	IF(OLD.reputation != NEW.reputation) THEN
		IF(NEW.reputation >= 5 AND OLD.statut = 'Normal') THEN
			UPDATE projet.utilisateurs SET statut = 'Avancé' WHERE id_utilisateur = NEW.id_utilisateur;
		END IF;
		IF(NEW.reputation >= 10 AND OLD.statut = 'Avancé') THEN
			UPDATE projet.utilisateurs SET statut = 'Master' WHERE id_utilisateur = NEW.id_utilisateur;
		END IF;
	ELSE
		-- Si le statut a changé sans que le score change, alors on ne peut pas regresser dans le statut
		IF(OLD.statut != NEW.statut) THEN
			IF(OLD.statut = 'Master') THEN RAISE 'Le statut de l utilisateur était déjà le plus élevé possible';
			END IF;
			IF((OLD.statut = 'Avancé' AND NEW.statut = 'Normal')) THEN RAISE 'Le statut de l utilisateur ne peut pas diminuer';
			END IF;
		END IF;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

---------------------------------------------------------
-- TRIGGER POUR VERIFIER SI LA QUESTION EST DEJA CLOTURE
---------------------------------------------------------

CREATE OR REPLACE FUNCTION projet.trigger_cloturerQuestion() RETURNS TRIGGER AS $$
DECLARE

BEGIN
	IF(OLD.est_cloture = true) THEN RAISE 'Question déjà cloturée ! ';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-----------------------------------------------------------------------------
-- CREATIONS DES TRIGGERS ---------------------------------------------------
-----------------------------------------------------------------------------

CREATE TRIGGER trigger_questions_tags BEFORE INSERT ON projet.questions_tags
	FOR EACH ROW EXECUTE PROCEDURE projet.trigger_totalTags();

CREATE TRIGGER trigger_vote BEFORE INSERT ON projet.votes
	FOR EACH ROW EXECUTE PROCEDURE projet.trigger_insererVote();

CREATE TRIGGER trigger_modification_questions BEFORE UPDATE ON projet.questions
	FOR EACH ROW EXECUTE PROCEDURE projet.trigger_modificationQuestion();

CREATE TRIGGER trigger_utilisateur AFTER UPDATE ON projet.utilisateurs
	FOR EACH ROW EXECUTE PROCEDURE projet.trigger_updateUtilisateur();

CREATE TRIGGER trigger_cloture AFTER UPDATE ON projet.questions
	FOR EACH ROW EXECUTE PROCEDURE projet.trigger_cloturerQuestion();


---------------------------------------------------------------
-- VIEWS ------------------------------------------------------
---------------------------------------------------------------

---------------------------------------------------------------
-- VUE REPONSES POSEES
---------------------------------------------------------------

CREATE VIEW projet.vue_reponses AS 
SELECT r.num_reponse AS "Numéro réponse", r.num_question AS "Numéro question",r.auteur AS "Auteur",r.date_publication AS "Date de publication",r.score AS "Score de la réponse",r.contenu AS "Contenu réponse"
FROM projet.reponses r ;

---------------------------------------------------------------
-- VUE QUESTIONS LIEES A UN TAG PARTICULIER
---------------------------------------------------------------

CREATE VIEW projet.vue_questions_tags AS
	SELECT q.num_question AS "Numéro question",  q.date_publication AS "Date de publication", u1.nom_utilisateur AS "Auteur question", q.date_modification AS "Date modification", u2.nom_utilisateur AS "Auteur modification", q.titre AS "Titre question", t.num_tag AS "Numéro tag", t.intitule_tag AS "Tag question"
	FROM  projet.questions q
	INNER JOIN projet.questions_tags qt ON qt.question = q.num_question
	INNER JOIN projet.tags t ON t.num_tag = qt.tag
	INNER JOIN projet.utilisateurs u1 ON q.auteur = u1.id_utilisateur
	LEFT OUTER JOIN projet.utilisateurs u2 ON q.auteur_modification = u2.id_utilisateur
	WHERE q.est_cloture = false
	ORDER BY date_publication DESC;

---------------------------------------------------------------
-- VUE TOUTES LES QUESTIONS
---------------------------------------------------------------

CREATE VIEW projet.vue_questions AS
	SELECT  q.num_question AS "Numéro question",q.date_publication AS "Date de publication", u1.id_utilisateur AS "ID auteur question", u1.nom_utilisateur AS "Auteur question", q.date_modification AS "Date modification", u2.id_utilisateur AS "ID auteur modification", u2.nom_utilisateur AS "Auteur modification", q.titre AS "Titre question", q.corps AS "Corps question"
	FROM  projet.questions q
	INNER JOIN projet.utilisateurs u1 ON q.auteur = u1.id_utilisateur
	LEFT OUTER JOIN projet.utilisateurs u2 ON q.auteur_modification = u2.id_utilisateur
	WHERE q.est_cloture = false
	ORDER BY date_publication DESC;

---------------------------------------------------------------
-- GRANT ------------------------------------------------------
---------------------------------------------------------------

GRANT CONNECT ON DATABASE dbndelann15 TO ftimmer16;
GRANT USAGE ON SCHEMA projet TO ftimmer16;
GRANT USAGE, SELECT ON projet.utilisateurs_id_utilisateur_seq, projet.questions_num_question_seq, projet.reponses_num_reponse_seq TO ftimmer16;
GRANT SELECT ON projet.vue_reponses, projet.vue_questions_tags, projet.vue_questions TO ftimmer16;
GRANT SELECT, INSERT, UPDATE ON projet.utilisateurs TO ftimmer16;	
GRANT SELECT, INSERT, UPDATE ON projet.questions TO ftimmer16;
GRANT SELECT, INSERT, UPDATE ON projet.reponses TO ftimmer16;
GRANT SELECT, INSERT ON projet.votes TO ftimmer16;
GRANT SELECT, INSERT ON projet.questions_tags TO ftimmer16;
GRANT SELECT ON projet.tags TO ftimmer16;