grammar NumberSelection;

NOT: 'not:';
NUMBER: ('+'|'-')? ('0'..'9')+('.'('0'..'9')+)?;
DELTA: '+-' | ':delta:' | ':dt:';
NEAR: 'near:';
EQUAL: 'eq:';
GREATHER_THAN: 'gt:';
GREATHER_THAN_OR_EQUAL: 'gte:';
LESS_THAN: 'lt:';
LESS_THAN_OR_EQUAL: 'lte:';
RANGE: ':' | ':and:';

selection: filter (';' filter)* EOF;

filter: clause (',' clause)*;

clause: comparison
      | negation
      ;

negation: NOT comparison;

comparison: operation
          | range
          | near
          ;

operation: target=NUMBER
         | name=EQUAL target=NUMBER
         | name=GREATHER_THAN target=NUMBER
         | name=GREATHER_THAN_OR_EQUAL target=NUMBER
         | name=LESS_THAN target=NUMBER
         | name=LESS_THAN_OR_EQUAL target=NUMBER
         ;

range: left=NUMBER RANGE right=NUMBER;

near: NEAR target=NUMBER DELTA delta=NUMBER;
