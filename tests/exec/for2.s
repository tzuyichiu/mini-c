	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $10, %rax
	movq %rax, -8(%rbp)
L18:
	movq -8(%rbp), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setg %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L4
	movq $65, %rdi
	movq -8(%rbp), %rax
	movq $1, %r10
	subq %r10, %rax
	movq %rax, -8(%rbp)
	addq %rax, %rdi
	movq $1, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	jmp L18
L4:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
