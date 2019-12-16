

# Build the server
mvn clean package

# Execute one server
mvn exec:java -Dexec.mainClass="trocadilho.ServerGRPC"

# Execute 100 clients
executeClients(){
	mvn exec:java -Dexec.mainClass="trocadilho.Client"
}

# 100 executions
for i in {1..100}
do
	executeClients & # Put the function in background
done

# Block the terminal until all threads finished
wait
echo "All done"