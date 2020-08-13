package org.getaviz.generator.output.abap_output;

public class metropolis_AFrame implements ABAP_OutputFormat {


    public String head() {
        return "<!DOCTYPE html>" +
                "\n" +
                "<html>" +
                "\n" +
                "\t <head>" +
                "\n" +
                "\t\t <meta charset=\"utf-8\">" +
                "\n" +
                "\t    <title>Metropolis</title>" +
                "\n" +
                "\t    <meta name=\"description\" content=\"Getaviz\">" +
                "\n" +
                "\t    <script src=\"https://aframe.io/releases/1.0.2/aframe.min.js\"></script>" +
                "\n" +
                "\t </head>" +
                "\n" +
                "\t <body>" +
                "\n" +
                "\t\t <a-scene>" +
                "\n" +
                "\t\t\t <a-entity id=\"rig\" position=\"25 10 0\">" +
                "\n" +
                "\t\t\t <a-entity camera look-controls wasd-controls=\"acceleration: 10000\" position=\"-10 10 0\"></a-entity>" +
                "\n" +
                "\t\t\t </a-entity>" +
                "\n" +
                "\t\t\t <a-assets>" +
                "\n" +
                "\t\t\t <img id=\"sky\" crossorigin=\"anonymous\" src=\"https://cdn.glitch.com/bb053900-3a29-4a59-a2e0-f06514f3857d%2Fsky_pano.jpg?\">" +
                "\n" +
                "\t\t\t <img id=\"sea\" crossorigin=\"anonymous\" src=\"https://static.vecteezy.com/system/resources/previews/000/108/621/original/blue-grunge-free-vector-texture.jpg\">" +
                "\n" +
                "\t\t\t <img id=\"ground\" crossorigin=\"anonymous\" src=\"https://cdn.glitch.com/bb053900-3a29-4a59-a2e0-f06514f3857d%2F791f5b1689d633dafee4f889b5f8e44b.jpg?\">" +
                "\n" +
                "\t\t\t <a-asset-item id=\"polyMountain\" src=\"https://cdn.glitch.com/bb053900-3a29-4a59-a2e0-f06514f3857d%2FpolyMountain.glb\"></a-asset-item>" +
                "\n" +
                "\t\t\t </a-assets>" +
                "\n" +
                "\t\t\t <a-sky src=\"#sky\"></a-sky>" +
                "\n" +
                "\t\t\t <a-plane src=\"#ground\" height=\"750\" width=\"750\" rotation=\"-90 0 0\" position=\"0 0 0\" repeat=\"30 30\"></a-plane>" +
                "\n";

    }

    public String tail() {
        return "\t\t </a-scene>" +
                "\n" +
                " \t </body>" +
                "\n" +
                "</html>" +
                "\n";
    }

}
