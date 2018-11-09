grammar Processors;

INTEGER: [+-]?('0'..'9')+;
DOUBLE: ([+-]?('0'..'9')+)?[,.]('0'..'9')+;
STRING: '"' ('\\"'|~'"')* '"';
WHITESPACE: [ \n\r\t];

fragment JavaLetter : [a-zA-Z$_]
	                | ~[\u0000-\u007F\uD800-\uDBFF] {Character.isJavaIdentifierStart(_input.LA(-1))}?
                    | [\uD800-\uDBFF] [\uDC00-\uDFFF] {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
                    ;

fragment JavaLetterOrDigit : [a-zA-Z0-9$_]
                           | ~[\u0000-\u007F\uD800-\uDBFF] {Character.isJavaIdentifierPart(_input.LA(-1))}?
                           | [\uD800-\uDBFF] [\uDC00-\uDFFF] {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	                       ;

processors: processor (';' processor)* EOF;

processor: identifier ('(' configurationParameters=parameters ')')? (':' executionParameters=parameters)?;

identifier: (JavaLetter JavaLetterOrDigit*) ('.' JavaLetter JavaLetterOrDigit*)*;

parameters: (parameter (',' parameter)*)?;

parameter: integerParameter | doubleParameter | stringParameter | constantParameter;

integerParameter: INTEGER;
doubleParameter: DOUBLE;
stringParameter: STRING;
constantParameter: JavaLetter JavaLetterOrDigit*;


