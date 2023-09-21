call mvn spring-boot:build-image -Dspring-boot.build-image.imageName=wutzebaer/cardano-tools:latest -Dmaven.test.skip
docker push wutzebaer/cardano-tools:latest
pause