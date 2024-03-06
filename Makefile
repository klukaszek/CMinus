JAVA=java
JAVAC=javac
JFLEX=jflex
CUP=cup
CLASSPATH=-cp /usr/share/java/cup.jar:.

all: absyn/*.java parser.java sym.java Scanner.class CM.class

Absyn.class: absyn/*.java

#Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

Scanner.class: Lexer.java ShowTreeVisitor.java Scanner.java

CM.class:  Lexer.java ShowTreeVisitor.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	$(CUP) $(CLASSPATH) -expect 10 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~
