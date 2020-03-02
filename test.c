struct S { int a; int b; };
int main() {
  struct S *p;
  p = sbrk(sizeof(struct S));
  p->a = 40;
  p->b = 2;
  return p->a + p->b;
}
