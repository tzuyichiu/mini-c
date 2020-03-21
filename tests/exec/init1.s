	.text
	.globl main
main:
	movq %rax, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L4
	movq $97, %rdi
	call putchar
	movq %rax, %rdi
L4:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	jmp L4
	.data
