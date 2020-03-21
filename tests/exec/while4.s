	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $10, %rax
	movq %rax, -8(%rbp)
L15:
	movq -8(%rbp), %rax
	addq $0, %rax
	jz L4
	movq -8(%rbp), %rdi
	movq $1, %rax
	subq %rax, %rdi
	movq %rdi, -8(%rbp)
	movq $65, %rdi
	movq -8(%rbp), %rax
	addq %rax, %rdi
	call putchar
	movq %rax, %rdi
	jmp L15
L4:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
