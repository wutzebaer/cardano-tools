REM docker run --rm -ti -e CARDANO_NODE_SOCKET_PATH=/ipc/node.socket -v testenet-ipc:/ipc -v %cd%:/work -w /work -u 0 --entrypoint bash cardanocommunity/cardano-node
docker exec -e CARDANO_NODE_SOCKET_PATH=/ipc/node.socket -ti testnet-cardano-node-1 bash