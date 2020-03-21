	.text
	.globl main
fact_rec:
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
	call fact_rec
	movq -8(%rbp), %r15
	imulq %rax, %r15
	movq %r15, -8(%rbp)
	jmp L1
main:
	movq $0, %rdi
	call fact_rec
	movq $1, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L30
	movq $49, %rdi
	call putchar
	movq %rax, %rdi
L30:
	movq $1, %rdi
	call fact_rec
	movq $1, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L23
	movq $50, %rdi
	call putchar
	movq %rax, %rdi
L23:
	movq $5, %rdi
	call fact_rec
	movq $120, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L16
	movq $51, %rdi
	call putchar
	movq %rax, %rdi
L16:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	jmp L16
	jmp L23
	jmp L30
	.data
