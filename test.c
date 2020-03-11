int fact(int acc, int n){
	if (n <= 1) return acc;
	return fact(acc * n, n - 1);
}
