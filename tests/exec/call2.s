	.text
	.globl main
f:
	pushq %rbp
	movq %rsp, %rbp
	addq $-32, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, -16(%rbp)
	movq %rdx, -24(%rbp)
	movq %rcx, -32(%rbp)
	movq -8(%rbp), %rax
	addq $0, %rax
	jz L2
	movq -8(%rbp), %rdi
	call putchar
	movq %rax, %rcx
	movq -8(%rbp), %rcx
	movq -32(%rbp), %rdx
	movq -24(%rbp), %rsi
	movq -16(%rbp), %rdi
	call f
L1:
	movq %rbp, %rsp
	popq %rbp
	ret
L2:
	movq $0, %rax
	jmp L1
main:
	movq $0, %rcx
	movq $67, %rdx
	movq $66, %rsi
	movq $65, %rdi
	call f
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
