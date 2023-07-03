#include "TF1.h"
#include "TCanvas.h"

void drawFunc(double (*func)(double*, double*), double parameter) {
  TF1 *Fun  = new TF1("Fun",func,-5.001,5.,2);
  Fun->SetParameter(0, parameter);
  TCanvas *c = new TCanvas("c", "Something", 0, 0, 800, 600);
  Fun->Draw();
  c->Print("demo4.pdf");
}

double line(double *x, double *parameter) {
  return parameter[0]*x[0];
}

int main(int argc, char **argv) {
  drawFunc(&line, 3.0);
  return 0;
}
