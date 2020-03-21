	.text
	.globl main
main:
	movq $1, %rdi
	movq %rdi, %rax
	movq $65, %rdi
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
