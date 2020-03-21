	.text
	.globl main
fact:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq %rdi, %rax
	movq $1, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setle %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L7
	movq $1, -8(%rbp)
L1:
	movq -8(%rbp), %rax
	movq %rbp, %rsp
	popq %rbp
	ret
L7:
	movq %rdi, -8(%rbp)
	movq $1, %rax
	subq %rax, %rdi
	call fact
	movq -8(%rbp), %r15
	imulq %rax, %r15
	movq %r15, -8(%rbp)
	jmp L1
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-16, %rsp
	movq $0, %rax
	movq %rax, -16(%rbp)
L30:
	movq -16(%rbp), %rax
	movq $4, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setle %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L16
	movq $65, -8(%rbp)
	movq -16(%rbp), %rdi
	call fact
	addq %rax, -8(%rbp)
	movq -8(%rbp), %rdi
	call putchar
	movq %rax, %rdi
	movq -16(%rbp), %rdi
	movq $1, %rax
	addq %rax, %rdi
	movq %rdi, -16(%rbp)
	jmp L30
L16:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
