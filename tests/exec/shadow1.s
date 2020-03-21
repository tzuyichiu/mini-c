	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $0, %r10
	movq %r10, %rax
	movq $1, %r10
	movq %r10, %rax
	movq %rax, %r10
	movq $1, %rax
	xorq %r15, %r15
	cmpq %rax, %r10
	sete %r15b
	movq %r15, %r10
	addq $0, %r10
	jz L10
	movq $97, %rdi
	call putchar
L10:
	movq -8(%rbp), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L4
	movq $98, %rdi
	call putchar
	movq %rax, %rdi
L4:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	jmp L4
	jmp L10
	.data
