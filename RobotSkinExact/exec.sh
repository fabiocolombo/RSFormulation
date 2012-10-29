CPLEX_JAR=/opt/ibm/ILOG/CPLEX_Studio124/cplex/lib/cplex.jar
CPLEX_LIB_PATH=/opt/ibm/ILOG/CPLEX_Studio124/cplex/bin/x86_sles10_4.1
echo "Using cplex jar in:$CPLEX_JAR"
echo "Input arguments: $@"
java -Djava.library.path=$CPLEX_LIB_PATH -classpath bin:$CPLEX_JAR rs.RSDriver $@ 
