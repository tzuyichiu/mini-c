	.text
	.globl main
get:
	movq %rsi, %r10
	movq $0, %rax
	xorq %r15, %r15
	cmpq %rax, %r10
	sete %r15b
	movq %r15, %r10
	addq $0, %r10
	jz L7
	movq 0(%rdi), %rax
L1:
	ret
L7:
	movq $1, %rax
	subq %rax, %rsi
	movq 8(%rdi), %rdi
	call get
	jmp L1
set:
	movq %rsi, %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L21
	movq %rdx, 0(%rdi)
L14:
	movq %rdi, %rax
	ret
L21:
	movq $1, %rax
	subq %rax, %rsi
	movq 8(%rdi), %rdi
	call set
	movq %rax, %rdi
	jmp L14
create:
	pushq %rbp
	movq %rsp, %rbp
	addq $-16, %rsp
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L42
	movq $0, %rax
L29:
	movq %rbp, %rsp
	popq %rbp
	ret
L42:
	movq $16, %rdi
	call sbrk
	movq %rax, -16(%rbp)
	movq $0, %rax
	movq -16(%rbp), %rdi
	movq %rax, 0(%rdi)
	movq -8(%rbp), %rdi
	movq $1, %rax
	subq %rax, %rdi
	call create
	movq %rax, %r10
	movq -16(%rbp), %rax
	movq %r10, 8(%rax)
	movq -16(%rbp), %rax
	jmp L29
print_row:
	pushq %rbp
	movq %rsp, %rbp
	addq $-24, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, -16(%rbp)
	movq $0, %rax
	movq %rax, -24(%rbp)
L70:
	movq -24(%rbp), %rax
	movq -16(%rbp), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setle %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L51
	movq -24(%rbp), %rsi
	movq -8(%rbp), %rdi
	call get
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setne %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L60
	movq $42, %rdi
	call putchar
	movq %rax, %rdi
L56:
	movq -24(%rbp), %rdi
	movq $1, %rax
	addq %rax, %rdi
	movq %rdi, -24(%rbp)
	jmp L70
L60:
	movq $46, %rdi
	call putchar
	movq %rax, %rdi
	jmp L56
L51:
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
mod7:
	movq $7, %r10
	movq %rdi, %rax
	movq $7, %rsi
	cqto
	idivq %rsi
	imulq %rax, %r10
	subq %r10, %rdi
	movq %rdi, %rax
	ret
compute_row:
	pushq %rbp
	movq %rsp, %rbp
	addq $-24, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, -16(%rbp)
L108:
	movq -16(%rbp), %rsi
	movq $0, %rax
	xorq %r15, %r15
	cmpq %rax, %rsi
	setg %r15b
	movq %r15, %rsi
	addq $0, %rsi
	jz L86
	movq -16(%rbp), %rsi
	movq -8(%rbp), %rdi
	call get
	movq %rax, -24(%rbp)
	movq -16(%rbp), %rsi
	movq $1, %rax
	subq %rax, %rsi
	movq -8(%rbp), %rdi
	call get
	addq %rax, -24(%rbp)
	movq -24(%rbp), %rdi
	call mod7
	movq %rax, %rdx
	movq -16(%rbp), %rsi
	movq -8(%rbp), %rdi
	call set
	movq %rax, %rdx
	movq -16(%rbp), %rdx
	movq $1, %rax
	subq %rax, %rdx
	movq %rdx, -16(%rbp)
	jmp L108
L86:
	movq $1, %rdx
	movq $0, %rsi
	movq -8(%rbp), %rdi
	call set
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
pascal:
	pushq %rbp
	movq %rsp, %rbp
	addq $-24, %rsp
	movq %rdi, -24(%rbp)
	movq -24(%rbp), %rdi
	movq $1, %rax
	addq %rax, %rdi
	call create
	movq %rax, -16(%rbp)
	movq $0, %rax
	movq %rax, -8(%rbp)
L131:
	movq -8(%rbp), %rax
	movq -24(%rbp), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L112
	movq $0, %rdx
	movq -8(%rbp), %rsi
	movq -16(%rbp), %rdi
	call set
	movq %rax, %rsi
	movq -8(%rbp), %rsi
	movq -16(%rbp), %rdi
	call compute_row
	movq %rax, %rsi
	movq -8(%rbp), %rsi
	movq -16(%rbp), %rdi
	call print_row
	movq -8(%rbp), %rax
	movq $1, %r10
	addq %r10, %rax
	movq %rax, -8(%rbp)
	jmp L131
L112:
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
main:
	movq $42, %rdi
	call pascal
	movq $0, %rax
	ret
	.data
