#!/bin/bash

java -jar jar/Server.jar &> serverstress.log &
sleep 0.5;

client() {
	while java -jar jar/ServerTest.jar > /dev/null; do
		echo "GOOD"
	done
}

for i in {1..8}; do
	client &
done

