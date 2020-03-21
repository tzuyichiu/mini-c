	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-16, %rsp
	movq $16, %rdi
	call sbrk
	movq %rax, %rdi
	movq %rdi, -8(%rbp)
	movq $24, %rdi
	call sbrk
	movq %rax, %rdi
	movq %rdi, -16(%rbp)
	movq $16, %rdi
	call sbrk
	movq -16(%rbp), %rdi
	movq %rax, 8(%rdi)
	movq $65, %rdi
	movq -16(%rbp), %r10
	movq %rdi, 0(%r10)
	movq $66, %r10
	movq -16(%rbp), %rax
	movq %r10, 16(%rax)
	movq $120, %rax
	movq -16(%rbp), %r10
	movq 8(%r10), %r10
	movq %rax, 0(%r10)
	movq $121, %r10
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq %r10, 8(%rax)
	movq -16(%rbp), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq 8(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 16(%rax), %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $88, %rax
	movq -8(%rbp), %r10
	movq %rax, 0(%r10)
	movq $89, %r10
	movq -8(%rbp), %rax
	movq %r10, 8(%rax)
	movq -16(%rbp), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq 8(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 16(%rax), %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq -8(%rbp), %rax
	movq -16(%rbp), %r10
	movq %rax, 8(%r10)
	movq -16(%rbp), %r10
	movq 0(%r10), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq 8(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 16(%rax), %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
