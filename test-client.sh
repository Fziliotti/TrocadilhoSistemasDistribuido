

# Execute servers
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