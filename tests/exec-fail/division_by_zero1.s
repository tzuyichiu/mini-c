	.text
	.globl main
main:
	movq $1, %rdi
	movq $0, %r10
	movq %rdi, %rax
	cqto
	idivq %r10
	movq %rax, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
