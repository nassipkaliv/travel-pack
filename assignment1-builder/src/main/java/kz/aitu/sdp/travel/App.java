package kz.aitu.sdp.travel;

import java.util.Scanner;

public final class App {

    public static void main(String[] args) {
        new TravelConsole(new Scanner(System.in), System.out).run();
    }
}
