/*
Name: Kyle Lukaszek
Class: CIS*4650 Compilers
Checkpoint 1: AST Construction
Due: March 4, 2024

This file structure is based on the JFlex file provided for the TINY language.
*/

/* --------------------------Usercode Section------------------------ */

import java_cup.runtime.*;

%%

/* -----------------Options and Declarations Section----------------- */

/*
  The name of the class JFlex will create will be Lexer.
  Will write the code to the file Lexer.java.
*/
%class Lexer

%eofval{
  return null;
%eofval};

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column

/*
  Will switch to a CUP compatibility mode to interface with a CUP
  generated parser.
*/
%cup

/*
  Declarations

  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.
*/
%{

  /* To create a new java_cup.runtime.Symbol with information about
     the current token, the token will have no value in this
     case. */
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }

  /* Also creates a new java_cup.runtime.Symbol with information
     about the current token, but this object has a value. */
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}


/* ------------------ Macros and Regular Expressions Section----------------- */ 

// First two macros are from tiny.flex

/* A line terminator is a \r (carriage return), \n (line feed), or
   \r\n. */
LineTerminator = \r|\n|\r\n
   
/* White space is a line terminator, space, tab, or form feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]

// Regexp for comments

%%
/* ------------------------Lexical Rules Section---------------------- */

/*
  This section contains regular expressions and actions, i.e. Java
  code, that will be executed when the scanner matches the associated
  regular expression.
*/

// Skip whitespace and comments
{WhiteSpace} { /* skip whitespace */ }
"\/\*"([^*]|(\*+([^*\/]|[\r\n])))*"\*\/"   { /* skip comments */ }

// Keywords
"bool"    { return symbol(sym.BOOL); }
"if"      { return symbol(sym.IF); }
"else"    { return symbol(sym.ELSE); }
"int"     { return symbol(sym.INT); }
"return"  { return symbol(sym.RETURN); }
"void"    { return symbol(sym.VOID); }
"while"   { return symbol(sym.WHILE); }

// Truth values
"true"    { return symbol(sym.TRUE); }
"false"   { return symbol(sym.FALSE); }

// Regexp for identifiers
[a-zA-Z_][a-zA-Z0-9_]* { return symbol(sym.ID, yytext()); }

// Regexp for numbers (integers)
[0-9]+   { return symbol(sym.INTEGER_LITERAL, yytext()); }

// Special symbols
"+"         { return symbol(sym.PLUS); }
"-"         { return symbol(sym.MINUS); }
"*"         { return symbol(sym.MULT); }
"/"         { return symbol(sym.DIV); }

"<"         { return symbol(sym.LT); }
"<="        { return symbol(sym.LE); }

">"         { return symbol(sym.GT); }
">="        { return symbol(sym.GE); }

"=="        { return symbol(sym.EQ); }
"!="        { return symbol(sym.NEQ); }

"~"         { return symbol(sym.NOT); }
"||"        { return symbol(sym.OR); }
"&&"        { return symbol(sym.AND); }

"="         { return symbol(sym.ASSIGN); }
";"         { return symbol(sym.SEMI); }
","         { return symbol(sym.COMMA); }

"("         { return symbol(sym.LPAREN); }
")"         { return symbol(sym.RPAREN); }

"{"         { return symbol(sym.LBRACE); }
"}"         { return symbol(sym.RBRACE); }

"["         { return symbol(sym.LBRACKET); }
"]"         { return symbol(sym.RBRACKET); }

// Anything else returns an error token
.         { return symbol(sym.ERROR); }

/* --------------------------End of File Section--------------------- */
