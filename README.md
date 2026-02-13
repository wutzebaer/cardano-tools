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
      - pledge-address=addr1qy556kyh5vx2k2qgatj75m4f3un0p282dkkvcm38pewkzvrv0h6rckjajnad9l4xc3eczdsel7r7r6t4amhpql5jvaxsr78krj
      - spring.datasource.url=jdbc:h2:file:/work/database
      - server.port=8087
      - network=mainnet
      network_mode: host
```

