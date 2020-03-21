	.text
	.globl main
print_int:
	pushq %rbp
	movq %rsp, %rbp
	addq $-16, %rsp
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rax
	movq $10, %r10
	cqto
	idivq %r10
	movq %rax, -16(%rbp)
	movq -8(%rbp), %rax
	movq $9, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setg %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L10
	movq -16(%rbp), %rdi
	call print_int
	movq %rax, %rdi
L10:
	movq $48, %rdi
	movq -8(%rbp), %rax
	movq $10, %r10
	movq -16(%rbp), %rsi
	imulq %rsi, %r10
	subq %r10, %rax
	addq %rax, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	jmp L10
main:
	movq $42, %rdi
	call print_int
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
