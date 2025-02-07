.PHONY: build-p0 build-p1 build-kafka up-kafka down-kafka run-p0 run-p1 consume-p0 consume-p1 consume-transformed consume-rejected run-all help \
        build-p2-articlebext run-p2-articlebext consume-p2-articlebext \
        build-p2-articledilicom run-p2-articledilicom consume-p2-articledilicom \
        build-p2-articleecom run-p2-articleecom consume-p2-articleecom \
        build-p2-articleur run-p2-articleur consume-p2-articleur \
        build-p2-manifestationecom run-p2-manifestationecom consume-p2-manifestationecom \
        build-p3-articlebext run-p3-articlebext consume-p3-articlebext

help:
	@echo "Usage: make <target>"
	@echo
	@echo "Targets disponibles :"
	@echo "  up-kafka                : Lance Kafka/Zookeeper (docker-compose) en arrière-plan"
	@echo "  down-kafka              : Arrête et supprime les containers Kafka/Zookeeper"
	@echo "  build-p0                : Construit l'image Docker pour le processus P0"
	@echo "  run-p0                  : Exécute le conteneur Docker de P0 (lecture du fichier et envoi vers topic staging)"
	@echo "  build-p1                : Construit l'image Docker pour le processus P1"
	@echo "  run-p1                  : Exécute le conteneur Docker de P1 (validation et envoi vers topic validé ou rejeté)"
	@echo "  build-p2-articlebext    : Construit l'image Docker pour la transformation P2 -> ArticleBext"
	@echo "  run-p2-articlebext      : Exécute le conteneur Docker P2 -> ArticleBext"
	@echo "  build-p2-articledilicom : Construit l'image Docker pour la transformation P2 -> ArticleDilicom"
	@echo "  run-p2-articledilicom   : Exécute le conteneur Docker P2 -> ArticleDilicom"
	@echo "  build-p2-articleecom    : Construit l'image Docker pour la transformation P2 -> ArticleEcom"
	@echo "  run-p2-articleecom      : Exécute le conteneur Docker P2 -> ArticleEcom"
	@echo "  build-p2-articleur      : Construit l'image Docker pour la transformation P2 -> ArticleUR"
	@echo "  run-p2-articleur        : Exécute le conteneur Docker P2 -> ArticleUR"
	@echo "  build-p2-manifestationecom  : Construit l'image Docker pour la transformation P2 -> ManifestationEcom"
	@echo "  run-p2-manifestationecom    : Exécute le conteneur Docker P2 -> ManifestationEcom"
	@echo "  build-p3-articlebext    : Construit l'image Docker pour le process P3 (ArticleBext FileWriter)"
	@echo "  run-p3-articlebext      : Exécute le conteneur Docker P3 (ArticleBext FileWriter)"
	@echo "  consume-p3-articlebext  : Affiche un message d'information pour P3 (le process écrit dans un fichier)"
	@echo
	@echo "  run-all                 : Exemple de pipeline (build p0/p1, up-kafka, run-p0, run-p1)"
	@echo

######################################
# Kafka
######################################
up-kafka:
	docker compose -f docker-compose-pipeline.yml up -d

down-kafka:
	docker compose -f docker-compose-pipeline.yml down

######################################
# P0
######################################
build-p0:
	docker build \
		-t p0-file-reader \
  		-f src/main/java/rmn/ETL/stream/process/P0/Dockerfiles/P0_ArticleX3_FileReader.Dockerfile \
  		.

run-p0:
	docker run --rm --name p0-file-reader \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e KAFKA_TOPIC_STAGING=article_staging \
	  -v "$(PWD)/input:/app/input" \
	  p0-file-reader

consume-p0:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_staging \
		  --from-beginning'

######################################
# P1
######################################
build-p1:
	docker build \
		-t p1-validation-process \
		-f src/main/java/rmn/ETL/stream/process/P1/Dockerfiles/P1_ArticleX3_ValidationProcess.Dockerfile \
		.

run-p1:
	docker run --rm --name p1-validation-process \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
	  -e KAFKA_TOPIC_STAGING=article_staging \
	  -e KAFKA_TOPIC_VALIDATED=article_validated \
	  -e KAFKA_TOPIC_REJECTED=article_rejected \
	  p1-validation-process

consume-p1:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_validated \
		  --from-beginning'

consume-rejected:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_rejected \
		  --from-beginning'

######################################
# P2 - ArticleBext
######################################
build-p2-articlebext:
	docker build \
	  -t p2-articlebext \
	  -f src/main/java/rmn/ETL/stream/process/P2/Dockerfiles/P2_ArticleX3_ArticleBext_TransformationProcess.Dockerfile \
	  .

run-p2-articlebext:
	docker run --rm --name p2-articlebext \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
	  -e KAFKA_TOPIC_VALIDATED=article_validated \
	  -e KAFKA_TOPIC_TRANSFORMED=article_bext_transformed \
	  -e SPRING_PROFILES_ACTIVE=P2 \
	  p2-articlebext

consume-p2-articlebext:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_bext_transformed \
		  --from-beginning'

######################################
# P2 - ArticleDilicom
######################################
build-p2-articledilicom:
	docker build \
	  -t p2-articledilicom \
	  -f src/main/java/rmn/ETL/stream/process/P2/Dockerfiles/P2_ArticleX3_ArticleDilicom_TransformationProcess.Dockerfile \
	  .

run-p2-articledilicom:
	docker run --rm --name p2-articledilicom \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
	  -e KAFKA_TOPIC_VALIDATED=article_validated \
	  -e KAFKA_TOPIC_TRANSFORMED=article_dilicom_transformed \
	  -e SPRING_PROFILES_ACTIVE=P2 \
	  p2-articledilicom

consume-p2-articledilicom:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_dilicom_transformed \
		  --from-beginning'

######################################
# P2 - ArticleEcom
######################################
build-p2-articleecom:
	docker build \
	  -t p2-articleecom \
	  -f src/main/java/rmn/ETL/stream/process/P2/Dockerfiles/P2_ArticleX3_ArticleEcom_TransformationProcess.Dockerfile \
	  .

run-p2-articleecom:
	docker run --rm --name p2-articleecom \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
	  -e KAFKA_TOPIC_VALIDATED=article_validated \
	  -e KAFKA_TOPIC_TRANSFORMED=article_ecom_transformed \
	  -e SPRING_PROFILES_ACTIVE=P2 \
	  p2-articleecom

consume-p2-articleecom:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_ecom_transformed \
		  --from-beginning'

######################################
# P2 - ArticleUR
######################################
build-p2-articleur:
	docker build \
	  -t p2-articleur \
	  -f src/main/java/rmn/ETL/stream/process/P2/Dockerfiles/P2_ArticleX3_ArticleUR_TransformationProcess.Dockerfile \
	  .

run-p2-articleur:
	docker run --rm --name p2-articleur \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
	  -e KAFKA_TOPIC_VALIDATED=article_validated \
	  -e KAFKA_TOPIC_TRANSFORMED=article_ur_transformed \
	  -e SPRING_PROFILES_ACTIVE=P2 \
	  p2-articleur

consume-p2-articleur:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic article_ur_transformed \
		  --from-beginning'

######################################
# P2 - ManifestationEcom
######################################
build-p2-manifestationecom:
	docker build \
	  -t p2-manifestationecom \
	  -f src/main/java/rmn/ETL/stream/process/P2/Dockerfiles/P2_ArticleX3_ManifestationEcom_TransformationProcess.Dockerfile \
	  .

run-p2-manifestationecom:
	docker run --rm --name p2-manifestationecom \
	  --network my_kafka_network \
	  -e KAFKA_BROKER=kafka:9092 \
	  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
	  -e KAFKA_TOPIC_VALIDATED=article_validated \
	  -e KAFKA_TOPIC_TRANSFORMED=manifestation_ecom_transformed \
	  -e SPRING_PROFILES_ACTIVE=P2 \
	  p2-manifestationecom

consume-p2-manifestationecom:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
		./kafka-console-consumer.sh \
		  --bootstrap-server kafka:9092 \
		  --topic manifestation_ecom_transformed \
		  --from-beginning'

######################################
# P3 - ArticleBext FileWriter (Load Process)
######################################
build-p3-articlebext:
	docker build \
	  -t p3-articlebext \
	  -f src/main/java/rmn/ETL/stream/process/P3/Dockerfiles/P3_ArticleBext_FileWriter.Dockerfile \
	  .

run-p3-articlebext:
	docker run --rm --name p3-articlebext \
		--network my_kafka_network \
		-e KAFKA_BROKER=kafka:9092 \
		-e OUTPUT_DIRECTORY=/app/output \
		-e KAFKA_TOPIC_TRANSFORMED=article_bext_transformed \
		-e SPRING_PROFILES_ACTIVE=P3 \
		-v "$(PWD)/output:/app/output" \
		p3-articlebext

consume-p3-articlebext:
	@echo "Le process P3 écrit dans un fichier, il n'y a pas de topic à consommer directement pour P3."

run-all: build-p0 build-p1 up-kafka run-p0 run-p1