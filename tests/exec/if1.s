	.text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	addq $-8, %rsp
	movq $65, %rdi
	movq %rdi, -8(%rbp)
	movq -8(%rbp), %rdi
	call putchar
	movq -8(%rbp), %rax
	addq $0, %rax
	jz L50
	movq $66, %rdi
	movq %rdi, -8(%rbp)
L50:
	movq -8(%rbp), %rdi
	call putchar
	movq -8(%rbp), %rax
	addq $0, %rax
	jnz L46
L43:
	movq $0, %rax
L42:
	addq $0, %rax
	jz L39
	movq $67, %rdi
	movq %rdi, -8(%rbp)
L39:
	movq -8(%rbp), %rdi
	call putchar
	movq -8(%rbp), %rax
	addq $0, %rax
	jnz L35
L32:
	movq $0, %rax
L31:
	addq $0, %rax
	jz L28
	movq $68, %rdi
	movq %rdi, -8(%rbp)
L28:
	movq -8(%rbp), %rdi
	call putchar
	movq -8(%rbp), %rax
	addq $0, %rax
	jz L24
L21:
	movq $1, %rax
L20:
	addq $0, %rax
	jz L17
	movq $69, %rdi
	movq %rdi, -8(%rbp)
L17:
	movq -8(%rbp), %rdi
	call putchar
	movq -8(%rbp), %rax
	addq $0, %rax
	jz L13
L10:
	movq $1, %rax
L9:
	addq $0, %rax
	jz L6
	movq $70, %rdi
	movq %rdi, -8(%rbp)
L6:
	movq -8(%rbp), %rdi
	call putchar
	movq %rax, %rdi
	movq $10, %rdi
	call putchar
	movq $0, %rax
	movq %rbp, %rsp
	popq %rbp
	ret
	jmp L6
L13:
	movq $1, %rax
	addq $0, %rax
	jnz L10
	movq $0, %rax
	jmp L9
	jmp L10
	jmp L17
L24:
	movq $0, %rax
	addq $0, %rax
	jnz L21
	movq $0, %rax
	jmp L20
	jmp L21
	jmp L28
L35:
	movq $1, %rax
	addq $0, %rax
	jz L32
	movq $1, %rax
	jmp L31
	jmp L32
	jmp L39
L46:
	movq $0, %rax
	addq $0, %rax
	jz L43
	movq $1, %rax
	jmp L42
	jmp L43
	jmp L50
	.data
