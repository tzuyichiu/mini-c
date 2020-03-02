struct S { int a; int b; };
int x;
int main() {
  struct S *p;
  p = sbrk(sizeof(p));
  p->a = 40;
  p->b = 2;
  return p->a + p->b;
}
