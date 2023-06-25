TCanvas* c = new TCanvas("c", "Something", 0, 0, 800, 600);
TF1 *f1 = new TF1("f1","sin(x)", -5, 5);
f1->SetLineColor(kBlue+1);
f1->SetTitle("My graph;x; sin(x)");
f1->Draw();
