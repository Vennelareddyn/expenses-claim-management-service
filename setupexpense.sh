#!/bin/bash

set -e

echo "🚀 Starting Docker Compose..."
docker-compose up -d

echo "⏳ Waiting for containers to be ready..."
sleep 15

echo "📦 Creating Kafka Topics..."
docker exec -it nvsat-kafka-1 kafka-topics --list --bootstrap-server localhost:9092
docker exec -it nvsat-kafka-1 kafka-topics --create --topic expense-submitted --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec -it nvsat-kafka-1 kafka-topics --create --topic expense-approved --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

echo "🕒 Starting Temporal Dev Server (in background)..."
temporal server start-dev &

echo "🗄️ Configuring MongoDB Replica Sets..."
docker exec -i nvsat-configsvr-1 mongosh --port 27019 <<EOF
rs.initiate({
  _id: "configReplSet",
  configsvr: true,
  members: [{ _id: 0, host: "nvsat-configsvr-1:27019" }]
})
EOF

docker exec -i nvsat-shard1-1 mongosh --port 27018 <<EOF
rs.initiate({
  _id: "shardReplSet1",
  members: [{ _id: 0, host: "nvsat-shard1-1:27018" }]
})
EOF

docker exec -i nvsat-shard2-1 mongosh --port 27028 <<EOF
rs.initiate({
  _id: "shardReplSet2",
  members: [{ _id: 0, host: "nvsat-shard2-1:27028" }]
})
EOF

docker exec -i nvsat-shard3-1 mongosh --port 27038 <<EOF
rs.initiate({
  _id: "shardReplSet3",
  members: [{ _id: 0, host: "nvsat-shard3-1:27038" }]
})
EOF

echo "🔗 Adding shards and configuring zones..."
docker exec -i nvsat-mongos-1 mongosh <<EOF
sh.addShard("shardReplSet1/nvsat-shard1-1:27018")
sh.addShard("shardReplSet2/nvsat-shard2-1:27028")
sh.addShard("shardReplSet3/nvsat-shard3-1:27038")

sh.enableSharding("expense_db")
sh.shardCollection("expense_db.expenses", { category: 1 })

sh.addShardTag("shardReplSet1", "office_zone")
sh.addShardTag("shardReplSet2", "training_zone")
sh.addShardTag("shardReplSet3", "equipment_zone")

sh.updateZoneKeyRange("expense_db.expenses", { category: "OFFICE" }, { category: "OFFICEZZZ" }, "office_zone")
sh.updateZoneKeyRange("expense_db.expenses", { category: "TRAINING" }, { category: "TRAININGZZZ" }, "training_zone")
sh.updateZoneKeyRange("expense_db.expenses", { category: "EQUIPMENT" }, { category: "EQUIPMENTZZZZZZ" }, "equipment_zone")

use expense_db

db.expenses.insertMany([
  { employeeId: "E123", category: "OFFICE", amount: 100, currency: "USD", description: "Office supplies", status: "PENDING" },
  { employeeId: "E456", category: "TRAINING", amount: 250, currency: "USD", description: "Workshop fee", status: "PENDING" },
  { employeeId: "E789", category: "EQUIPMENT", amount: 500, currency: "USD", description: "Laptop purchase", status: "PENDING" }
])

sh.status()
db.expenses.getShardDistribution()
EOF

echo "✅ Setup complete!"
