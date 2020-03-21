	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $16, %rdi
	call sbrk
	movq %rax, -8(%rbp)
	movq $65, %rax
	movq -8(%rbp), %r10
	movq %rax, 0(%r10)
	movq -8(%rbp), %r10
	movq 0(%r10), %rdi
	call putchar
	movq $66, %rax
	movq -8(%rbp), %r10
	movq %rax, 8(%r10)
	movq -8(%rbp), %r10
	movq 8(%r10), %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
