	.text
	.globl main
many:
	pushq %rbp
	movq %rsp, %rbp
	addq $-96, %rsp
	movq %rdi, -32(%rbp)
	movq %rsi, -40(%rbp)
	movq %rdx, -48(%rbp)
	movq %rcx, -56(%rbp)
	movq %r8, -64(%rbp)
	movq %r9, -72(%rbp)
	movq 16(%rbp), %r15
	movq %r15, -8(%rbp)
	movq 24(%rbp), %r15
	movq %r15, -96(%rbp)
	movq 32(%rbp), %r15
	movq %r15, -88(%rbp)
	movq 40(%rbp), %r15
	movq %r15, -80(%rbp)
	movq $64, %rdi
	movq -32(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -40(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -48(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -56(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -64(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -72(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -80(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -88(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -96(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $64, %rdi
	movq -8(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq -32(%rbp), %rax
	movq $10, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L2
	movq -32(%rbp), %r10
	movq -8(%rbp), %r15
	movq %r15, -24(%rbp)
	movq -96(%rbp), %r15
	movq %r15, -16(%rbp)
	movq -88(%rbp), %rax
	movq -80(%rbp), %r9
	movq -72(%rbp), %r8
	movq -64(%rbp), %rcx
	movq -56(%rbp), %rdx
	movq -48(%rbp), %rsi
	movq -40(%rbp), %rdi
	pushq %rax
	movq -16(%rbp), %r15
	pushq %r15
	movq -24(%rbp), %r15
	pushq %r15
	pushq %r10
	call many
	movq $32, %rax
	subq %rax, %rsp
L2:
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	jmp L2
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-16, %rsp
	movq $10, %r10
	movq $9, -16(%rbp)
	movq $8, -8(%rbp)
	movq $7, %rax
	movq $6, %r9
	movq $5, %r8
	movq $4, %rcx
	movq $3, %rdx
	movq $2, %rsi
	movq $1, %rdi
	pushq %rax
	movq -8(%rbp), %r15
	pushq %r15
	movq -16(%rbp), %r15
	pushq %r15
	pushq %r10
	call many
	movq $32, %rax
	subq %rax, %rsp
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
