	.text
	.globl main
main:
	movq $100, %rdi
	movq $4, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $102, %rdi
	movq $1, %rax
	subq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $100, %rdi
	movq $2, %rax
	movq $4, %r10
	imulq %r10, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $216, %rdi
	movq $2, %r10
	movq %rdi, %rax
	cqto
	idivq %r10
	movq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $3, %rdi
	movq $37, %rax
	imulq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $32, %rdi
	call putchar
	movq %rax, %rdi
	movq $118, %rdi
	movq $1, %r10
	movq $2, %rax
	subq %rax, %r10
	movq $0, %rax
	subq %r10, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $100, %rdi
	movq $122, %rax
	movq $11, %r10
	cqto
	idivq %r10
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $113, %rdi
	movq $1, %rax
	movq $2, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $108, %rdi
	movq $2, %rax
	movq $1, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $99, %rdi
	movq $2, %rax
	movq $1, %r10
	movq $1, %rsi
	addq %rsi, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	movq $1, %rax
	movq $2, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq %rax, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
