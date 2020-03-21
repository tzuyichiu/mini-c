	.text
	.globl main
f:
	movq %rdi, %rax
	movq $2, %r10
	imulq %rsi, %r10
	addq %r10, %rax
	ret
main:
	movq $0, %rsi
	movq $65, %rdi
	call f
	movq %rax, %rdi
	call putchar
	movq %rax, %rsi
	movq $1, %rsi
	movq $65, %rdi
	call f
	movq %rax, %rdi
	call putchar
	movq %rax, %rsi
	movq $2, %rsi
	movq $65, %rdi
	call f
	movq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
