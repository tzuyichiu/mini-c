int fact(int acc, int n){
	if (n <= 1) return acc;
	return fact(acc * n, n - 1);
}

int main() {
	return fact(1,4);
}
