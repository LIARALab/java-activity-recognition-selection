grammar DurationSelection;

WHITESPACE: [ \n\r\t];
NOT: 'not:';
DELTA: '+-' | ':delta:' | ':dt:';
NUMBER: ('+'|'-')? ('0'..'9')+;
NEAR: 'near:';
EQUAL: 'eq:';
GREATHER_THAN: 'gt:';
GREATHER_THAN_OR_EQUAL: 'gte:';
LESS_THAN: 'lt:';
LESS_THAN_OR_EQUAL: 'lte:';
RANGE: ':' | ':and:';
YEAR: ('y' | 'year' | 'years');
MONTH: ('M' | 'month' | 'months');
WEEK: ('w' | 'week' | 'weeks');
DAY: ('d' | 'day' | 'days');
HOUR: ('h' | 'hour' | 'hours');
MINUTE: ('m' | 'minute' | 'minutes');
SECOND: ('s' | 'second' | 'seconds');
MILLISECOND: ('ms' | 'millisecond' | 'milliseconds');

selection: filter (';' filter)* EOF;

filter: clause (',' clause)*;

clause: negation
      | comparison
      ;

negation: NOT comparison;

comparison: range
          | near
          | operation
          ;

operation: duration
         | name=EQUAL duration
         | name=GREATHER_THAN duration
         | name=GREATHER_THAN_OR_EQUAL duration
         | name=LESS_THAN duration
         | name=LESS_THAN_OR_EQUAL duration
         ;

range: left=duration RANGE right=duration;

near: NEAR target=duration DELTA delta=duration;

duration: durationEntry+;

durationEntry: value=NUMBER unit=(YEAR | MONTH | WEEK | DAY | HOUR | MINUTE | SECOND | MILLISECOND);
