	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $10, %rax
	movq %rax, -8(%rbp)
L14:
	movq -8(%rbp), %rax
	addq $0, %rax
	jz L4
	movq $65, %rdi
	movq -8(%rbp), %rax
	movq $1, %r10
	subq %r10, %rax
	movq %rax, -8(%rbp)
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	jmp L14
L4:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
