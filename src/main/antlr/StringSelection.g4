grammar StringSelection;

NOT: 'not:';
WHITESPACE: [ \n\r\t];
STRING: '"' ('\\"'|~'"')* '"';
REGEXP: '/' ('\\/' | ~'/')* '/';
TOKEN: (~[ \n\r\t"',;/])+;

selection: filter (';' filter)* EOF;

filter: clause ((',' | WHITESPACE+) clause)*;

clause: negation
      | operation
      ;

negation: NOT operation;

operation: REGEXP
         | STRING
         | TOKEN
         ;
