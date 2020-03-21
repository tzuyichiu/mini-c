	.text
	.globl main
main:
	movq $66, %rdi
	movq $1, %r10
	movq $0, %rax
	subq %r10, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $65, %rdi
	movq $1, %rax
	movq $0, %r10
	subq %rax, %r10
	movq $0, %rax
	subq %r10, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
