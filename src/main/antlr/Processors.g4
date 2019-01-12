grammar Processors;

STRING: '"' ('\\"'|~'"')* '"';
DOUBLE: ([+-]?('0'..'9')+)?[,.]('0'..'9')+;
INTEGER: [+-]?('0'..'9')+;
NAME: [a-zA-Z$_][a-zA-Z0-9$_]+;
WHITESPACE: [ \n\r\t];

processors: processor (';' processor)* EOF;

processor: identifier ('(' configurationParameters=parameters ')')? (':' executionParameters=parameters)?;

identifier: NAME ('.' NAME)*;

parameters: (parameter (',' parameter)*)?;

parameter: integerParameter | doubleParameter | stringParameter | constantParameter;

integerParameter: INTEGER;
doubleParameter: DOUBLE;
stringParameter: STRING;
constantParameter: NAME;


