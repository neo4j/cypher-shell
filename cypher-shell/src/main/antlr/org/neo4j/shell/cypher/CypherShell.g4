grammar CypherShell;

@header {
package org.neo4j.shell.cypher;
}

cypherScript : ( cypher )+ EOF ;

cypher : ws queryOptions statement ( ws ';' )? ws ;

queryOptions : ( anyCypherOption ws )* ;

anyCypherOption : cypherOption
                | explain
                | profile
                ;

cypherOption : CYPHER ( sp versionNumber )? ( sp configurationOption )* ;

versionNumber : DecimalInteger '.' DecimalInteger ;

explain : EXPLAIN ;

profile : PROFILE ;

configurationOption : symbolicName ws '=' ws symbolicName ;

statement : command
          | query
          ;

query : regularQuery
      | bulkImportQuery
      ;

regularQuery : singleQuery ( ws union )* ;

bulkImportQuery : periodicCommitHint ws loadCSVQuery ;

singleQuery : ( ( clause ws )* returnClause )
            | ( clause ( ws clause )* )
            ;

periodicCommitHint : USING sp PERIODIC sp COMMIT ( sp )? ;

loadCSVQuery : loadCSV ( ws clause )* returnClause? ;

union : ( UNION sp ALL singleQuery )
      | ( UNION singleQuery )
      ;

clause : loadCSV
       | start
       | match
       | unwind
       | merge
       | create
       | createUnique
       | set
       | delete
       | remove
       | foreach
       | with
       ;

command : createIndex
        | dropIndex
        | createUniqueConstraint
        | dropUniqueConstraint
        | createNodePropertyExistenceConstraint
        | dropNodePropertyExistenceConstraint
        | createRelationshipPropertyExistenceConstraint
        | dropRelationshipPropertyExistenceConstraint
        ;

createUniqueConstraint : CREATE sp uniqueConstraint ;

createNodePropertyExistenceConstraint : CREATE sp nodePropertyExistenceConstraint ;

createRelationshipPropertyExistenceConstraint : CREATE sp relationshipPropertyExistenceConstraint ;

createIndex : CREATE sp index ;

dropUniqueConstraint : DROP sp uniqueConstraint ;

dropNodePropertyExistenceConstraint : DROP sp nodePropertyExistenceConstraint ;

dropRelationshipPropertyExistenceConstraint : DROP sp relationshipPropertyExistenceConstraint ;

dropIndex : DROP sp index ;

index : INDEX sp ON ws nodeLabel '(' propertyKeyName ')' ;

uniqueConstraint : CONSTRAINT sp ON ws '(' variable nodeLabel ')' ws ASSERT sp propertyExpression sp IS sp UNIQUE ;

nodePropertyExistenceConstraint : CONSTRAINT sp ON ws '(' variable nodeLabel ')' ws ASSERT sp EXISTS ws '(' propertyExpression ')' ;

relationshipPropertyExistenceConstraint : CONSTRAINT sp ON ws relationshipPatternSyntax ws ASSERT sp EXISTS ws '(' propertyExpression ')' ;

relationshipPatternSyntax : ( '(' ws ')' dash '[' variable relType ']' dash '(' ws ')' )
                          | ( '(' ws ')' dash '[' variable relType ']' dash rightArrowHead '(' ws ')' )
                          | ( '(' ws ')' leftArrowHead dash '[' variable relType ']' dash '(' ws ')' )
                          ;

loadCSV : LOAD sp CSV sp ( WITH sp HEADERS sp )? FROM sp expression sp AS sp variable sp ( FIELDTERMINATOR sp StringLiteral )? ;

match : ( OPTIONAL sp )? MATCH ws pattern ( hint )* ( ws where )? ;

unwind : UNWIND ws expression sp AS sp variable ;

merge : MERGE ws patternPart ( sp mergeAction )* ;

mergeAction : ( ON sp MATCH sp set )
            | ( ON sp CREATE sp set )
            ;

create : CREATE ws pattern ;

createUnique : CREATE sp UNIQUE ws pattern ;

set : SET ws setItem ( ws ',' ws setItem )* ;

setItem : ( propertyExpression ws '=' ws expression )
        | ( variable ws '=' ws expression )
        | ( variable ws '+=' ws expression )
        | ( variable ws nodeLabels )
        ;

delete : ( DELETE expression ( ',' expression )* )
       | ( DETACH sp DELETE expression ( ',' expression )* )
       ;

remove : REMOVE sp removeItem ( ws ',' ws removeItem )* ;

removeItem : ( variable nodeLabels )
           | propertyExpression
           ;

foreach : FOREACH ws '(' ws variable sp IN sp expression ws '|' ( sp clause )+ ws ')' ;

with : ( WITH DISTINCT sp returnBody where? )
     | ( WITH sp returnBody where? )
     ;

returnClause : ( RETURN sp DISTINCT sp returnBody )
             | ( RETURN sp returnBody )
             ;

returnBody : returnItems ( sp order )? ( sp skip )? ( sp limit )? ;

returnItems : ( '*' ( ws ',' ws returnItem )* )
            | ( returnItem ( ws ',' ws returnItem )* )
            ;

returnItem : ( expression sp AS sp variable )
           | expression
           ;

order : ORDER sp BY sp sortItem ( ',' ws sortItem )* ;

skip : L_SKIP sp expression ;

limit : LIMIT sp expression ;

sortItem : ( expression ws ( DESCENDING | DESC | ASCENDING | ASC )? );

hint : ws ( ( USING sp INDEX sp variable nodeLabel '(' propertyKeyName ')' ) | ( USING sp JOIN sp ON sp variable ( ws ',' ws variable )* ) | ( USING sp SCAN sp variable nodeLabel ) ) ;

start : START sp startPoint ( ws ',' ws startPoint )* where? ;

startPoint : variable ws '=' ws lookup ;

lookup : nodeLookup
       | relationshipLookup
       ;

nodeLookup : NODE ( identifiedIndexLookup | indexQuery | idLookup ) ;

relationshipLookup : ( RELATIONSHIP | REL ) ( identifiedIndexLookup | indexQuery | idLookup ) ;

identifiedIndexLookup : ':' symbolicName '(' symbolicName '=' ( StringLiteral | legacyParameter ) ')' ;

indexQuery : ':' symbolicName '(' ( StringLiteral | legacyParameter ) ')' ;

idLookup : '(' ( literalIds | legacyParameter | '*' ) ')' ;

literalIds : ( ws ',' ws )* ;

where : WHERE sp expression ;

pattern : patternPart ( ',' patternPart )* ;

patternPart : ( variable ws '=' ws anonymousPatternPart )
            | anonymousPatternPart
            ;

anonymousPatternPart : shortestPathPattern
                     | patternElement
                     ;

shortestPathPattern : ( SHORTESTPATH '(' patternElement ')' )
                    | ( ALLSHORTESTPATHS '(' patternElement ')' )
                    ;

patternElement : ( nodePattern ( ws patternElementChain )* )
               | ( '(' patternElement ')' )
               ;

nodePattern : '(' ws ( variable ws )? ( nodeLabels ws )? ( properties ws )? ')' ;

patternElementChain : relationshipPattern ws nodePattern ;

relationshipPattern : ( leftArrowHead ws dash ws relationshipDetail? ws dash ws rightArrowHead )
                    | ( leftArrowHead ws dash ws relationshipDetail? ws dash )
                    | ( dash ws relationshipDetail? ws dash ws rightArrowHead )
                    | ( dash ws relationshipDetail? ws dash )
                    ;

relationshipDetail : '[' ws variable? '?'? ( relationshipTypes ws )? ( '*' rangeLiteral ws )?  ( properties ws )? ']' ;

properties : mapLiteral
           | parameter
           | legacyParameter
           ;

relType : ':' relTypeName ;

relationshipTypes : ':' relTypeName ( ws '|' ':'? ws relTypeName )* ;

nodeLabels : nodeLabel ( ws nodeLabel )* ;

nodeLabel : ':' labelName ;

rangeLiteral : ws ( integerLiteral ws )? ( '..' ws ( integerLiteral ws )? )? ;

labelName : symbolicName ;

relTypeName : symbolicName ;

expression : expression12 ;

expression12 : expression11 ( sp OR sp expression11 )* ;

expression11 : expression10 ( sp XOR sp expression10 )* ;

expression10 : expression9 ( sp AND sp expression9 )* ;

expression9 : ( sp NOT sp )* expression8 ;

expression8 : expression7 ( ws partialComparisonExpression )* ;

expression7 : expression6 ( ( ws '+' ws expression6 ) | ( ws '-' ws expression6 ) )* ;

expression6 : expression5 ( ( ws '*' ws expression5 ) | ( ws '/' ws expression5 ) | ( ws '%' ws expression5 ) )* ;

expression5 : expression4 ( ws '^' ws expression4 )* ;

expression4 : ( ( '+' | '-' ) ws )* expression3 ;

expression3 : expression2 ( ( ws '[' expression ']' ) | ( ws '[' expression? '..' expression? ']' ) | ( ( ( ws '=~' ) | ( sp IN ) | ( sp STARTS sp WITH ) | ( sp ENDS sp WITH ) | ( sp CONTAINS ) ) ws expression2 ) | ( sp IS sp NULL ) | ( sp IS sp NOT sp NULL ) )* ;

expression2 : atom ( propertyLookup | nodeLabels )* ;

atom : numberLiteral
     | StringLiteral
     | parameter
     | legacyParameter
     | TRUE
     | FALSE
     | NULL
     | caseExpression
     | ( COUNT '(' '*' ')' )
     | mapLiteral
     | listComprehension
     | ( '[' ws expression ws ( ',' ws expression ws )* ']' )
     | ( FILTER ws '(' ws filterExpression ws ')' )
     | ( EXTRACT ws '(' ws filterExpression ws ( ws '|' expression )? ')' )
     | reduce
     | ( ALL ws '(' ws filterExpression ws ')' )
     | ( ANY ws '(' ws filterExpression ws ')' )
     | ( NONE ws '(' ws filterExpression ws ')' )
     | ( SINGLE ws '(' ws filterExpression ws ')' )
     | shortestPathPattern
     | relationshipsPattern
     | parenthesizedExpression
     | functionInvocation
     | variable
     ;

reduce : REDUCE ws '(' variable '=' expression ',' idInColl '|' expression ')' ;

partialComparisonExpression : ( '=' ws expression7 )
                            | ( '<>' ws expression7 )
                            | ( '!=' ws expression7 )
                            | ( '<' ws expression7 )
                            | ( '>' ws expression7 )
                            | ( '<=' ws expression7 )
                            | ( '>=' ws expression7 )
                            ;

parenthesizedExpression : '(' ws expression ws ')' ;

relationshipsPattern : nodePattern ( ws patternElementChain )+ ;

filterExpression : idInColl ( ws where )? ;

idInColl : variable sp IN sp expression ;

functionInvocation : functionName ws '(' ws DISTINCT? ( expression ( ',' ws expression )* )? ws ')' ;

functionName : symbolicName ;

listComprehension : '[' filterExpression ( ws '|' expression )? ']' ;

propertyLookup : ws '.' ws ( ( propertyKeyName ( '?' | '!' ) ) | propertyKeyName ) ;

caseExpression : ( ( CASE ( ws caseAlternatives )+ ) | ( CASE expression ( ws caseAlternatives )+ ) ) ( ws ELSE ws expression )? ws END ;

caseAlternatives : WHEN ws expression ws THEN ws expression ;

variable : symbolicName ;

StringLiteral : ( '"' ( StringLiteral_0 | EscapedChar )* '"' )
              | ( '\'' ( StringLiteral_1 | EscapedChar )* '\'' )
              ;

EscapedChar : '\\' ( '\\' | '\'' | '"' | ( 'B' | 'b' ) | ( 'F' | 'f' ) | ( 'N' | 'n' ) | ( 'R' | 'r' ) | ( 'T' | 't' ) | '_' | '%' | ( ( 'U' | 'u' ) ( HexDigit HexDigit HexDigit HexDigit ) ) | ( ( 'U' | 'u' ) ( HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit ) ) ) ;

numberLiteral : doubleLiteral
              | integerLiteral
              ;

mapLiteral : '{' ws ( propertyKeyName ws ':' ws expression ws ( ',' ws propertyKeyName ws ':' ws expression ws )* )? '}' ;

legacyParameter : '{' ws ( symbolicName | DecimalInteger ) ws '}' ;

parameter : '$' ( symbolicName | DecimalInteger ) ;

propertyExpression : atom ( ws propertyLookup )+ ;

propertyKeyName : symbolicName ;

integerLiteral : HexInteger
               | OctalInteger
               | DecimalInteger
               ;

HexInteger : L_0X HexString ;

DecimalInteger : ( ( '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ) DigitString? )
               | '0'
               ;

OctalInteger : '0' OctalString ;

HexString : ( HexDigit )+ ;

DigitString : ( Digit )+ ;

OctalString : ( OctDigit )+ ;

HexDigit : '0'
         | '1'
         | '2'
         | '3'
         | '4'
         | '5'
         | '6'
         | '7'
         | '8'
         | '9'
         | ( 'A' | 'a' )
         | ( 'B' | 'b' )
         | ( 'C' | 'c' )
         | ( 'D' | 'd' )
         | ( 'E' | 'e' )
         | ( 'F' | 'f' )
         ;

Digit : '0'
      | '1'
      | '2'
      | '3'
      | '4'
      | '5'
      | '6'
      | '7'
      | '8'
      | '9'
      ;

OctDigit : '0'
         | '1'
         | '2'
         | '3'
         | '4'
         | '5'
         | '6'
         | '7'
         ;

doubleLiteral : exponentDecimalReal
              | regularDecimalReal
              ;

exponentDecimalReal : ( ( Digit | '.' )+ | DecimalInteger ) ( ( 'E' | 'e' ) | ( 'E' | 'e' ) ) ( DigitString | DecimalInteger ) ;

regularDecimalReal : ( ( Digit )* | DecimalInteger ) '.' ( DigitString | DecimalInteger ) ;

symbolicName : UnescapedSymbolicName
             | EscapedSymbolicName
             | CYPHER
             | EXPLAIN
             | PROFILE
             | USING
             | PERIODIC
             | COMMIT
             | UNION
             | ALL
             | CREATE
             | DROP
             | INDEX
             | ON
             | CONSTRAINT
             | ASSERT
             | IS
             | UNIQUE
             | EXISTS
             | LOAD
             | CSV
             | WITH
             | HEADERS
             | FROM
             | AS
             | FIELDTERMINATOR
             | OPTIONAL
             | MATCH
             | UNWIND
             | MERGE
             | SET
             | DELETE
             | DETACH
             | REMOVE
             | FOREACH
             | IN
             | DISTINCT
             | RETURN
             | ORDER
             | BY
             | L_SKIP
             | LIMIT
             | DESCENDING
             | DESC
             | ASCENDING
             | ASC
             | JOIN
             | SCAN
             | START
             | NODE
             | RELATIONSHIP
             | REL
             | WHERE
             | SHORTESTPATH
             | ALLSHORTESTPATHS
             | OR
             | XOR
             | AND
             | NOT
             | STARTS
             | ENDS
             | CONTAINS
             | NULL
             | TRUE
             | FALSE
             | COUNT
             | FILTER
             | EXTRACT
             | ANY
             | NONE
             | SINGLE
             | REDUCE
             | CASE
             | ELSE
             | END
             | WHEN
             | THEN
             | HexString
             ;

CYPHER : ( 'C' | 'c' ) ( 'Y' | 'y' ) ( 'P' | 'p' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;

EXPLAIN : ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'P' | 'p' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' )  ;

PROFILE : ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'E' | 'e' )  ;

USING : ( 'U' | 'u' ) ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )  ;

PERIODIC : ( 'P' | 'p' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'C' | 'c' )  ;

COMMIT : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'M' | 'm' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' )  ;

UNION : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' )  ;

ALL : ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )  ;

CREATE : ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

DROP : ( 'D' | 'd' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'P' | 'p' )  ;

INDEX : ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'X' | 'x' )  ;

ON : ( 'O' | 'o' ) ( 'N' | 'n' )  ;

CONSTRAINT : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' )  ;

ASSERT : ( 'A' | 'a' ) ( 'S' | 's' ) ( 'S' | 's' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'T' | 't' )  ;

IS : ( 'I' | 'i' ) ( 'S' | 's' )  ;

UNIQUE : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'Q' | 'q' ) ( 'U' | 'u' ) ( 'E' | 'e' )  ;

EXISTS : ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'S' | 's' )  ;

LOAD : ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'A' | 'a' ) ( 'D' | 'd' )  ;

CSV : ( 'C' | 'c' ) ( 'S' | 's' ) ( 'V' | 'v' )  ;

WITH : ( 'W' | 'w' ) ( 'I' | 'i' ) ( 'T' | 't' ) ( 'H' | 'h' )  ;

HEADERS : ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'S' | 's' )  ;

FROM : ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )  ;

AS : ( 'A' | 'a' ) ( 'S' | 's' )  ;

FIELDTERMINATOR : ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'D' | 'd' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'O' | 'o' ) ( 'R' | 'r' )  ;

OPTIONAL : ( 'O' | 'o' ) ( 'P' | 'p' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'L' | 'l' )  ;

MATCH : ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'C' | 'c' ) ( 'H' | 'h' )  ;

UNWIND : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'W' | 'w' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'D' | 'd' )  ;

MERGE : ( 'M' | 'm' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'G' | 'g' ) ( 'E' | 'e' )  ;

SET : ( 'S' | 's' ) ( 'E' | 'e' ) ( 'T' | 't' )  ;

DELETE : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

DETACH : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'C' | 'c' ) ( 'H' | 'h' )  ;

REMOVE : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'O' | 'o' ) ( 'V' | 'v' ) ( 'E' | 'e' )  ;

FOREACH : ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'C' | 'c' ) ( 'H' | 'h' )  ;

IN : ( 'I' | 'i' ) ( 'N' | 'n' )  ;

DISTINCT : ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' )  ;

RETURN : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'U' | 'u' ) ( 'R' | 'r' ) ( 'N' | 'n' )  ;

ORDER : ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;

BY : ( 'B' | 'b' ) ( 'Y' | 'y' )  ;

L_SKIP : ( 'S' | 's' ) ( 'K' | 'k' ) ( 'I' | 'i' ) ( 'P' | 'p' )  ;

LIMIT : ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' )  ;

DESCENDING : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )  ;

DESC : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' )  ;

ASCENDING : ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )  ;

ASC : ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' )  ;

JOIN : ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )  ;

SCAN : ( 'S' | 's' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'N' | 'n' )  ;

START : ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'T' | 't' )  ;

NODE : ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'D' | 'd' ) ( 'E' | 'e' )  ;

RELATIONSHIP : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'S' | 's' ) ( 'H' | 'h' ) ( 'I' | 'i' ) ( 'P' | 'p' )  ;

REL : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'L' | 'l' )  ;

WHERE : ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )  ;

SHORTESTPATH : ( 'S' | 's' ) ( 'H' | 'h' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'P' | 'p' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'H' | 'h' )  ;

ALLSHORTESTPATHS : ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'H' | 'h' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'P' | 'p' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'H' | 'h' ) ( 'S' | 's' )  ;

OR : ( 'O' | 'o' ) ( 'R' | 'r' )  ;

XOR : ( 'X' | 'x' ) ( 'O' | 'o' ) ( 'R' | 'r' )  ;

AND : ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )  ;

NOT : ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )  ;

STARTS : ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'S' | 's' )  ;

ENDS : ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ( 'S' | 's' )  ;

CONTAINS : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' )  ;

NULL : ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )  ;

TRUE : ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )  ;

FALSE : ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )  ;

COUNT : ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'T' | 't' )  ;

FILTER : ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;

EXTRACT : ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'T' | 't' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'C' | 'c' ) ( 'T' | 't' )  ;

ANY : ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' )  ;

NONE : ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'E' | 'e' )  ;

SINGLE : ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' ) ( 'L' | 'l' ) ( 'E' | 'e' )  ;

REDUCE : ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'D' | 'd' ) ( 'U' | 'u' ) ( 'C' | 'c' ) ( 'E' | 'e' )  ;

CASE : ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )  ;

ELSE : ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )  ;

END : ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'D' | 'd' )  ;

WHEN : ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'N' | 'n' )  ;

THEN : ( 'T' | 't' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'N' | 'n' )  ;

UnescapedSymbolicName : IdentifierStart ( IdentifierPart )* ;

/**
 * Based on the unicode identifier and pattern syntax
 *   (http://www.unicode.org/reports/tr31/)
 * And extended with a few characters.
 */
IdentifierStart : ID_Start
                | '_'
                | '‿'
                | '⁀'
                | '⁔'
                | '︳'
                | '︴'
                | '﹍'
                | '﹎'
                | '﹏'
                | '＿'
                ;

/**
 * Based on the unicode identifier and pattern syntax
 *   (http://www.unicode.org/reports/tr31/)
 * And extended with a few characters.
 */
IdentifierPart : ID_Continue
               | Sc
               ;

/**
 * Any character except "`", enclosed within `backticks`. Backticks are escaped with double backticks. */
EscapedSymbolicName : ( '`' ( EscapedSymbolicName_0 )* '`' )+ ;

ws : ( WHITESPACE )* ;

sp : ( WHITESPACE )+ ;

WHITESPACE : SPACE
           | TAB
           | LF
           | VT
           | FF
           | CR
           | FS
           | GS
           | RS
           | US
           | ' '
           | '᠎'
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | ' '
           | '　'
           | ' '
           | ' '
           | ' '
           | COMMENT
           | BLOCKCOMMENT
           ;

BLOCKCOMMENT : '/*' .*? '*/' -> skip ;
COMMENT : '//' ~[\r\n]* '\r'? '\n' -> skip ;
//Comment : ( '/*' ( Comment_0 | ( '*' Comment_1 ) )* '*/' )
//        | ( '//' Comment_2 CR? ( LF | EOF ) )
//        ;

leftArrowHead : '<'
              | '⟨'
              | '〈'
              | '﹤'
              | '＜'
              ;

rightArrowHead : '>'
               | '⟩'
               | '〉'
               | '﹥'
               | '＞'
               ;

dash : '-'
     | '­'
     | '‐'
     | '‑'
     | '‒'
     | '–'
     | '—'
     | '―'
     | '−'
     | '﹘'
     | '﹣'
     | '－'
     ;


digit : Digit;
L_0X : ( '0' | '0' ) ( 'X' | 'x' )  ;

fragment FF : [\f] ;

fragment EscapedSymbolicName_0 : [\u0000-_a-\uFFFF] ;

fragment RS : [\u001E] ;

fragment ID_Continue : [0-9A-Z_a-z\u00AA\u00B5\u00B7\u00BA\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02C1\u02C6-\u02D1\u02E0-\u02E4\u02EC\u02EE\u0300-\u0374\u0376-\u0377\u037A-\u037D\u0386-\u038A\u038C\u038E-\u03A1\u03A3-\u03F5\u03F7-\u0481\u0483-\u0487\u048A-\u0527\u0531-\u0556\u0559\u0561-\u0587\u0591-\u05BD\u05BF\u05C1-\u05C2\u05C4-\u05C5\u05C7\u05D0-\u05EA\u05F0-\u05F2\u0610-\u061A\u0620-\u0669\u066E-\u06D3\u06D5-\u06DC\u06DF-\u06E8\u06EA-\u06FC\u06FF\u0710-\u074A\u074D-\u07B1\u07C0-\u07F5\u07FA\u0800-\u082D\u0840-\u085B\u08A0\u08A2-\u08AC\u08E4-\u08FE\u0900-\u0963\u0966-\u096F\u0971-\u0977\u0979-\u097F\u0981-\u0983\u0985-\u098C\u098F-\u0990\u0993-\u09A8\u09AA-\u09B0\u09B2\u09B6-\u09B9\u09BC-\u09C4\u09C7-\u09C8\u09CB-\u09CE\u09D7\u09DC-\u09DD\u09DF-\u09E3\u09E6-\u09F1\u0A01-\u0A03\u0A05-\u0A0A\u0A0F-\u0A10\u0A13-\u0A28\u0A2A-\u0A30\u0A32-\u0A33\u0A35-\u0A36\u0A38-\u0A39\u0A3C\u0A3E-\u0A42\u0A47-\u0A48\u0A4B-\u0A4D\u0A51\u0A59-\u0A5C\u0A5E\u0A66-\u0A75\u0A81-\u0A83\u0A85-\u0A8D\u0A8F-\u0A91\u0A93-\u0AA8\u0AAA-\u0AB0\u0AB2-\u0AB3\u0AB5-\u0AB9\u0ABC-\u0AC5\u0AC7-\u0AC9\u0ACB-\u0ACD\u0AD0\u0AE0-\u0AE3\u0AE6-\u0AEF\u0B01-\u0B03\u0B05-\u0B0C\u0B0F-\u0B10\u0B13-\u0B28\u0B2A-\u0B30\u0B32-\u0B33\u0B35-\u0B39\u0B3C-\u0B44\u0B47-\u0B48\u0B4B-\u0B4D\u0B56-\u0B57\u0B5C-\u0B5D\u0B5F-\u0B63\u0B66-\u0B6F\u0B71\u0B82-\u0B83\u0B85-\u0B8A\u0B8E-\u0B90\u0B92-\u0B95\u0B99-\u0B9A\u0B9C\u0B9E-\u0B9F\u0BA3-\u0BA4\u0BA8-\u0BAA\u0BAE-\u0BB9\u0BBE-\u0BC2\u0BC6-\u0BC8\u0BCA-\u0BCD\u0BD0\u0BD7\u0BE6-\u0BEF\u0C01-\u0C03\u0C05-\u0C0C\u0C0E-\u0C10\u0C12-\u0C28\u0C2A-\u0C33\u0C35-\u0C39\u0C3D-\u0C44\u0C46-\u0C48\u0C4A-\u0C4D\u0C55-\u0C56\u0C58-\u0C59\u0C60-\u0C63\u0C66-\u0C6F\u0C82-\u0C83\u0C85-\u0C8C\u0C8E-\u0C90\u0C92-\u0CA8\u0CAA-\u0CB3\u0CB5-\u0CB9\u0CBC-\u0CC4\u0CC6-\u0CC8\u0CCA-\u0CCD\u0CD5-\u0CD6\u0CDE\u0CE0-\u0CE3\u0CE6-\u0CEF\u0CF1-\u0CF2\u0D02-\u0D03\u0D05-\u0D0C\u0D0E-\u0D10\u0D12-\u0D3A\u0D3D-\u0D44\u0D46-\u0D48\u0D4A-\u0D4E\u0D57\u0D60-\u0D63\u0D66-\u0D6F\u0D7A-\u0D7F\u0D82-\u0D83\u0D85-\u0D96\u0D9A-\u0DB1\u0DB3-\u0DBB\u0DBD\u0DC0-\u0DC6\u0DCA\u0DCF-\u0DD4\u0DD6\u0DD8-\u0DDF\u0DF2-\u0DF3\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E59\u0E81-\u0E82\u0E84\u0E87-\u0E88\u0E8A\u0E8D\u0E94-\u0E97\u0E99-\u0E9F\u0EA1-\u0EA3\u0EA5\u0EA7\u0EAA-\u0EAB\u0EAD-\u0EB9\u0EBB-\u0EBD\u0EC0-\u0EC4\u0EC6\u0EC8-\u0ECD\u0ED0-\u0ED9\u0EDC-\u0EDF\u0F00\u0F18-\u0F19\u0F20-\u0F29\u0F35\u0F37\u0F39\u0F3E-\u0F47\u0F49-\u0F6C\u0F71-\u0F84\u0F86-\u0F97\u0F99-\u0FBC\u0FC6\u1000-\u1049\u1050-\u109D\u10A0-\u10C5\u10C7\u10CD\u10D0-\u10FA\u10FC-\u1248\u124A-\u124D\u1250-\u1256\u1258\u125A-\u125D\u1260-\u1288\u128A-\u128D\u1290-\u12B0\u12B2-\u12B5\u12B8-\u12BE\u12C0\u12C2-\u12C5\u12C8-\u12D6\u12D8-\u1310\u1312-\u1315\u1318-\u135A\u135D-\u135F\u1369-\u1371\u1380-\u138F\u13A0-\u13F4\u1401-\u166C\u166F-\u167F\u1681-\u169A\u16A0-\u16EA\u16EE-\u16F0\u1700-\u170C\u170E-\u1714\u1720-\u1734\u1740-\u1753\u1760-\u176C\u176E-\u1770\u1772-\u1773\u1780-\u17D3\u17D7\u17DC-\u17DD\u17E0-\u17E9\u180B-\u180D\u1810-\u1819\u1820-\u1877\u1880-\u18AA\u18B0-\u18F5\u1900-\u191C\u1920-\u192B\u1930-\u193B\u1946-\u196D\u1970-\u1974\u1980-\u19AB\u19B0-\u19C9\u19D0-\u19DA\u1A00-\u1A1B\u1A20-\u1A5E\u1A60-\u1A7C\u1A7F-\u1A89\u1A90-\u1A99\u1AA7\u1B00-\u1B4B\u1B50-\u1B59\u1B6B-\u1B73\u1B80-\u1BF3\u1C00-\u1C37\u1C40-\u1C49\u1C4D-\u1C7D\u1CD0-\u1CD2\u1CD4-\u1CF6\u1D00-\u1DE6\u1DFC-\u1F15\u1F18-\u1F1D\u1F20-\u1F45\u1F48-\u1F4D\u1F50-\u1F57\u1F59\u1F5B\u1F5D\u1F5F-\u1F7D\u1F80-\u1FB4\u1FB6-\u1FBC\u1FBE\u1FC2-\u1FC4\u1FC6-\u1FCC\u1FD0-\u1FD3\u1FD6-\u1FDB\u1FE0-\u1FEC\u1FF2-\u1FF4\u1FF6-\u1FFC\u203F-\u2040\u2054\u2071\u207F\u2090-\u209C\u20D0-\u20DC\u20E1\u20E5-\u20F0\u2102\u2107\u210A-\u2113\u2115\u2118-\u211D\u2124\u2126\u2128\u212A-\u2139\u213C-\u213F\u2145-\u2149\u214E\u2160-\u2188\u2C00-\u2C2E\u2C30-\u2C5E\u2C60-\u2CE4\u2CEB-\u2CF3\u2D00-\u2D25\u2D27\u2D2D\u2D30-\u2D67\u2D6F\u2D7F-\u2D96\u2DA0-\u2DA6\u2DA8-\u2DAE\u2DB0-\u2DB6\u2DB8-\u2DBE\u2DC0-\u2DC6\u2DC8-\u2DCE\u2DD0-\u2DD6\u2DD8-\u2DDE\u2DE0-\u2DFF\u3005-\u3007\u3021-\u302F\u3031-\u3035\u3038-\u303C\u3041-\u3096\u3099-\u309F\u30A1-\u30FA\u30FC-\u30FF\u3105-\u312D\u3131-\u318E\u31A0-\u31BA\u31F0-\u31FF\u3400-\u4DB5\u4E00-\u9FCC\uA000-\uA48C\uA4D0-\uA4FD\uA500-\uA60C\uA610-\uA62B\uA640-\uA66F\uA674-\uA67D\uA67F-\uA697\uA69F-\uA6F1\uA717-\uA71F\uA722-\uA788\uA78B-\uA78E\uA790-\uA793\uA7A0-\uA7AA\uA7F8-\uA827\uA840-\uA873\uA880-\uA8C4\uA8D0-\uA8D9\uA8E0-\uA8F7\uA8FB\uA900-\uA92D\uA930-\uA953\uA960-\uA97C\uA980-\uA9C0\uA9CF-\uA9D9\uAA00-\uAA36\uAA40-\uAA4D\uAA50-\uAA59\uAA60-\uAA76\uAA7A-\uAA7B\uAA80-\uAAC2\uAADB-\uAADD\uAAE0-\uAAEF\uAAF2-\uAAF6\uAB01-\uAB06\uAB09-\uAB0E\uAB11-\uAB16\uAB20-\uAB26\uAB28-\uAB2E\uABC0-\uABEA\uABEC-\uABED\uABF0-\uABF9\uAC00-\uD7A3\uD7B0-\uD7C6\uD7CB-\uD7FB\uF900-\uFA6D\uFA70-\uFAD9\uFB00-\uFB06\uFB13-\uFB17\uFB1D-\uFB28\uFB2A-\uFB36\uFB38-\uFB3C\uFB3E\uFB40-\uFB41\uFB43-\uFB44\uFB46-\uFBB1\uFBD3-\uFD3D\uFD50-\uFD8F\uFD92-\uFDC7\uFDF0-\uFDFB\uFE00-\uFE0F\uFE20-\uFE26\uFE33-\uFE34\uFE4D-\uFE4F\uFE70-\uFE74\uFE76-\uFEFC\uFF10-\uFF19\uFF21-\uFF3A\uFF3F\uFF41-\uFF5A\uFF66-\uFFBE\uFFC2-\uFFC7\uFFCA-\uFFCF\uFFD2-\uFFD7\uFFDA-\uFFDC] ;

fragment Comment_1 : [\u0000-.0-\uFFFF] ;

fragment Comment_0 : [\u0000-)+-\uFFFF] ;

fragment StringLiteral_1 : [\u0000-&(-\[\]-\uFFFF] ;

fragment Comment_2 : [\u0000-\t\u000B-\f\u000E-\uFFFF] ;

fragment GS : [\u001D] ;

fragment FS : [\u001C] ;

fragment CR : [\r] ;

fragment Sc : [$\u00A2-\u00A5\u058F\u060B\u09F2-\u09F3\u09FB\u0AF1\u0BF9\u0E3F\u17DB\u20A0-\u20BA\uA838\uFDFC\uFE69\uFF04\uFFE0-\uFFE1\uFFE5-\uFFE6] ;

fragment SPACE : [ ] ;

fragment TAB : [\t] ;

fragment StringLiteral_0 : [\u0000-!#-\[\]-\uFFFF] ;

fragment LF : [\n] ;

fragment VT : [\u000B] ;

fragment US : [\u001F] ;

fragment ID_Start : [A-Za-z\u00AA\u00B5\u00BA\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02C1\u02C6-\u02D1\u02E0-\u02E4\u02EC\u02EE\u0370-\u0374\u0376-\u0377\u037A-\u037D\u0386\u0388-\u038A\u038C\u038E-\u03A1\u03A3-\u03F5\u03F7-\u0481\u048A-\u0527\u0531-\u0556\u0559\u0561-\u0587\u05D0-\u05EA\u05F0-\u05F2\u0620-\u064A\u066E-\u066F\u0671-\u06D3\u06D5\u06E5-\u06E6\u06EE-\u06EF\u06FA-\u06FC\u06FF\u0710\u0712-\u072F\u074D-\u07A5\u07B1\u07CA-\u07EA\u07F4-\u07F5\u07FA\u0800-\u0815\u081A\u0824\u0828\u0840-\u0858\u08A0\u08A2-\u08AC\u0904-\u0939\u093D\u0950\u0958-\u0961\u0971-\u0977\u0979-\u097F\u0985-\u098C\u098F-\u0990\u0993-\u09A8\u09AA-\u09B0\u09B2\u09B6-\u09B9\u09BD\u09CE\u09DC-\u09DD\u09DF-\u09E1\u09F0-\u09F1\u0A05-\u0A0A\u0A0F-\u0A10\u0A13-\u0A28\u0A2A-\u0A30\u0A32-\u0A33\u0A35-\u0A36\u0A38-\u0A39\u0A59-\u0A5C\u0A5E\u0A72-\u0A74\u0A85-\u0A8D\u0A8F-\u0A91\u0A93-\u0AA8\u0AAA-\u0AB0\u0AB2-\u0AB3\u0AB5-\u0AB9\u0ABD\u0AD0\u0AE0-\u0AE1\u0B05-\u0B0C\u0B0F-\u0B10\u0B13-\u0B28\u0B2A-\u0B30\u0B32-\u0B33\u0B35-\u0B39\u0B3D\u0B5C-\u0B5D\u0B5F-\u0B61\u0B71\u0B83\u0B85-\u0B8A\u0B8E-\u0B90\u0B92-\u0B95\u0B99-\u0B9A\u0B9C\u0B9E-\u0B9F\u0BA3-\u0BA4\u0BA8-\u0BAA\u0BAE-\u0BB9\u0BD0\u0C05-\u0C0C\u0C0E-\u0C10\u0C12-\u0C28\u0C2A-\u0C33\u0C35-\u0C39\u0C3D\u0C58-\u0C59\u0C60-\u0C61\u0C85-\u0C8C\u0C8E-\u0C90\u0C92-\u0CA8\u0CAA-\u0CB3\u0CB5-\u0CB9\u0CBD\u0CDE\u0CE0-\u0CE1\u0CF1-\u0CF2\u0D05-\u0D0C\u0D0E-\u0D10\u0D12-\u0D3A\u0D3D\u0D4E\u0D60-\u0D61\u0D7A-\u0D7F\u0D85-\u0D96\u0D9A-\u0DB1\u0DB3-\u0DBB\u0DBD\u0DC0-\u0DC6\u0E01-\u0E30\u0E32-\u0E33\u0E40-\u0E46\u0E81-\u0E82\u0E84\u0E87-\u0E88\u0E8A\u0E8D\u0E94-\u0E97\u0E99-\u0E9F\u0EA1-\u0EA3\u0EA5\u0EA7\u0EAA-\u0EAB\u0EAD-\u0EB0\u0EB2-\u0EB3\u0EBD\u0EC0-\u0EC4\u0EC6\u0EDC-\u0EDF\u0F00\u0F40-\u0F47\u0F49-\u0F6C\u0F88-\u0F8C\u1000-\u102A\u103F\u1050-\u1055\u105A-\u105D\u1061\u1065-\u1066\u106E-\u1070\u1075-\u1081\u108E\u10A0-\u10C5\u10C7\u10CD\u10D0-\u10FA\u10FC-\u1248\u124A-\u124D\u1250-\u1256\u1258\u125A-\u125D\u1260-\u1288\u128A-\u128D\u1290-\u12B0\u12B2-\u12B5\u12B8-\u12BE\u12C0\u12C2-\u12C5\u12C8-\u12D6\u12D8-\u1310\u1312-\u1315\u1318-\u135A\u1380-\u138F\u13A0-\u13F4\u1401-\u166C\u166F-\u167F\u1681-\u169A\u16A0-\u16EA\u16EE-\u16F0\u1700-\u170C\u170E-\u1711\u1720-\u1731\u1740-\u1751\u1760-\u176C\u176E-\u1770\u1780-\u17B3\u17D7\u17DC\u1820-\u1877\u1880-\u18A8\u18AA\u18B0-\u18F5\u1900-\u191C\u1950-\u196D\u1970-\u1974\u1980-\u19AB\u19C1-\u19C7\u1A00-\u1A16\u1A20-\u1A54\u1AA7\u1B05-\u1B33\u1B45-\u1B4B\u1B83-\u1BA0\u1BAE-\u1BAF\u1BBA-\u1BE5\u1C00-\u1C23\u1C4D-\u1C4F\u1C5A-\u1C7D\u1CE9-\u1CEC\u1CEE-\u1CF1\u1CF5-\u1CF6\u1D00-\u1DBF\u1E00-\u1F15\u1F18-\u1F1D\u1F20-\u1F45\u1F48-\u1F4D\u1F50-\u1F57\u1F59\u1F5B\u1F5D\u1F5F-\u1F7D\u1F80-\u1FB4\u1FB6-\u1FBC\u1FBE\u1FC2-\u1FC4\u1FC6-\u1FCC\u1FD0-\u1FD3\u1FD6-\u1FDB\u1FE0-\u1FEC\u1FF2-\u1FF4\u1FF6-\u1FFC\u2071\u207F\u2090-\u209C\u2102\u2107\u210A-\u2113\u2115\u2118-\u211D\u2124\u2126\u2128\u212A-\u2139\u213C-\u213F\u2145-\u2149\u214E\u2160-\u2188\u2C00-\u2C2E\u2C30-\u2C5E\u2C60-\u2CE4\u2CEB-\u2CEE\u2CF2-\u2CF3\u2D00-\u2D25\u2D27\u2D2D\u2D30-\u2D67\u2D6F\u2D80-\u2D96\u2DA0-\u2DA6\u2DA8-\u2DAE\u2DB0-\u2DB6\u2DB8-\u2DBE\u2DC0-\u2DC6\u2DC8-\u2DCE\u2DD0-\u2DD6\u2DD8-\u2DDE\u3005-\u3007\u3021-\u3029\u3031-\u3035\u3038-\u303C\u3041-\u3096\u309B-\u309F\u30A1-\u30FA\u30FC-\u30FF\u3105-\u312D\u3131-\u318E\u31A0-\u31BA\u31F0-\u31FF\u3400-\u4DB5\u4E00-\u9FCC\uA000-\uA48C\uA4D0-\uA4FD\uA500-\uA60C\uA610-\uA61F\uA62A-\uA62B\uA640-\uA66E\uA67F-\uA697\uA6A0-\uA6EF\uA717-\uA71F\uA722-\uA788\uA78B-\uA78E\uA790-\uA793\uA7A0-\uA7AA\uA7F8-\uA801\uA803-\uA805\uA807-\uA80A\uA80C-\uA822\uA840-\uA873\uA882-\uA8B3\uA8F2-\uA8F7\uA8FB\uA90A-\uA925\uA930-\uA946\uA960-\uA97C\uA984-\uA9B2\uA9CF\uAA00-\uAA28\uAA40-\uAA42\uAA44-\uAA4B\uAA60-\uAA76\uAA7A\uAA80-\uAAAF\uAAB1\uAAB5-\uAAB6\uAAB9-\uAABD\uAAC0\uAAC2\uAADB-\uAADD\uAAE0-\uAAEA\uAAF2-\uAAF4\uAB01-\uAB06\uAB09-\uAB0E\uAB11-\uAB16\uAB20-\uAB26\uAB28-\uAB2E\uABC0-\uABE2\uAC00-\uD7A3\uD7B0-\uD7C6\uD7CB-\uD7FB\uF900-\uFA6D\uFA70-\uFAD9\uFB00-\uFB06\uFB13-\uFB17\uFB1D\uFB1F-\uFB28\uFB2A-\uFB36\uFB38-\uFB3C\uFB3E\uFB40-\uFB41\uFB43-\uFB44\uFB46-\uFBB1\uFBD3-\uFD3D\uFD50-\uFD8F\uFD92-\uFDC7\uFDF0-\uFDFB\uFE70-\uFE74\uFE76-\uFEFC\uFF21-\uFF3A\uFF41-\uFF5A\uFF66-\uFFBE\uFFC2-\uFFC7\uFFCA-\uFFCF\uFFD2-\uFFD7\uFFDA-\uFFDC] ;

