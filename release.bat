call mvn package -DskipTests=true
docker build -t wutzebaer/cardano-tools:latest . 
docker push wutzebaer/cardano-tools:latest
pause