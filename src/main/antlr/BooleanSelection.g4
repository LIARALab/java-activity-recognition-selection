grammar BooleanSelection;

NOT: 'not:';
TRUE: 'true' | 'TRUE' | '1';
FALSE: 'false' | 'FALSE' | '0';
NULL: 'null' | 'NULL';
EQUAL: 'eq:';

selection: filter (';' filter)* EOF;

filter: clause (',' clause)*;

clause: negation
      | operation
      ;

negation: NOT clause;

operation: target=(TRUE | FALSE | NULL)
         | name=EQUAL target=(TRUE | FALSE | NULL)
         ;
