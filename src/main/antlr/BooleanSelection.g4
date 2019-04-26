grammar BooleanSelection;

NOT: 'not:';
TRUE: 'true' | 'TRUE' | '1';
FALSE: 'false' | 'FALSE' | '0';
EQUAL: 'eq:';

selection: filter (';' filter)* EOF;

filter: clause (',' clause)*;

clause: operation
      | negation
      ;

negation: NOT operation;

operation: target=(TRUE | FALSE)
         | name=EQUAL target=(TRUE | FALSE)
         | name=LESS_THAN_OR_EQUAL target=NUMBER
         ;
