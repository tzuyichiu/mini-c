	.text
	.globl main
f:
	call putchar
	movq $0, %rax
	ret
main:
	movq $66, %rsi
	movq $65, %rdi
	call f
	movq %rax, %rsi
	movq $65, %rsi
	movq $66, %rdi
	call f
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
