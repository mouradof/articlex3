.PHONY: build-p0 build-p1 build-kafka up-kafka down-kafka run-p0 run-p1 consume-p0 consume-p1 consume-transformed consume-rejected run-all

build-p0:
	docker build \
		-t p0-file-reader \
  		-f src/main/java/rmn/ETL/stream/process/P0/Dockerfiles/P0_ArticleX3_FileReader.Dockerfile \
  		.

build-p1:
	docker build \
		-t p1-validation-process \
  		-f src/main/java/rmn/ETL/stream/process/P1/Dockerfiles/P1_ArticleX3_ValidationProcess.Dockerfile \
		.

up-kafka:
	docker compose -f docker-compose-pipeline.yml up -d

down-kafka:
	docker compose -f docker-compose-pipeline.yml down

run-p0:
	docker run --rm --name p0-file-reader \
      --network my_kafka_network \
      -e KAFKA_BROKER=kafka:9092 \
      -e KAFKA_TOPIC_STAGING=article_staging \
      -v "$(PWD)/input:/app/input" \
      p0-file-reader

run-p1:
	docker run --rm --name p1-validation-process \
		--network my_kafka_network \
		-e KAFKA_BROKER=kafka:9092 \
		-e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
		-e KAFKA_TOPIC_STAGING=article_staging \
		-e KAFKA_TOPIC_VALIDATED=article_validated \
		-e KAFKA_TOPIC_REJECTED=article_rejected \
		p1-validation-process

consume-p0:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
	./kafka-console-consumer.sh \
	  --bootstrap-server kafka:9092 \
	  --topic article_staging \
	  --from-beginning'

consume-p1:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
	./kafka-console-consumer.sh \
	  --bootstrap-server localhost:9092 \
	  --topic article_transformed \
	  --from-beginning'

consume-rejected:
	docker exec -it kafka bash -c 'cd /opt/bitnami/kafka/bin && \
	./kafka-console-consumer.sh \
	  --bootstrap-server localhost:9092 \
	  --topic article_rejected \
	  --from-beginning'

run-all: build-p0 build-p1 up-kafka run-p0 run-p1
