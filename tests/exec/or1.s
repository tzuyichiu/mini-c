	.text
	.globl main
main:
	movq $65, %rdi
	movq $1, %rax
	addq $0, %rax
	jz L37
L34:
	movq $1, %rax
L33:
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $65, %rdi
	movq $0, %rax
	addq $0, %rax
	jz L28
L25:
	movq $1, %rax
L24:
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $65, %rdi
	movq $1, %rax
	addq $0, %rax
	jz L19
L16:
	movq $1, %rax
L15:
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $65, %rdi
	movq $0, %rax
	addq $0, %rax
	jz L10
L7:
	movq $1, %rax
L6:
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
L10:
	movq $0, %rax
	addq $0, %rax
	jnz L7
	movq $0, %rax
	jmp L6
	jmp L7
L19:
	movq $0, %rax
	addq $0, %rax
	jnz L16
	movq $0, %rax
	jmp L15
	jmp L16
L28:
	movq $2, %rax
	addq $0, %rax
	jnz L25
	movq $0, %rax
	jmp L24
	jmp L25
L37:
	movq $1, %rax
	addq $0, %rax
	jnz L34
	movq $0, %rax
	jmp L33
	jmp L34
	.data
