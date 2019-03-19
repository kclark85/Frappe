
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
      if ( token.isKind("eof")) { //there are no more classes
         return new Node("classes", first, null, null);
      }
      else { //there is another class
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
      token = lex.getNextToken();
      errorCheck(token, "single", "{");
      Node first = parseMembers();
      return new Node("class", name.getDetails(), first, null, null);
   }

   public Node parseMembers() {
      System.out.println("-----> parsing <members>:");
      Node first = parseMember();
      Token token = lex.getNextToken();
      if (token.matches("single", "}")) { //there are no more members
         return new Node("members", first, null, null);
      }
      else { //there is another member
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
      else if (token.isKind("name")) { //child might be a <instanceField> or <instanceMethod>
         Token name = token;
         token = lex.getNextToken();
         if (token.matches("single", "(")) { //child is an <instanceMethod>
            lex.putBackToken(token);
            lex.putBackToken(name);
            Node first = parseInstanceMethod();
            return new Node("member", first, null, null);
         }
         else { //child is an <instanceField>
            lex.putBackToken(token);
            lex.putBackToken(name);
            Node first = parseInstanceField();
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
      Token token = lex.getNextToken();
      errorCheck(token, "name");
      token = lex.getNextToken();
      if (token.matches("single", "=")) { //child is an <expression>
         Node first = parseExpression();
         return new Node("staticField", token.getDetails(), first, null, null);
      }
      else { //node is a declaration
         return new Node("staticField", token.getDetails(), null, null, null);
      }
   }

   public Node parseStaticMethod() {
      System.out.println("-----> parsing <staticMethod>:");
      Token name = lex.getNextToken();
      errorCheck(name, "name");
      Token token = lex.getNextToken();
      errorCheck(token, "single", "(");
      token = lex.getNextToken();
      if (token.matches("single", ")")) { //no params
         token = lex.getNextToken();
         errorCheck(token, "single", "{");
         Node first = parseStatements();
         return new Node("staticMethod", name.getDetails(), first, null, null);
      }
      else { //has params
         lex.putBackToken(token);
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck(token, "single", "{");
         Node second = parseStatements();
         return new Node("staticMethod", name.getDetails(), first, second, null);
      }
   }

   public Node parseInstanceField() {
      System.out.println("-----> parsing <instanceField>:");
      Token name = lex.getNextToken();
      errorCheck(name, "name");
      return new Node("instanceField", name.getDetails(), null, null, null);
   }

   public Node parseConstructor() {
      System.out.println("-----> parsing <constructor>:");
      Token name = lex.getNextToken();
      errorCheck(name, "className");
      Token token = lex.getNextToken();
      errorCheck(token, "single", "(");
      token = lex.getNextToken();
      if(token.matches("single",")")) { // no params
         token = lex.getNextToken();
         errorCheck(token, "single", "{");
         token = lex.getNextToken();
         if(token.matches("single", "}")) { // no statements
            return new Node("constructor", name.getDetails(), null, null, null);
         }
         else { //have statements
            lex.putBackToken(token);
            Node second = parseStatements();
            token = lex.getNextToken();
            lex.putBackToken(token);
            errorCheck(token, "single", "}");
            return new Node("constructor", name.getDetails(), null, second, null);
         }
      }
      else { // have params
         lex.putBackToken(token);
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck(token, "single", ")");
         token = lex.getNextToken();
         errorCheck(token, "single", "{");
         token = lex.getNextToken();
         if(token.matches("single", "}")) { // no statements
            return new Node("constructor", name.getDetails(), first, null, null);
         }
         else { // have statements
            lex.putBackToken(token);
            Node second = parseStatements();
            token = lex.getNextToken();
            errorCheck(token, "single", "}" );
            return new Node("funcDef", name.getDetails(), first, second, null );
         }
      }
   }

   public Node parseInstanceMethod() {
      System.out.println("-----> parsing <instanceMethod>:");
      return null;
   }

   public Node parseRestOfMethod() {
      System.out.println("-----> parsing <restOfMethod>");
      return null;
   }

   public Node parseMethodBody() {
      System.out.println("-----> parsing <methodBody>");
      return null;
   }

   public Node parseParams() {
      System.out.println("-----> parsing <params>:");
      return null;
   }

   public Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
      return null;
   }

   public Node parseStatement() {
      System.out.println("-----> parsing <statement>:");
      return null;
   }

   public Node parseWhileStatement() {
      System.out.println("----> parsing <whileStatement>");
      return null;
   }

   public Node parseExpression() {
      System.out.println("-----> parsing <expression>:");
      return null;
   }

   public Node parseCall() {
      System.out.println("-----> parsing <call>:");
      return null;
   }

   public Node parseCaller() {
      System.out.println("-----> parsing <caller>:");
      return null;
   }

   public Node parseArgs() {
      System.out.println("-----> parsing <args>:");
      return null;
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
