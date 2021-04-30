# cardano-tools

## Build
```
mvn package -DskipTests=true
docker build -t wutzebaer/cardano-tools:latest . 
docker push wutzebaer/cardano-tools:latest
```

## Compose file
```
version: '3'

services:

  cardano-tools:
      image: wutzebaer/cardano-tools:latest
      restart: always
      volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - /work:/work
      ports:
      - 127.0.0.1:8087:8087
      environment:
      - working.dir=/work
      - cardano-db-sync.password=XXX
      - pledge-address=addr1qx6pnsm9n3lrvtwx24kq7a0mfwq2txum2tvtaevnpkn4mpyghzw2ukr33p5k45j42w62pqysdkf65p34mrvl4yu4n72s7yfgkq
      - spring.datasource.url=jdbc:h2:file:/work/database
      - server.port=8087
      - network=mainnet
      network_mode: host
```

