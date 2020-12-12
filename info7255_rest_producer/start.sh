#!/bin/bash

### info 7255 script

# start the redis-server
redis-server &

# start the elastic search
cd /usr/local/var/homebrew/linked
 ./elasticsearch-full/bin/elasticsearch -d

# start the zookeeper
brew services start zookeeper

# start the kafka
brew services start kafka

# start the kibana
./kibana-full/bin/kibana