	.text
	.globl main
main:
	movq $0, %rax
	movq %rax, %rsi
	movq $0, %rax
	movq %rax, %rdi
L34:
	movq %rdi, %rax
	movq $10, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L10
	movq $10, %rax
	movq %rax, %rdx
L28:
	movq %rdx, %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setg %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L15
	movq %rsi, %r10
	movq $1, %rax
	addq %rax, %r10
	movq %r10, %rsi
	movq %rdx, %r10
	movq $1, %rax
	subq %rax, %r10
	movq %r10, %rdx
	jmp L28
L15:
	movq %rdi, %r10
	movq $1, %rax
	addq %rax, %r10
	movq %r10, %rdi
	jmp L34
L10:
	movq %rsi, %r10
	movq $100, %rax
	xorq %r15, %r15
	cmpq %rax, %r10
	sete %r15b
	movq %r15, %r10
	addq $0, %r10
	jz L4
	movq $33, %rdi
	call putchar
	movq %rax, %rdi
L4:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	jmp L4
	.data
