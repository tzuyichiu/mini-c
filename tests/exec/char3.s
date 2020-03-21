	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $65, %rdi
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rdi
	call putchar
	movq %rax, %rdi
	movq -8(%rbp), %rdi
	movq $1, %rax
	addq %rax, %rdi
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
