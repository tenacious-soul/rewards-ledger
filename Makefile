.PHONY: run test build docker-up docker-down clean

run:
	sh ./mvnw spring-boot:run

test:
	sh ./mvnw test

build:
	sh ./mvnw package

docker-up:
	docker compose up --build

docker-down:
	docker compose down

clean:
	sh ./mvnw clean
