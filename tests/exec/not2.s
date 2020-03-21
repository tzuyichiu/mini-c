	.text
	.globl main
main:
	movq $0, %rdi
	movq %rdi, %rax
	movq $65, %rdi
	xorq %r15, %r15
	cmpq $0, %rax
	sete %r15b
	movq %r15, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $1, %rdi
	movq %rdi, %rax
	movq $65, %rdi
	xorq %r15, %r15
	cmpq $0, %rax
	sete %r15b
	movq %r15, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
