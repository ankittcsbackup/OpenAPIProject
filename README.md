# Hello OpenAPI Contract-first

## Run
mvn clean install
java -jar target/*.jar

GET http://localhost:8080/hello

## Docker
docker build -t hello-api .
docker run -p 8080:8080 hello-api
