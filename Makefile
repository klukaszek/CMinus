JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP= $(JAVA) $(CLASSPATH) java_cup.Main

all: absyn/*.java parser.java sym.java CM.class

#Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

CM.class: ShowTreeVisitor.java Lexer.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) -expect 10 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~
