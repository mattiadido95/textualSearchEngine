package it.unipi.dii.mircv.prompt;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("Insert your query ...");
        Scanner scanner = new Scanner(System.in);
        String inputStringa = scanner.nextLine();
        System.out.println("Processing your query: " + inputStringa);
        scanner.close();
    }
}