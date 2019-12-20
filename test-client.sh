

# Execute servers
executeClients(){
	mvn exec:java -Dexec.mainClass="trocadilho.client.Client"
}

# 100 executions
for i in {1..15}
do
	executeClients & # Put the function in background
	sleep 15
done

# Block the terminal until all threads finished
wait
echo "All done"