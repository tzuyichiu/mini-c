	.text
	.globl main
fact_imp:
	movq $1, %r10
	movq %r10, %rax
L16:
	movq %rdi, %r10
	movq $1, %rsi
	xorq %r15, %r15
	cmpq %rsi, %r10
	setg %r15b
	movq %r15, %r10
	addq $0, %r10
	jz L2
	movq %rdi, %r10
	movq $1, %rdi
	subq %rdi, %r10
	movq %r10, %rdi
	movq $1, %rsi
	addq %rsi, %r10
	imulq %r10, %rax
	jmp L16
L2:
	ret
main:
	movq $0, %rdi
	call fact_imp
	movq $1, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L36
	movq $49, %rdi
	call putchar
	movq %rax, %rdi
L36:
	movq $1, %rdi
	call fact_imp
	movq $1, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L29
	movq $50, %rdi
	call putchar
	movq %rax, %rdi
L29:
	movq $5, %rdi
	call fact_imp
	movq $120, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L22
	movq $51, %rdi
	call putchar
	movq %rax, %rdi
L22:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	jmp L22
	jmp L29
	jmp L36
	.data
