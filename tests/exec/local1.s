	.text
	.globl main
main:
	movq $65, %rdi
	call putchar
	movq %rax, %rdi
	movq $66, %rdi
	call putchar
	movq %rax, %rdi
	movq $67, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
