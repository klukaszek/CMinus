JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP=$(JAVA) $(CLASSPATH) java_cup.Main

all:  CM.class

CM.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) -expect 10 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~
