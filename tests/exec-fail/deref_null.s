	.text
	.globl main
main:
	movq $0, %rax
	movq 0(%rax), %rdi
	call putchar
	movq $0, %rax
	ret
	.data
