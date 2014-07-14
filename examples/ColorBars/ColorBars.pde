import org.chrisjr.loom.*;

Loom loom;
Pattern[] patterns;

void setup() {
  size(400, 400);
  
  loom = new Loom(this);
  patterns = new Pattern[1];
  for (int i = 0; i < patterns.length; i++) {
    patterns[i] = new Pattern(loom)
      .extend("0123456")
      .loop();
  }
  patterns[0].asColor(#EBEBEB, 
    #EBEB10, 
    #10EBEB, 
    #10EB10, 
    #EB10EB, 
    #EB1010, 
    #1010EB);
  
  loom.play();
}

void draw() {
  background(0);
  patterns[0].rect(0, 0, width, height);
}

