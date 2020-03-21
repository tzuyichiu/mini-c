	.text
	.globl main
main:
	movq $104, %rdi
	call putchar
	movq %rax, %rdi
	movq $101, %rdi
	call putchar
	movq %rax, %rdi
	movq $108, %rdi
	call putchar
	movq %rax, %rdi
	movq $108, %rdi
	call putchar
	movq %rax, %rdi
	movq $111, %rdi
	call putchar
	movq %rax, %rdi
	movq $32, %rdi
	call putchar
	movq %rax, %rdi
	movq $119, %rdi
	call putchar
	movq %rax, %rdi
	movq $111, %rdi
	call putchar
	movq %rax, %rdi
	movq $114, %rdi
	call putchar
	movq %rax, %rdi
	movq $108, %rdi
	call putchar
	movq %rax, %rdi
	movq $100, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	ret
	.data
