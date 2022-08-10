call mvn spring-boot:build-image -Dspring-boot.build-image.imageName=wutzebaer/cardano-tools:latest -DskipTests=true
docker push wutzebaer/cardano-tools:latest
pause