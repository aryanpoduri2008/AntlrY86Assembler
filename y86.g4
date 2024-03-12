grammar y86;

program: ( (section | section '\t'* (instruction | dot) | '\t'* instruction | '\t'* dot | '\n') '\n'?)* EOF ;

section: name;
name: LABEL ':' ;

dot: dottypes ;
dottypes: '.align ' INT
        | '.long ' HEX
        | '.pos ' (INT | HEX) ;

HEX: '0x' [a-f_0-9]*;

instruction: halt | nop | mov | op | jump | call | ret | stack ;

halt: 'halt' ;

nop: 'nop' ;

mov
    : ('rrmovl' | 'cmovle' | 'cmovl' | 'cmove' | 'cmovne' | 'cmovge' | 'cmovg') REGISTER ',' REGISTER
    | 'irmovl ' VALUE ',' REGISTER
    | 'irmovl ' LABEL ',' REGISTER
    | 'rmmovl ' REGISTER (RMMOVLHELPER | COMMA) '(' REGISTER ')'
    | 'mrmovl ' INT? '(' REGISTER ')' ',' REGISTER
    ;

RMMOVLHELPER: ',' INT ;
COMMA: ',' ;
INT: [0-9][0-9]* ;

op: ('addl' | 'subl' | 'andl' | 'xorl') REGISTER ',' REGISTER ;

jump: ('jmp ' | 'jle ' | 'je ' | 'jl ' | 'jne ' | 'je ' | 'jg ') LABEL ;

call: 'call ' LABEL ;

ret: 'ret' ;

stack: ('pushl' | 'popl') REGISTER ;
LABEL: [a-zA-Z_0-9][a-zA-Z_0-9]* ;


REGISTER: (' %' | '%') REGISTER_CHARS ;
VALUE: ((' $' | '$') '-'? INT) | LABEL ;



REGISTER_CHARS: [r,e][a-zA-Z_0-9]* ;