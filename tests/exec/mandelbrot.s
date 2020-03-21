	.text
	.globl main
add:
	movq %rdi, %rax
	addq %rsi, %rax
	ret
sub:
	subq %rsi, %rdi
	movq %rdi, %rax
	ret
mul:
	imulq %rsi, %rdi
	movq $8192, %rax
	movq $2, %r10
	cqto
	idivq %r10
	addq %rax, %rdi
	movq $8192, %r10
	movq %rdi, %rax
	cqto
	idivq %r10
	movq %rax, %rdi
	movq %rdi, %rax
	ret
div:
	movq $8192, %rax
	imulq %rax, %rdi
	movq %rsi, %rax
	movq $2, %r10
	cqto
	idivq %r10
	addq %rax, %rdi
	movq %rdi, %rax
	cqto
	idivq %rsi
	movq %rax, %rdi
	movq %rdi, %rax
	ret
of_int:
	movq %rdi, %rax
	movq $8192, %r10
	imulq %r10, %rax
	ret
iter:
	pushq %rbp
	movq %rsp, %rbp
	addq $-96, %rsp
	movq %rdi, -88(%rbp)
	movq %rsi, -96(%rbp)
	movq %rdx, -8(%rbp)
	movq %rcx, -16(%rbp)
	movq %r8, -32(%rbp)
	movq -88(%rbp), %rax
	movq $100, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L72
	movq $1, %rax
L37:
	movq %rbp, %rsp
	popq %rbp
	ret
L72:
	movq -16(%rbp), %rsi
	movq -16(%rbp), %rdi
	call mul
	movq %rax, %rsi
	movq %rsi, -48(%rbp)
	movq -32(%rbp), %rsi
	movq -32(%rbp), %rdi
	call mul
	movq %rax, %rsi
	movq %rsi, -40(%rbp)
	movq -40(%rbp), %rsi
	movq -48(%rbp), %rdi
	call add
	movq %rax, -24(%rbp)
	movq $4, %rdi
	call of_int
	xorq %r15, %r15
	cmpq %rax, -24(%rbp)
	setg %r15b
	movq %r15, -24(%rbp)
	addq $0, -24(%rbp)
	jz L56
	movq $0, %rax
	jmp L37
L56:
	movq -8(%rbp), %r15
	movq %r15, -72(%rbp)
	movq -32(%rbp), %rsi
	movq -16(%rbp), %rdi
	call mul
	movq %rax, -80(%rbp)
	movq $2, %rdi
	call of_int
	movq %rax, %rdi
	movq -80(%rbp), %rsi
	call mul
	movq %rax, %rdi
	movq -72(%rbp), %rsi
	call add
	movq %rax, -56(%rbp)
	movq -96(%rbp), %r15
	movq %r15, -64(%rbp)
	movq -40(%rbp), %rsi
	movq -48(%rbp), %rdi
	call sub
	movq %rax, %rdi
	movq -64(%rbp), %rsi
	call add
	movq %rax, %rcx
	movq -8(%rbp), %rdx
	movq -96(%rbp), %rsi
	movq -88(%rbp), %rdi
	movq $1, %rax
	addq %rax, %rdi
	movq -56(%rbp), %r8
	call iter
	jmp L37
inside:
	pushq %rbp
	movq %rsp, %rbp
	addq $-24, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, -16(%rbp)
	movq $0, %rdi
	call of_int
	movq %rax, -24(%rbp)
	movq $0, %rdi
	call of_int
	movq %rax, %rcx
	movq -16(%rbp), %rdx
	movq -8(%rbp), %rsi
	movq $0, %rdi
	movq -24(%rbp), %r8
	call iter
	movq %rbp, %rsp
	popq %rbp
	ret
run:
	pushq %rbp
	movq %rsp, %rbp
	addq $-112, %rsp
	movq %rdi, -32(%rbp)
	movq $2, %rax
	movq $0, %rdi
	subq %rax, %rdi
	call of_int
	movq %rax, %rdi
	movq %rdi, -88(%rbp)
	movq $1, %rdi
	call of_int
	movq %rax, %rdi
	movq %rdi, -80(%rbp)
	movq $2, %rdi
	movq -32(%rbp), %rax
	imulq %rax, %rdi
	call of_int
	movq %rax, -16(%rbp)
	movq -88(%rbp), %rsi
	movq -80(%rbp), %rdi
	call sub
	movq %rax, %rdi
	movq -16(%rbp), %rsi
	call div
	movq %rax, -72(%rbp)
	movq $1, %rax
	movq $0, %rdi
	subq %rax, %rdi
	call of_int
	movq %rax, %rdi
	movq %rdi, -64(%rbp)
	movq $1, %rdi
	call of_int
	movq %rax, %rdi
	movq %rdi, -56(%rbp)
	movq -32(%rbp), %rdi
	call of_int
	movq %rax, -24(%rbp)
	movq -64(%rbp), %rsi
	movq -56(%rbp), %rdi
	call sub
	movq %rax, %rdi
	movq -24(%rbp), %rsi
	call div
	movq %rax, -48(%rbp)
	movq $0, %rax
	movq %rax, -40(%rbp)
L134:
	movq -40(%rbp), %rax
	movq -32(%rbp), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L88
	movq -48(%rbp), %r15
	movq %r15, -112(%rbp)
	movq -40(%rbp), %rdi
	call of_int
	movq %rax, %rdi
	movq -112(%rbp), %rsi
	call mul
	movq %rax, %rsi
	movq -64(%rbp), %rdi
	call add
	movq %rax, -104(%rbp)
	movq $0, %rax
	movq %rax, -96(%rbp)
L121:
	movq -96(%rbp), %rax
	movq $2, %r10
	movq -32(%rbp), %rdi
	imulq %rdi, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L95
	movq -72(%rbp), %r15
	movq %r15, -8(%rbp)
	movq -96(%rbp), %rdi
	call of_int
	movq %rax, %rdi
	movq -8(%rbp), %rsi
	call mul
	movq %rax, %rsi
	movq -88(%rbp), %rdi
	call add
	movq %rax, %rsi
	movq %rsi, %rdi
	movq -104(%rbp), %rsi
	call inside
	addq $0, %rax
	jz L104
	movq $48, %rdi
	call putchar
	movq %rax, %rdi
L100:
	movq -96(%rbp), %rdi
	movq $1, %rax
	addq %rax, %rdi
	movq %rdi, -96(%rbp)
	jmp L121
L104:
	movq $49, %rdi
	call putchar
	movq %rax, %rdi
	jmp L100
L95:
	movq $10, %rdi
	call putchar
	movq -40(%rbp), %rax
	movq $1, %r10
	addq %r10, %rax
	movq %rax, -40(%rbp)
	jmp L134
L88:
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
main:
	movq $30, %rdi
	call run
	movq $0, %rax
	ret
	.data
