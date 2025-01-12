# AntlrY86Assembler

This project accurately transforms the y86 assembly code into machine code, just like a [y86 assembler](https://esolangs.org/wiki/Y86).

Using Antlr to create a y86 grammar object, the program can accurately split the .txt file into the instructions, registers, and parameters. This is necessary as each instruction has different formats and validity.

**Antlr Code Example**
```
REGISTER: (' %' | '%') REGISTER_CHARS ;
REGISTER_CHARS: [r,e][a-zA-Z_0-9]* ;
```
This piece of Antlr code ensures the validity of a register. It must begin with `%`, followed by either an `r` or `e`, and then a mix of characters and numbers.

Once all parts of each instruction are correctly split, the computer can start mapping each Y86 instruction to its machine code correspondent (e.g., "halt" -> 00). Then, using specific position increments for each instruction, the correct positions are obtained for each line of machine code. The new machine code is then written into an output file.

## Examples:

**Y86 Assembler Code**
```.pos 0
init:	irmovl Stack, %esp
	irmovl Stack, %ebp
	call Main
	halt
array:	.long 0xd
	.long 0xc0
	.long 0xb00
	.long 0xa000
Main:	pushl %ebp
	rrmovl %esp,%ebp
	irmovl $4,%eax
	pushl %eax
	irmovl array,%edx
	pushl %edx
	call Sum
	rrmovl %ebp,%esp
	popl %ebp
	ret
Sum:	pushl %ebp
	rrmovl %esp,%ebp
	mrmovl 8(%ebp),%ecx
	mrmovl 12(%ebp),%edx
	xorl %eax,%eax
	andl %edx,%edx
	je End
Loop:	mrmovl (%ecx),%esi
	addl %esi,%eax
	irmovl $4,%ebx
	addl %ebx,%ecx
	irmovl $-1,%ebx
	addl %ebx,%edx
	jne Loop
End:	rrmovl %ebp,%esp
	popl %ebp
	ret

	.pos 0x100
Stack:
```

**Converted Machine Code**
```0x0: 30f400010000
0x6: 30f500010000
0xc: 8024000000
0x11: 00
0x14: 0d000000
0x18: c0000000
0x1c: 000b0000
0x20: 00a00000
0x24: a05f
0x26: 2045
0x28: 30f004000000
0x2e: a00f
0x30: 30f214000000
0x36: a02f
0x38: 8042000000
0x3d: 2054
0x3f: b05f
0x41: 90
0x42: a05f
0x44: 2045
0x46: 501508000000
0x4c: 50250c000000
0x52: 6300
0x54: 6222
0x56: 7378000000
0x5b: 506100000000
0x61: 6060
0x63: 30f304000000
0x69: 6031
0x6b: 30f3ffffffff
0x71: 6032
0x73: 745b000000
0x78: 2054
0x7a: b05f
0x7c: 90
```
