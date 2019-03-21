
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
      Node first = parseRestOfMethod();
      return new Node("staticMethod", name.getDetails(), first, null, null);
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
      Node first = parseRestOfMethod();
      return new Node("constructor", name.getDetails(), first, null, null);
   }

   public Node parseInstanceMethod() {
      System.out.println("-----> parsing <instanceMethod>:");
      Token name = lex.getNextToken();
      errorCheck(name, "name");
      Node first = parseRestOfMethod();
      return new Node("instanceMethod", name.getDetails(), first, null, null);
   }

   public Node parseRestOfMethod() {
      System.out.println("-----> parsing <restOfMethod>");
      Token token = lex.getNextToken();
      errorCheck(token, "single", "(");
      token = lex.getNextToken();
      if(token.matches("single",")")) { // no params
         Node first = parseMethodBody();
         return new Node("restOfMethod", first, null, null);
      }
      else { // have params
         lex.putBackToken(token);
         Node first = parseParams();
         Node second = parseMethodBody();
         return new Node("restOfMethod", first, second, null);
      }
   }

   public Node parseParams() {
      System.out.println("-----> parsing <params>:");
      Token name = lex.getNextToken();
      errorCheck(name, "name");
      Token token = lex.getNextToken();
      if(token.matches("single", ",")){ // have more params
         Node first = parseParams();
         return new Node("params", name.getDetails(), first, null, null);
      }
      else { // no more params
         errorCheck(token, "single", ")");
         return new Node("params", name.getDetails(), null, null, null);
      }
   }

   public Node parseMethodBody() {
      System.out.println("-----> parsing <methodBody>");
      Token token = lex.getNextToken();
      errorCheck(token, "{");
      token = lex.getNextToken();
      if(token.matches("single", "}")){ // no statements
         return new Node("methodBody", null, null, null);
      }
      else { // have statements
         lex.putBackToken(token);
         Node first = parseStatements();
         return new Node("methodBody", first, null, null);
      }
   }

   public Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
      Node first = parseStatement();
      Token token = lex.getNextToken();
      if(token.matches("single", "}")){ // no more statements
         return new Node("statements", first, null, null);
      }
      else {
         lex.putBackToken(token);
         Node second = parseStatements();
         return new Node("statements", first, second, null);
      }
   }

   public Node parseStatement() {
      System.out.println("-----> parsing <statement>:");
      Token token = lex.getNextToken();

      if(token.isKind("while")) { // whileStatement
         lex.putBackToken(token);
         Node first = parseWhileStatement();
         return new Node("statement", first, null, null);
      }
      else if(token.isKind("if")) { // ifStatement
         lex.putBackToken(token);
         Node first = parseIfStatement();
         return new Node("statement", first, null, null);
      }
      else if(token.isKind("return")) { // RETURN <expression>
         lex.putBackToken(token);
      }
      else if(token.isKind("name")) {
         Token name = token;
         Token equals = lex.getNextToken();
         errorCheck(equals,"=");
         Node first = parseRHS();
         return new Node("statement", name.getDetails(), first,null, null);

      }

      else if(token.isKind("new")) {
         Token className = lex.getNextToken();
         errorCheck(className, "className");
         Token single = lex.getNextToken();
         errorCheck(single, "single", "(");
         Token args = lex.getNextToken();
         if(args.getDetails()==")"){
            return new Node("statement", null, null, null);
         }
         else{
            lex.putBackToken(args);
            Node first = parseArgs();
            return new Node("statement", first, null,null);
         }

      }
      return null; // needs removed when finished
   }

   public Node parseWhileStatement() {
      System.out.println("----> parsing <whileStatement>");
      return null;
   }

   public Node parseIfStatement() {
      System.out.println("----> parsing <ifStatement>");
      return null;
   }

   public Node parseLoopBody() {
      System.out.println("----> parsing <loopBody>");
      return null;
   }

   public Node parseExpression() {
      System.out.println("-----> parsing <expression>:");
      return null;
   }

   public Node parseRefChain() {
      System.out.println("-----> parsing <refChain>:");
      return null;
   }

   public Node parseCaller() {
      System.out.println("-----> parsing <caller>:");
      return null;
   }

   public Node parseArgsPart() {
       System.out.println("-----> parsing <argsPart>:");
       Token token = lex.getNextToken();
       errorCheck(token, "single", "(");
       token = lex.getNextToken();
       if(token.matches("single",")")) { // no params     
          Node first = parseArgsPart();
          return new Node("parseArgsPart", first, null, null);
          }
       else{
          errorCheck(token, "name", "args");
          lex.putBackToken(token);
          Node first = parseArgs();
          return new Node("argsPart", first, null, null);

          }
    }

   public Node parseArgs() {
      System.out.println("-----> parsing <args>:");
      return null;
   }

   public Node parseRHS() {
      System.out.println("-----> parsing <rhs>:");
      Token token = lex.getNextToken();
      if(!token.isKind("new")){  //if the RHS is an expression
         lex.putBackToken(token);
         Node first = parseExpression();
         return new Node("statement", first, null, null);
      }
      errorCheck(token, "new"); //if the RHS is a class declaration
      Token className = lex.getNextToken();
      errorCheck(className, "className");
      Token single = lex.getNextToken();
      errorCheck(single, "single", "(");
      Token args = lex.getNextToken();
      if(args.getDetails()==")"){
         return new Node("statement", null, null, null);
      }
      else{
         lex.putBackToken(args);
         Node first = parseArgs();
         return new Node("statement", first, null,null);
      }
      //return null;
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
