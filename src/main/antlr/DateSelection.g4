grammar DateSelection;

WHITESPACE: [ \n\r\t];

NOT: 'not:';
DEFAULT: 'default:';
LOCALE: 'locale:';
FORMAT: 'format:';
EQUAL: 'eq:';
GREATHER_THAN: 'gt:';
GREATHER_THAN_OR_EQUAL: 'gte:';
LESS_THAN: 'lt:';
LESS_THAN_OR_EQUAL: 'lte:';
RANGE: ':' | ':and:';
TOKEN: '(' ('\')\'' | '\\)' |~')')* ')';

selection: (defaultConfiguration ';')? filter (';' filter)* EOF;

defaultConfiguration: locale format? | format locale?;

filter: clause (',' clause)*;

clause: comparison
      | negation
      ;

negation: NOT comparison;

comparison: operation
          | range
          ;

operation: date
         | name=EQUAL date
         | name=GREATHER_THAN date
         | name=GREATHER_THAN_OR_EQUAL date
         | name=LESS_THAN date
         | name=LESS_THAN_OR_EQUAL date
         ;

range: locale? format? left=TOKEN RANGE right=TOKEN;

date: locale? format? value=TOKEN;

locale: LOCALE TOKEN;

format: FORMAT TOKEN;