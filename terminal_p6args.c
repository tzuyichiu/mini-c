int fact(int acc, int n, int a, int b, int c, int d, int e, int f) {
  if (n <= 1) return acc;
  else {
    putchar(65+a);
    return fact(acc * n, n - 1, b, c, d, e, f, a);
  }
}

int main(){
  fact(1,4,1,2,3,4,5,6);
  return 0;
}