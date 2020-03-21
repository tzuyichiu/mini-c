	.text
	.globl main
make:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq %rdi, -8(%rbp)
	movq $24, %rdi
	call sbrk
	movq %rax, %r10
	movq %r10, %rax
	movq -8(%rbp), %r10
	movq %r10, 0(%rax)
	movq %rax, %r10
	movq %rax, 8(%r10)
	movq %r10, 16(%rax)
	movq %rbp, %rsp
	popq %rbp
	ret
inserer_apres:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, %rdi
	call make
	movq %rax, %r10
	movq %r10, %rax
	movq -8(%rbp), %r10
	movq 16(%r10), %r10
	movq %r10, 16(%rax)
	movq -8(%rbp), %rdi
	movq %rax, 16(%rdi)
	movq %rax, %rdi
	movq 16(%rax), %r10
	movq %rdi, 8(%r10)
	movq -8(%rbp), %r10
	movq %r10, 8(%rax)
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
supprimer:
	movq 16(%rdi), %r10
	movq 8(%rdi), %rax
	movq %r10, 16(%rax)
	movq %rdi, %rax
	movq 8(%rax), %r10
	movq 16(%rdi), %rax
	movq %r10, 8(%rax)
	movq $0, %rax
	ret
afficher:
	pushq %rbp
	movq %rsp, %rbp
	addq $-16, %rsp
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rax
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 16(%rax), %rax
	movq %rax, -16(%rbp)
L59:
	movq -16(%rbp), %rax
	movq -8(%rbp), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setne %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L48
	movq -16(%rbp), %rax
	movq 0(%rax), %rdi
	call putchar
	movq -16(%rbp), %rax
	movq 16(%rax), %rdi
	movq %rdi, -16(%rbp)
	jmp L59
L48:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $65, %rdi
	call make
	movq %rax, %rdi
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rdi
	call afficher
	movq %rax, %rsi
	movq $66, %rsi
	movq -8(%rbp), %rdi
	call inserer_apres
	movq %rax, %rdi
	movq -8(%rbp), %rdi
	call afficher
	movq %rax, %rsi
	movq $67, %rsi
	movq -8(%rbp), %rdi
	call inserer_apres
	movq %rax, %rdi
	movq -8(%rbp), %rdi
	call afficher
	movq -8(%rbp), %rax
	movq 16(%rax), %rdi
	call supprimer
	movq %rax, %rdi
	movq -8(%rbp), %rdi
	call afficher
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	.data
