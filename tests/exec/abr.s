	.text
	.globl main
make:
	pushq %rbp
	movq %rsp, %rbp
	addq $-24, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, -16(%rbp)
	movq %rdx, -24(%rbp)
	movq $24, %rdi
	call sbrk
	movq %rax, %r10
	movq %r10, %rax
	movq -8(%rbp), %r10
	movq %rax, %rdi
	movq %r10, 0(%rdi)
	movq -16(%rbp), %rdi
	movq %rax, %r10
	movq %rdi, 16(%r10)
	movq -24(%rbp), %r10
	movq %r10, 8(%rax)
	movq %rbp, %rsp
	popq %rbp
	ret
insere:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq %rdi, -8(%rbp)
	movq %rsi, %rax
	movq -8(%rbp), %r10
	movq 0(%r10), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L51
	movq $0, %rax
L15:
	movq %rbp, %rsp
	popq %rbp
	ret
L51:
	movq %rsi, %rax
	movq -8(%rbp), %r10
	movq 0(%r10), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L46
	movq -8(%rbp), %r10
	movq 16(%r10), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L26
	movq $0, %rdx
	movq $0, %rax
	movq %rsi, %rdi
	movq %rax, %rsi
	call make
	movq %rax, %r10
	movq -8(%rbp), %rax
	movq %r10, 16(%rax)
L16:
	movq $0, %rax
	jmp L15
L26:
	movq -8(%rbp), %rax
	movq 16(%rax), %rdi
	call insere
	movq %rax, %rdx
	jmp L16
L46:
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L41
	movq $0, %rdx
	movq $0, %rax
	movq %rsi, %rdi
	movq %rax, %rsi
	call make
	movq -8(%rbp), %r10
	movq %rax, 8(%r10)
	jmp L16
L41:
	movq -8(%rbp), %rax
	movq 8(%rax), %rdi
	call insere
	movq %rax, %rdx
	jmp L16
contient:
	movq %rsi, %rax
	movq 0(%rdi), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L85
	movq $1, %rax
L58:
	ret
L85:
	movq %rsi, %rax
	movq 0(%rdi), %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setl %r15b
	movq %r15, %rax
	addq $0, %rax
	jnz L80
L74:
	movq $0, %r10
L73:
	addq $0, %r10
	jz L68
	movq 16(%rdi), %rdi
	call contient
	jmp L58
L68:
	movq 8(%rdi), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setne %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L59
	movq 8(%rdi), %rdi
	call contient
	jmp L58
L59:
	movq $0, %rax
	jmp L58
L80:
	movq %rdi, %rax
	movq 16(%rax), %r10
	movq $0, %rax
	xorq %r15, %r15
	cmpq %rax, %r10
	setne %r15b
	movq %r15, %r10
	addq $0, %r10
	jz L74
	movq $1, %r10
	jmp L73
	jmp L74
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
	jz L101
	movq -16(%rbp), %rdi
	call print_int
	movq %rax, %rdi
L101:
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
	jmp L101
print:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq %rdi, -8(%rbp)
	movq $40, %rdi
	call putchar
	movq -8(%rbp), %rax
	movq 16(%rax), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setne %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L125
	movq -8(%rbp), %rax
	movq 16(%rax), %rdi
	call print
L125:
	movq -8(%rbp), %rax
	movq 0(%rax), %rdi
	call print_int
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq $0, %r10
	xorq %r15, %r15
	cmpq %r10, %rax
	setne %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L114
	movq -8(%rbp), %rax
	movq 8(%rax), %rdi
	call print
	movq %rax, %rdi
L114:
	movq $41, %rdi
	call putchar
	movq %rbp, %rsp
	popq %rbp
	ret
	jmp L114
	jmp L125
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $0, %rdx
	movq $0, %rsi
	movq $1, %rdi
	call make
	movq %rax, %rsi
	movq %rsi, -8(%rbp)
	movq $17, %rsi
	movq -8(%rbp), %rdi
	call insere
	movq %rax, %rsi
	movq $5, %rsi
	movq -8(%rbp), %rdi
	call insere
	movq %rax, %rsi
	movq $8, %rsi
	movq -8(%rbp), %rdi
	call insere
	movq %rax, %rdi
	movq -8(%rbp), %rdi
	call print
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq %rax, %rsi
	movq $5, %rsi
	movq -8(%rbp), %rdi
	call contient
	movq %rax, %rsi
	addq $0, %rsi
	jnz L179
L173:
	movq $0, %rsi
L172:
	addq $0, %rsi
	jnz L171
L166:
	movq $0, %rsi
L165:
	addq $0, %rsi
	jnz L164
L158:
	movq $0, %rax
L157:
	addq $0, %rax
	jz L150
	movq $111, %rdi
	call putchar
	movq %rax, %rdi
	movq $107, %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq %rax, %rsi
L150:
	movq $42, %rsi
	movq -8(%rbp), %rdi
	call insere
	movq %rax, %rsi
	movq $1000, %rsi
	movq -8(%rbp), %rdi
	call insere
	movq %rax, %rsi
	movq $0, %rsi
	movq -8(%rbp), %rdi
	call insere
	movq %rax, %rdi
	movq -8(%rbp), %rdi
	call print
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	jmp L150
L164:
	movq $3, %rsi
	movq -8(%rbp), %rdi
	call contient
	xorq %r15, %r15
	cmpq $0, %rax
	sete %r15b
	movq %r15, %rax
	addq $0, %rax
	jz L158
	movq $1, %rax
	jmp L157
	jmp L158
L171:
	movq $17, %rsi
	movq -8(%rbp), %rdi
	call contient
	movq %rax, %rsi
	addq $0, %rsi
	jz L166
	movq $1, %rsi
	jmp L165
	jmp L166
L179:
	movq $0, %rsi
	movq -8(%rbp), %rdi
	call contient
	movq %rax, %rsi
	xorq %r15, %r15
	cmpq $0, %rsi
	sete %r15b
	movq %r15, %rsi
	addq $0, %rsi
	jz L173
	movq $1, %rsi
	jmp L172
	jmp L173
	.data
