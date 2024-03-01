grammar y86;

program: instruction* EOF ;

instruction: mov;

mov
    : 'rrmovq' REGISTER ',' REGISTER
    ;

REGISTER: (' %' | '%') CHARS ;

CHARS: [a-zA-Z_0-9][a-zA-Z_0-9]* ;