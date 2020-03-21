	.text
	.globl main
main:
	movq $65, %rdi
	movq $8, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq %rax, %rdi
	movq $65, %rdi
	movq $16, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
