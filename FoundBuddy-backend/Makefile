.PHONY: run build test db-up db-down docker-build docker-run fmt

run:
	\tmvn -q spring-boot:run

build:
	\tmvn -q -DskipTests package

test:
	\tmvn -q test

db-up:
	\tdocker compose up -d

db-down:
	\tdocker compose down -v

docker-build:
	\tdocker build -t foundbuddy-backend:dev .

docker-run:
	\tdocker run --rm -p 8080:8080 foundbuddy-backend:dev

fmt:
	\t# Platzhalter für Code-Formatierung (Spotless/Formatter kannst du später hinzufügen)
	\t@echo "Format step (add your formatter here)"
