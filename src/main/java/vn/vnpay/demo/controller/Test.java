package vn.vnpay.demo.controller;

public class Test {
    public static void main(String args[]) {
        // creating one object
        Line obj = new Line();

        // creating two threads
        Train train1 = new Train(obj);
        Train train2 = new Train(obj);

        // threads start their execution
        train1.start();
        train2.start();
    }
}

class Line {

    // phương thức đồng bộ hóa
    synchronized void getLine() {
        for (int i = 0; i < 3; i++) {
            System.out.println(i);
            try {
                Thread.sleep(400);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}

class Train extends Thread {
    // Reference to Line's Object.
    Line line;

    Train(Line line) {
        this.line = line;
    }

    @Override
    public void run() {
        line.getLine();
    }
}


