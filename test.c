int fact(int acc, int n) {
    if (n <= 1) return acc;
    return fact(acc * n, n - 1);
}

int main() {
    putchar(fact(1, 5));
    return 0;
}