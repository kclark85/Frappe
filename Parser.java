/*
    This class provides a recursive descent parser 
    for Frappe,
    creating a parse tree which can be interpreted
    to simulate execution of a Frappe program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      System.out.println("-----> parsing <program>:");
      Node first = parseClasses();
      return new Node("program", first, null, null);
   }

   public Node parseClasses() {
      System.out.println("-----> parsing <classes>:");
      Node first = parseClass();
      Token token = lex.getNextToken();
      if ( token.isKind("eof")) {
         return new Node("classes", first, null, null);
      }
      else {
         lex.putBackToken(token);
         Node second = parseClasses();
         return new Node("classes", first, second, null);
      }
   }

   public Node parseClass() {
      System.out.println("-----> parsing <class>:");
      Token token = lex.getNextToken();
      errorCheck(token, "class");
      Token name = lex.getNextToken();
      errorCheck(name, "className");
      token.getNextToken()
      errorCheck(token, "single", "{")
      Node first = parseMembers();
      return new Node("class", name.getDetails(), first, null, null);
   }

   public Node parseMembers() {
      System.out.println("-----> parsing <members>:");
      Node first = parseMember();
      Token token = lex.getNextToken();
      if ( if (token.matches("single", "}")) {) {
         return new Node("members", first, null, null);
      }
      else {
         lex.putBackToken(token);
         Node second = parseMembers();
         return new Node("members", first, second, null);
      }
   }

   public Node parseMember() {
      System.out.println("-----> parsing <member>:");
      Token token = lex.getNextToken();
      if (token.isKind("static")) { //child might be a <staticMethod> or <staticField>
         Token name = lex.getNextToken();
         errorCheck(name, "name");
         token = lex.getNextToken();
         if (token.matches("single", "(")) { //child is a <staticMethod>
            lex.putBackToken(token);
            lex.putBackToken(name);
            Node first = parseStaticMethod();
            return new Node("member", first, null, null);
         }
         else { //child is a <staticField>
            lex.putBackToken(token);
            lex.putBackToken(name);
            Node first = parseStaticField();
            return new Node("member", first, null, null);
         }
      }
      else if (token.isKind("name") { //child might be a <instanceField> or <instanceMethod>
         Token name = token;
         token = lex.getNextToken();
         if (token.matches("single", "(")) { //child is an <instanceMethod>
            lex.putBackToken(token);
            lex.putBackToken(name);
            Node first = parseInstanceMethod();
            return new Node("member", first, null, null);
         }
         else if { //child is an <instanceMethod>
            lex.putBackToken(token);
            lex.putBackToken(name);
            Node first = parseInstanceMethod();
            return new Node("member", first, null, null);
         }
      }
      else if (token.isKind("className")) { //child is a <constructor>
         lex.putBackToken(token);
         Node first = parseConstructor();
         return new Node("member", first, null, null);
      }
      else { // error
         System.out.println("expected static or name or className and saw " + token );
         System.exit(1);
         return null;
      }
   }

   public Node parseStaticField() {
      System.out.println("-----> parsing <staticField>:");
      Node name = lex.getNextToken();
      errorCheck(name, "name");
      Token token = lex.getNextToken();
      if (token.matches("single", "=")) { //child is an <expression>
         Node first = parseExpression();
         return new Node("staticField", name.getDetails(), first, null, null);
      }
      else { //node is a declaration
         return new Node("staticField", name.getDetails(), null, null, null);
      }
   }

   public Node parseStaticMethod() {
      System.out.println("-----> parsing <staticMethod>:");
   }

   public Node parseInstanceField() {
      System.out.println("-----> parsing <instanceField>:");
   }

   public Node parseConstructor() {
      System.out.println("-----> parsing <constructor>:");
   }

   public Node parseInstanceMethod() {
      System.out.println("-----> parsing <instanceMethod>:");
   }

   public Node parseParams() {
      System.out.println("-----> parsing <params>:");
   }

   public Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
   }

   public Node parseStatement() {
      System.out.println("-----> parsing <statement>:");
   }

   public Node parseForStatement() {
      System.out.println("-----> parsing <forStatement>:");
   }

   public Node parseIfStatement() {
      System.out.println("-----> parsing <ifStatement>:");
   }

   public Node parseExpression() {
      System.out.println("-----> parsing <expression>:");
   }

   public Node parseCall() {
      System.out.println("-----> parsing <call>:");
   }

   public Node parseCaller() {
      System.out.println("-----> parsing <caller>:");
   }

   public Node parseArgs() {
      System.out.println("-----> parsing <args>:");
   }

   // check whether token is correct kind
   private void errorCheck( Token token, String kind ) {
      if( ! token.isKind( kind ) ) {
         System.out.println("Error:  expected " + token + 
                            " to be of kind " + kind );
         System.exit(1);
      }
   }

   // check whether token is correct kind and details
   private void errorCheck( Token token, String kind, String details ) {
      if( ! token.isKind( kind ) || 
          ! token.getDetails().equals( details ) ) {
         System.out.println("Error:  expected " + token + 
                             " to be kind= " + kind + 
                             " and details= " + details );
         System.exit(1);
      }
   }

}
