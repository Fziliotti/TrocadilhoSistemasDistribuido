

# Build the server
# mvn clean package

# Execute one server
cd target
java -jar server.jar &

# Execute 100 clients
executeClients(){
	java -jar client.jar < in
}

# 100 executions
for i in {1..100}
do
	executeClients & # Put the function in background
done

# Block the terminal until all threads finished
wait
echo "All done"