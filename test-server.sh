

# Execute servers
executeServers(){
	mvn exec:java -Dexec.mainClass="trocadilho.ServerGRPC"
}

# 10 executions
for i in {1..4}
do
	executeServers & # Put the function in background
	sleep 20
done

# Block the terminal until all threads finished
echo "All done"