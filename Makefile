JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP=cup

all: absyn/*.java parser.java sym.java CM.class

Absyn.class: absyn/*.java

#Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

CM.class:  Lexer.java ShowTreeVisitor.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) -expect 10 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~
