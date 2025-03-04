# Objectif du document :

L’objectif de ce document de fournir les « recettes » et les informations utiles au développement de nouvelles interfaces en s’appuyant sur la plateforme développée dans le cadre du POC interface sans Talend de la nouvelle architecture

Il n’a pas pour objectif d’expliquer le détail du fonctionnement de la plateforme par elle-même.

# Installation du projet.

## Outils de développement :

- Docker desktop

- IDE : Visual Studio Code/IntelliJ

- Gitlab

- Spring Boot

- Environment Kafka

- Gradle

## paramétrage

- Lancement de l'environnement Kafka

- Mettre la librarie common-library dans son local-repo et changer le path dans le gradle

- Faire un ./gradlew clean build -x test

# Description du projet.

## Répertoire « config »

Le répertoire configuration contient les classes de configuration Spring Boot nécessaires à l'exécution et à l'organisation des composants de l'application ETL. Ces configurations permettent d'activer des fonctionnalités clés telles que la planification des tâches, la gestion des dépendances entre les composants et la structuration des flux de traitement des données.

Les configurations sont organisées pour couvrir les différents processus ETL, notamment :

- Gestion des tâches planifiées : Définition d'un planificateur de tâches pour orchestrer les exécutions périodiques.

- Chargement des composants métier : Identification et chargement automatique des services, entités et utilitaires communs.

- Paramétrage du traitement des fichiers : Structuration des fichiers en entrée et en sortie avec des formats spécifiques pour garantir la cohérence des données traitées.

## Répertoire « entities »

Le répertoire entities regroupe l'ensemble des classes représentant les objets métier de l'application ETL. Ces entités permettent de modéliser les données manipulées au cours des traitements, en structurant les informations échangées entre les différents processus.

L'objectif principal de ces entités est de normaliser la gestion des données en définissant :

- La structure des données sources et cibles : Chaque entité représente un format de données précis utilisé dans les flux de traitement.

- L'encapsulation des attributs métier : Les classes encapsulent les attributs spécifiques des articles, produits, manifestations, et autres éléments du domaine.

- L'interopérabilité avec les traitements : Les entités sont conçues pour être utilisées par les services applicatifs en facilitant la sérialisation, la validation et la transformation des données.

## Répertoire « process »

Le répertoire process regroupe l'ensemble des processus de traitement de l'application ETL. Chaque sous-répertoire correspond à une phase distincte du pipeline ETL, permettant une séparation claire des responsabilités et une meilleure maintenabilité.

Les processus sont structurés en plusieurs phases, chacune correspondant à une étape spécifique du pipeline ETL :

#### P0 - Ingestion des données

- Chargement des fichiers d'entrée et structuration des données en entités exploitables.

- Vérification de l'existence des fichiers avant traitement.

- Lecture et parsing des fichiers structurés selon leur format métier.

#### P1 - Validation des données

- Vérification de la complétude et de l'intégrité des données extraites.

- Identification des valeurs obligatoires manquantes.

- Détection des doublons et des incohérences pour assurer la qualité des données traitées.

#### P2 - Transformation des données

- Conversion des entités source vers des formats cibles selon les besoins métier.

- Application de règles de transformation métier pour standardiser les données.

- Publication des données transformées vers des topics Kafka dédiés.

#### P3 - Chargement et export des données

- Consommation des messages transformés depuis Kafka.

- Écriture des données traitées dans des fichiers de sortie.

- Formatage des données selon les besoins de stockage ou d'exploitation.

## Répertoire « service »

Le répertoire service regroupe les services applicatifs de l'ETL. Ces services sont responsables de l'orchestration des processus métier, du traitement des fichiers en entrée et de la gestion des événements au sein du pipeline de transformation des données.

L'objectif principal des services est d'assurer la bonne exécution et la gestion des tâches ETL en :

- Supervisant l'ingestion des fichiers : Surveillance du répertoire d'entrée pour détecter et traiter les nouveaux fichiers.

- Assurant l'exécution des processus : Déclenchement des traitements ETL en fonction des fichiers entrants et des profils d'exécution.

- Gérant les logs et la traçabilité : Suivi des traitements et journalisation des erreurs ou événements critiques.

## Fichier Main

L’application ETL est initialisée via un point d’entrée centralisé, où le processus d’exécution est déterminé dynamiquement :

Détection du type de traitement via la propriété système process.

Chargement de la configuration Spring appropriée selon la valeur du processus (P0, P1, P2, P3).

Démarrage de l'application avec la configuration adaptée.

## Fichiers Gradle :

La configuration Gradle de l'application ETL définit l'environnement de build, la gestion des dépendances et les configurations spécifiques pour assurer un bon fonctionnement du projet. Elle utilise Spring Boot, Kafka, et d'autres bibliothèques essentielles pour le traitement des données.

#### Dépôts de dépendances

- Maven Central : Dépôt principal pour récupérer les bibliothèques publiques.

- Dépôt local (.m2/local-repo) : Utilisé pour gérer les bibliothèques spécifiques au projet.

#### Exclusion de dépendances

- Suppression de slf4j-simple pour éviter les conflits de gestion des logs.

#### Dépendances Principales

- Spring Boot Starter Web : Support pour les API REST et la gestion des services web.

- Spring Kafka : Support pour la communication avec Kafka.

- Jackson (XML Format) : Gestion de la sérialisation et désérialisation des fichiers XML.

- Lombok : Simplification du code avec annotations (getter, setter, etc.).

- Spring Boot DevTools : Facilite le développement en rechargement automatique.

## Liste de commandes utiles :

#### 1. Lancement des services Kafka et Zookeeper

Avant d'exécuter les traitements, il est nécessaire de démarrer l'infrastructure Kafka :

`make up-kafka`

Pour arrêter l'infrastructure :

`make down-kafka`

#### 2. Build et exécution des différents processus

Chaque processus a ses propres commandes pour la construction de l’image Docker et son exécution.

##### Processus P0 : Lecture et ingestion des fichiers

*Build l'image Docker pour P0 :*

`make build-p0`

*Exécuter le conteneur P0 pour lire et envoyer les fichiers à Kafka :*

`make run-p0`

*Consommer les messages envoyés vers le topic article_staging :*

`make consume-p0`

##### Processus P1 : Validation et routage des articles

*Build l'image Docker pour P1 :*

`make build-p1`

*Exécuter le conteneur P1 pour valider les articles :*

`make run-p1`

*Consommer les messages validés (article_validated) :*

`make consume-p1`

*Consommer les messages rejetés (article_rejected) :*

`make consume-rejected`

##### Processus P2 : Transformations des articles

Chaque transformation possède sa propre commande pour le build et l'exécution :

*Article Bext :*

- Build : `make build-p2-articlebext`

- Exécution : `make run-p2-articlebext`

- Consommation Kafka : `make consume-p2-articlebext`

*Article Dilicom :*

- Build : `make build-p2-articledilicom`

- Exécution : `make run-p2-articledilicom`

- Consommation Kafka : `make consume-p2-articledilicom`

*Article Ecom :*

- Build : `make build-p2-articleecom`

- Exécution : `make run-p2-articleecom`

-Consommation Kafka : `make consume-p2-articleecom`

*Article UR :*

- Build : `make build-p2-articleur`

- Exécution : `make run-p2-articleur`

- Consommation Kafka : `make consume-p2-articleur`

*Manifestation Ecom :*

- Build : `make build-p2-manifestationecom`

- Exécution : `make run-p2-manifestationecom`

- Consommation Kafka : `make consume-p2-manifestationecom`

*Pour builder tous les processus P2 en une seule commande :*

`make build-all-p2`

*Pour exécuter tous les processus P2 en une seule commande :*

`make run-all-p2`

##### Processus P3 : Chargement et persistance des articles

*Article Bext :*

- Build : `make build-p3-articlebext`

- Exécution : `make run-p3-articlebext`

Aucun topic Kafka à consommer (les fichiers sont écrits directement en sortie)

*Article Dilicom :*

- Build : `make build-p3-articledilicom`

- Exécution : `make run-p3-articledilicom`

*Article Ecom :*

- Build : `make build-p3-articleecom`

- Exécution : `make run-p3-articleecom`

*Article UR :*

- Build : `make build-p3-articleur`

- Exécution : `make run-p3-articleur`

*Manifestation Ecom :*

- Build : `make build-p3-manifestationecom`

- Exécution : `make run-p3-manifestationecom`

*Pour builder tous les processus P3 en une seule commande :*

`make build-all-p3`

*Pour exécuter tous les processus P3 en une seule commande :*

`make run-all-p3`

##### Exécution d'un pipeline complet (P0 -> P1 -> P2 -> P3)

Un exemple de pipeline complet peut être exécuté avec la commande suivante :

`make run-all`

Cette commande effectuera les étapes suivantes :

- Build des images Docker de P0 et P1

- Démarrage de Kafka (make up-kafka)

- Exécution des processus P0 et P1

- Transformation des données avec P2

- Persistance des données avec P3